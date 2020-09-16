package ca.uhn.fhir.jpa.starter.oauth;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.eq;

public class Utils {
  private static MongoDatabase usersDB;
  static
  {
     String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
     MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
     usersDB = mongoClient.getDatabase("users");
  }

  public static String getUserIdIdExists(String authToken) {
    MongoCollection<org.bson.Document> oAuthAccessTokensCollection = usersDB.getCollection("OAuthAccessToken");
    var authTokenDocument = oAuthAccessTokensCollection.find(eq("accessToken", authToken)).first();
    if (authTokenDocument != null)
    {
      return authTokenDocument.getString("uid");
    }

    return null;
  }
}