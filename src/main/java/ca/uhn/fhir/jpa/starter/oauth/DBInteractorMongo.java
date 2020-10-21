package ca.uhn.fhir.jpa.starter.oauth;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class DBInteractorMongo implements IDBInteractor {

  private final MongoDatabase usersDB;
  public DBInteractorMongo(String connectionString) {
    MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
    usersDB = mongoClient.getDatabase("users");
  }

  org.bson.Document AuthenticateToken(String authToken) {

    MongoCollection<Document> oAuthAccessTokensCollection = usersDB.getCollection("OAuthAccessToken");
    return oAuthAccessTokensCollection.find(eq("accessToken", authToken)).first();
  }

  org.bson.Document GetUserByID(String userID) {

    MongoCollection<org.bson.Document> usersCollection = usersDB.getCollection("user");
    return usersCollection.find(eq("_id", userID)).first();
  }

  @Override
  public TokenRecord getTokenRecord(String token) {
    Document authTokenDocument;
    authTokenDocument = AuthenticateToken(token);

    if (authTokenDocument != null) {
      Document userDocument = GetUserByID(authTokenDocument.getString("uid"));
      String userId = userDocument.getString("_id");
      Boolean isPractitioner = userDocument.getBoolean("isPractitioner");
      if (isPractitioner == null) isPractitioner = false;
      long issued = -1;
      long expire = -1;
      return new TokenRecord(userId, token, isPractitioner, issued, expire);
    }
    return null;
  }
}
