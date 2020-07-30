package ca.uhn.fhir.jpa.starter.oauth;

import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


import org.bson.Document;
import org.springframework.web.client.HttpClientErrorException;

import javax.print.Doc;

import static com.mongodb.client.model.Filters.eq;

public class Utils {
  //returns a user document
  public static class TokenNotFoundException extends Exception{
    public TokenNotFoundException(){
      super("Token not found");
    }
  }
  public static class TokenExpiredException extends Exception{
    public TokenExpiredException(){
      super("Token has expired");
    }
  }
  public static org.bson.Document AuthenticateToken(String authToken) throws TokenNotFoundException,TokenExpiredException{
    String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
    MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
    MongoDatabase database = mongoClient.getDatabase("users");
    MongoCollection<org.bson.Document> oAuthAccessTokensCollection = database.getCollection("OAuthAccessToken");
    org.bson.Document authTokenDocument = oAuthAccessTokensCollection.find(eq("accessToken", authToken)).first();
    if(authTokenDocument==null){
      throw new TokenNotFoundException();
    }
    long expireTime = authTokenDocument.getDate("issuedAt").getTime()/1000+ authTokenDocument.getInteger("expiresIn");
    long now = java.time.Instant.now().getEpochSecond();
    if (expireTime < now)
    {
      throw new TokenExpiredException();
    }
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
}