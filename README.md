# Warehouse example application with camel-k

This repository contains an example application for orders processing in a warehouse, with the purpose of trying and showing different technologies. This is a diagram showcasing in high level how the application works.

![app diagram](/img/diagram.png)

## How it's implemented?

In this case the application is based on [camel-k] to implement different components of the application, like the REST API or the Stock Service. [Knative] eventing is also used, as camel-k is based on knative, and it's used to provide the cannels like the queue and topic showed in the diagram.

## How to run it?

First of all a kubernetes or openshift cluster with camel-k, knative serving and knative-eventing operators installed.

The files I used to set up the operators are included as well as a bunch of scripts in the scripts folder to deploy and try the system.

### Cluster set up

Install camel-k
```
kamel install --cluster-setup
kamel install
```
Install knative
```
oc apply -f infra/operators/openshift-serverless-sub.yaml
oc apply -f infra/knative-serving.yaml
oc apply -f infra/operators/knative-eventing-sub.yaml
oc apply -f infra/knative-eventing.yaml
oc apply -f infra/channels
```
### Demo
```
./scripts/deploy.sh
./scripts/add-stock.sh
./scripts/send_orders.sh
./scripts/watch-demo-logs.sh
```


[camel-k]: <https://github.com/apache/camel-k>
[Knative]: <https://knative.dev/>