package ca.uhn.fhir.jpa.starter.oauth;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


import org.bson.Document;
import org.springframework.web.client.HttpClientErrorException;

import javax.print.Doc;

import static com.mongodb.client.model.Filters.eq;

public class Utils {
  private static MongoDatabase usersDB;
  static
  {
     String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
     MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
     usersDB = mongoClient.getDatabase("users");
  }

  public static org.bson.Document AuthenticateToken(String authToken) {

    MongoCollection<org.bson.Document> oAuthAccessTokensCollection = usersDB.getCollection("OAuthAccessToken");
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
    Document authTokenDocument;
    try {
      authTokenDocument = AuthenticateToken(authToken);
    }
    catch (Exception e) {
      return null;
    }
    if (authTokenDocument != null) {
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

  public static boolean isTokenValid(String authToken){
    return AuthenticateToken(authToken)!=null;
  }
}
