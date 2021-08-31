# Fiscal Event Post Processor

We are going to store all the **fiscal-event** in 2 data stores:
1. MongoDB
2. Druid

## Kafka to DataStore Sink

### MongoDB Sink
We can use kafka-connect to push the data from a kafka topic to MongoDB. We will have to follow these steps to start the connector:
1. Connect(port-forward) with the kafka-connect server.
2. We can create a new connector with a POST API call to localhost:8083/connectors.
3. The request body for that API call is written in the file [fiscal-event-mongodb-sink](./fiscal-event-mongodb-sink.json). 
4. Within that file, wherever ${---} replace it with the actual value based on the environment. Get ${mongo-db-authenticated-uri} from the configured secrets of the environment. 
5. *(Optional)* Verify and make changes to the topic names.  
6. The connector is ready. You can check it using API call GET localhost:8083/connectors. 

### Druid Sink
We will use the Druid console to start ingesting data from a kafka topic to the Druid data store. Please follow 
the steps mentioned below to start the *Druid Supervisor*:
1. Open the Druid console
2. Go to the **Load Data** section
3. Select **Other**
4. Click on **Submit Supervisor**
5. Copy...Paste the JSON from the [druid-ingestion-config.json](./druid-ingestion-config.json) file in the available 
   text box.
6. You should verify the kafka topic name and the kafka bootstrap server address before submitting the config. 
7. Now submit the config and the data ingestion should start into the **fiscal-event** _datasource_. 
