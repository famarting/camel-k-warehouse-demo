
// camel-k: language=java

import java.util.Map;

import org.apache.camel.util.json.JsonObject;

public class OrdersProcessorFinish extends org.apache.camel.builder.RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("knative:channel/processed-stocks")
            .unmarshal()
            .json()
            .log("Stock operation result received -> ${body[order-id]} message ${body[message]}")
            .transform()
            .body(Map.class, (b, headers) -> {
                JsonObject body = new JsonObject(b);
                JsonObject result = new JsonObject();
                result.put("order-id", body.get("order-id"));
                result.put("approved", body.get("result"));
                result.put("message", body.get("message"));
                return result;
            })
            .log("Order processed")
            .marshal()
            .json()
            .to("knative:channel/processed-orders");
    }

}