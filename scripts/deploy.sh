kamel run --dependency=camel-rest --dependency=camel-undertow OrdersRestApi.java
kamel run OrdersProcessorStart.java
kamel run OrdersProcessorFinish.java
kamel run --trait knative-service.min-scale=1 --trait knative-service.max-scale=1 StockService.java
kamel run --dependency=camel-rest --dependency=camel-undertow StockRestApi.java
kamel run --trait knative-service.min-scale=1 --trait knative-service.max-scale=1 DemoService.java