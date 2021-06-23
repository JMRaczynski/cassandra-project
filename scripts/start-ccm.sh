#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Please provide exactly one argument - ip prefix (without last digit, f.e. '192.168.1.24', which will turn into '192.168.1.241'"
    exit -1
fi

ccm create test -i $1 -v 3.11.8 -n 1 -s
ccm node1 cqlsh -f ./scripts/create-keyspace.cql
ccm node1 cqlsh -f ./scripts/create-schema.cql
ccm node1 cqlsh -f ./scripts/import-sample-data.cql
