package ca.uhn.fhir.jpa.starter.oauth;

import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


import org.bson.Document;

import javax.print.Doc;

import static com.mongodb.client.model.Filters.eq;

public class Utils {
  //retunrs a user document
  public static org.bson.Document AuthenticateToken(String authToken) {
    String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
    MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
    MongoDatabase database = mongoClient.getDatabase("users");
    MongoCollection<org.bson.Document> oAuthAccessTokensCollection = database.getCollection("OAuthAccessToken");
    org.bson.Document authTokenDocument = oAuthAccessTokensCollection.find(eq("accessToken", authToken)).first();
    return authTokenDocument;
  }

  public static org.bson.Document GetUserByID(String userID) {
    String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
    MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
    MongoDatabase database = mongoClient.getDatabase("users");
    MongoCollection<org.bson.Document> usersCollection = database.getCollection("user");
    return usersCollection.find(eq("_id", userID)).first();
  }

  public static org.bson.Document GetUserByToken(String authToken) {
        Document authTokenDocument = AuthenticateToken(authToken);
        if(authTokenDocument != null){
          return GetUserByID(authTokenDocument.getString("uid"));
        }
        return null;
  }

  public static org.bson.Document GetClientByToken(String verificationToken) {

    String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
    MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
    MongoDatabase database = mongoClient.getDatabase("users");
    MongoCollection<org.bson.Document> oAuthClientApplication = database.getCollection("OAuthClientApplication");
    org.bson.Document client  =  oAuthClientApplication.find(eq("verificationToken",verificationToken)).first();
    if(client != null){
      return client;
    }
    return null;
  }

  public  static org.bson.Document RegisterApp(String clientID){
    Document application = new Document();
    application.put("clientID",clientID);
    return null;
  }
}