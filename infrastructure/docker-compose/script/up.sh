#!/bin/bash

cd /Users/riza/Documents/Self-Learn/SpringBoot/food-ordering-system/infrastructure/docker-compose/udemy/kafka-cluster

source .env

echo "Creating network $GLOBAL_NETWORK..."
docker network create $GLOBAL_NETWORK

echo "Starting Zookeeper..."
docker-compose -f zookeeper.yml up -d
echo "Waiting 5 secs for Zookeeper..."
sleep 5
echo "Starting Kafka Cluster..."
docker-compose -f kafka_cluster.yml up -d
echo "Waiting 15 secs for Kafka Cluster..."
sleep 15
echo "Initializing Kafka Cluster..."
docker-compose -f init_kafka.yml up
echo "Done 👌"
