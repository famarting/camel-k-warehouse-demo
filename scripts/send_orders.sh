#!/bin/bash
NUM_OF_ORDERS=${1:-5}
QUANTITY_PER_ORDER=${2:-1}

for i in $(seq 1 $NUM_OF_ORDERS)
do
    curl -i -d '{"item-id":"123456", "quantity":'$QUANTITY_PER_ORDER'}' -H "Content-Type: application/json" -X POST $(oc get ksvc orders-rest-api --template='{{ .status.url }}')/orders
    echo
done