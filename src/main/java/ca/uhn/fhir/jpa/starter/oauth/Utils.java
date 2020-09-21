package ca.uhn.fhir.jpa.starter.oauth;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

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

    MongoCollection<org.bson.Document> usersCollection = usersDB.getCollection("user");
    return usersCollection.find(eq("_id", userID)).first();
  }

  public static org.bson.Document GetUserByToken(String authToken) {
    Document authTokenDocument;
    authTokenDocument = AuthenticateToken(authToken);

    if (authTokenDocument != null) {
      return GetUserByID(authTokenDocument.getString("uid"));
    }
    return null;
  }
}
