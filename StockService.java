// camel-k: language=java

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.Predicate;
import org.apache.camel.util.json.JsonObject;

public class StockService  extends org.apache.camel.builder.RouteBuilder {

    private Map<String, Integer> stocks = new ConcurrentHashMap<>();

    private static final String ORDER_ID = "order-id";
    private static final String STOCK_OP_RESULT = "stock-op-result";
    private static final String STOCK_OP_MESSAGE = "stock-op-message";

    @Override
    public void configure() throws Exception {
        from("knative:channel/stocks")
            .unmarshal()
            .json()
            .log("Processing stock operation")
            .process()
                .exchange(e -> {
                    JsonObject body = new JsonObject(e.getIn().getBody(Map.class));
                    e.setProperty("send-result", body.getString("order-id") != null);
                    if (e.getProperty("send-result", Boolean.class)) {
                        e.setProperty(ORDER_ID, body.getString("order-id"));
                    }
                    String itemId = body.getString("item-id");
                    if (itemId == null) {
                        e.setProperty(STOCK_OP_RESULT, false);
                        e.setProperty(STOCK_OP_MESSAGE, "item-id missing");
                        return;
                    }
                    Integer quantity = body.getInteger("quantity");
                    if (quantity == null) {
                        e.setProperty(STOCK_OP_RESULT, false);
                        e.setProperty(STOCK_OP_MESSAGE, "quantity missing");
                        return;
                    }
                    if (quantity == 0) {
                        e.setProperty(STOCK_OP_RESULT, false);
                        e.setProperty(STOCK_OP_MESSAGE, "No quantity specified");
                    } else {
                        try {
                            Integer newValue = stocks.compute(itemId, (k, v) -> {
                                if (quantity<0 && (v == null || v < (quantity * -1))) {
                                    throw new IllegalStateException("Insufficient stock for this operation, stock "+v+" request "+(-1 * quantity));
                                } else if (quantity>0 && v == null) {
                                    return quantity;
                                }
                                return v + quantity;
                            });
                            e.setProperty(STOCK_OP_RESULT, true);
                            e.setProperty(STOCK_OP_MESSAGE, "New stock "+newValue);
                        } catch (IllegalStateException ex) {
                            e.setProperty(STOCK_OP_RESULT, false);
                            e.setProperty(STOCK_OP_MESSAGE, ex.getMessage());
                        }

                    }

                })
            .setBody(e -> {
                JsonObject result = new JsonObject();
                result.put("result", e.getProperty(STOCK_OP_RESULT));
                result.put("message", e.getProperty(STOCK_OP_MESSAGE));
                result.put("order-id", e.getProperty(ORDER_ID));
                return result;
            })
            .marshal()
            .json()
            .choice()
                .when(e -> e.getProperty("send-result", Boolean.class))
                    .log("Sending message to processed-stocks channel")
                    .to("knative:channel/processed-stocks")
                .otherwise()
                    .log("Not sending message to processed-stocks channel")
                .end();
    }

}