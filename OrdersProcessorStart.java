
// camel-k: language=java

import java.util.Map;

import org.apache.camel.util.json.JsonObject;

public class OrdersProcessorStart extends org.apache.camel.builder.RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("knative:channel/orders")
            .unmarshal()
            .json()
            .log("Processing order -> ${body.toString}")
            .setBody(e -> {
                JsonObject body = new JsonObject(e.getIn().getBody(Map.class));
                int q = body.getInteger("quantity") * -1;
                body.put("quantity", q);
                return body;
            })
            .log("Order processed -> ${body.toString}")
            .marshal()
            .json()
            .log("Sending stock request")
            .to("knative:channel/stocks");
    }

}