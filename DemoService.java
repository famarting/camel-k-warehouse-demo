import java.util.Map;

// camel-k: language=java

public class DemoService extends org.apache.camel.builder.RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("knative:channel/processed-orders")
            .unmarshal()
            .json()
            .transform()
            .body(Map.class, (b, headers) -> {
                return b.toString();
            })
            .log("Order result")
            .log("${body}");
    }

}