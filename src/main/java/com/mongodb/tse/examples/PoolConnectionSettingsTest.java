package com.mongodb.tse.examples;

import com.mongodb.Block;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.connection.ClusterConnectionMode;

import java.util.concurrent.TimeUnit;
import org.bson.Document;



public class PoolConnectionSettingsTest {

  public static void main(String[] args) {

    String appName = "test-ttl";
    int minPoolSize = 3;
    int maxPoolSize = 5;
    int maxConnectionIdleTime = 20;
    int connectTimeout = 10;          // When the client can not connect, the exception is thrown: com.mongodb.MongoSocketOpenException: Exception opening socket
    int readTimeout = 120;

    Block<Document> printBlock = new Block<Document>() {
      @Override
      public void apply(final Document document) {
        System.out.println(document.toJson());

        /**  Sleep to simulate time processing per document.
        try {
          Thread.sleep(1); // <---
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        */

      }
    };

    // A sample to connect to a MongoDB database using  connection pool settings.
    ConnectionString connection = new ConnectionString(
            "mongodb+srv://cluster0-om7f7.mongodb.net/test?retryWrites=true&w=majority,");
    String username = "administrator";
    char[] password = "monotematico".toCharArray();

    final MongoClientSettings config =
        MongoClientSettings.builder()
            .applyToClusterSettings(
                cluster ->
                    cluster
                        .applyConnectionString(connection)
                        .requiredReplicaSetName(connection.getRequiredReplicaSetName())
                        .mode(ClusterConnectionMode.MULTIPLE))
            .applyToConnectionPoolSettings(
                pool ->
                    pool.minSize(minPoolSize)
                        .maxSize(maxPoolSize)
                        .maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.SECONDS))
            .applyToSslSettings(ssl -> ssl.enabled(true).invalidHostNameAllowed(true))
            .applyToSocketSettings(
                socket ->
                    socket
                        .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                        .readTimeout(readTimeout, TimeUnit.SECONDS))
            .credential(MongoCredential.createCredential(username, "admin", password))
            .applicationName(appName)
            .build();

    try {
      MongoClient client = MongoClients.create(config);

      MongoDatabase db = client.getDatabase("test");
      int i = 0;
      while (true) {
        db.getCollection("foo").insertOne(new Document("count", ++i));

        Thread.sleep(1000);
        System.out.println("Inserted " + i);

        int age = 18 + (int)(Math.random() * ((65 - 18) + 1));
        System.out.println("Find age= " + age);
        FindIterable<Document> c = db.getCollection("onemill").find(new Document("age", age));
        c.forEach(printBlock);

        int number = 1 + (int)(Math.random() * ((1000000 - 1) + 1));
        UpdateResult u = db.getCollection("onemill").updateMany(new Document("age", age), new Document("$set", new Document("m", number)));
        System.out.println(u);

      }
    } catch (MongoException e) {
      System.out.println(e.getMessage());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
