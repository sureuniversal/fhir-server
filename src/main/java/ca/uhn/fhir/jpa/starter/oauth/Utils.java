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
  private static MongoDatabase usersDB = null;
  private static void init()
  {
    String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
    MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
    usersDB = mongoClient.getDatabase("users");
  }
  public static org.bson.Document AuthenticateToken(String authToken) {
    if(usersDB == null){
      init();
    }
    MongoCollection<org.bson.Document> oAuthAccessTokensCollection = usersDB.getCollection("OAuthAccessToken");
    org.bson.Document authTokenDocument = oAuthAccessTokensCollection.find(eq("accessToken", authToken)).first();
    return authTokenDocument;
  }
  public static boolean isTokenValid(String authToken){
    return AuthenticateToken(authToken)!=null;
  }
}