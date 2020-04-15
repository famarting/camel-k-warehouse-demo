// camel-k: language=java

//
// To run this integrations use:
//
//     kamel run --name=rest-with-undertow --dependency=camel-rest --dependency=camel-undertow examples/RestWithUndertow.java
//
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;

public class OrdersRestApi extends org.apache.camel.builder.RouteBuilder {
    @Override
    public void configure() throws Exception {

        restConfiguration()
            .component("undertow")
            .host("0.0.0.0")
            .port("8080");

        rest()
            .consumes("application/json")
            .post("/orders")
            .to("direct:orders-validation");

        from("direct:orders-validation")
            .log("#### New incoming http message ####")

            .unmarshal()
            .json()

            .choice()
                .when().method(isValid())
                    .log("Valid order")
                    .transform()
                    .body(Map.class, (b, headers) -> {
                        b.put("order-id", UUID.randomUUID().toString());
                        return b;
                    })
                    .setProperty("order-body").body()
                    .marshal().json()
                    .to("knative:channel/orders")
                    .setBody(e -> {
                        return e.getProperty("order-body");
                    })
                    .log("Order enqueued -> ${body.toString}")
                .otherwise()
                    .log("Invalid order")
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                    .setBody(e -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("message", "Invalid order");
                        result.put("order", e.getIn().getBody(Map.class));
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
