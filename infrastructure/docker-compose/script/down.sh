#!/bin/bash

cd /Users/riza/Documents/Self-Learn/SpringBoot/food-ordering-system/infrastructure/docker-compose/udemy/kafka-cluster

source .env

echo "Removing Kafka Cluster..."
docker-compose -f kafka_cluster.yml down -v
echo "Sleeping 10 secs..."
sleep 10
echo "Removing Zookeeper..."
docker-compose -f zookeeper.yml down -v
echo "Sleeping 5 secs..."
sleep 5
docker-compose -f init_kafka.yml down

echo "Removing $GLOBAL_NETWORK network..."
docker network rm kafka-network $GLOBAL_NETWORK
echo "Done 👌"
