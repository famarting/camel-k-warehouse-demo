// camel-k: language=java

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;

public class StockRestApi extends org.apache.camel.builder.RouteBuilder {
    @Override
    public void configure() throws Exception {

        restConfiguration()
            .component("undertow")
            .host("0.0.0.0")
            .port("8080");

        rest()
            .consumes("application/json")
            .post("/stocks")
            .to("direct:stock-add");

        from("direct:stock-add")
            .log("#### New incoming http message ####")

            .unmarshal()
            .json()

            .choice()
                .when().method(isValid())
                    .log("Valid stock op")
                    .setProperty("req-body").body()
                    .marshal().json()
                    .to("knative:channel/stocks")
                    .setBody(e -> {
                        return e.getProperty("req-body");
                    })
                .otherwise()
                    .log("Invalid stock op")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                    .setBody(e -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("message", "Invalid body");
                        result.put("body", e.getIn().getBody(Map.class));
                        return result;
                    })
            .end()

            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            
            .marshal()
            .json();
                    
    }

    Predicate isValid() {
        return exchange -> {
            if ( !(exchange.getIn().getBody() instanceof Map)) {
                return false;
            }
            Map<?, ?> order = exchange.getIn().getBody(Map.class);
            try {
                return order != null 
                && order.containsKey("item-id") && order.containsKey("quantity") && order.get("quantity")!=null && order.get("quantity") instanceof Integer && (Integer) order.get("quantity")>0;
            } catch (RuntimeException e) {
                return false;
            }
        };
    }

}
