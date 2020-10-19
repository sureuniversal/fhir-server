package ca.uhn.fhir.jpa.starter.oauth;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.sql.*;

import static com.mongodb.client.model.Filters.eq;

public class Utils {
  private static MongoDatabase usersDB = null;
  private static Statement postgresStm = null;
  private static final boolean isPostgres;

  static {
    isPostgres = System.getenv("FHIR_PG_TOKEN_URL") != null;
    if (!isPostgres) {
      String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
      MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
      usersDB = mongoClient.getDatabase("users");
    } else {
      try {
        Class.forName("org.postgresql.Driver");
        String connectionString = System.getenv("FHIR_PG_TOKEN_URL");
        String postgresUser = System.getenv("FHIR_PG_TOKEN_USER_NAME");
        String postgresPass = System.getenv("FHIR_PG_TOKEN_PASSWORD");
        Connection postgresCon = DriverManager.getConnection(connectionString, postgresUser, postgresPass);
        postgresStm = postgresCon.createStatement();
      } catch (SQLException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
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

  public static TokenRecord getTokenRecord(String token) {
    if (isPostgres)
      return getTokenRecordPostgres(token);
    else
      return getTokenRecordMongo(token);
  }

    private static TokenRecord getTokenRecordPostgres(String token) {
    try {
      ResultSet resultSet = postgresStm.executeQuery(
        "select u.id, u.ispractitioner, o.accesstoken, o.issuedat, o.expiresin " +
          "from public.oauthaccesstoken o " +
          "join public.user u on o.uid = u.id " +
          "where o.accesstoken = '" + token + "';");
      if (!resultSet.next()) return null;
      String userId = resultSet.getString("id");
      boolean isPractitioner = resultSet.getBoolean("ispractitioner");
      long issued = -1;
      long expire = -1;
      return new TokenRecord(userId, token, isPractitioner, issued, expire);
    } catch (SQLException e) {
      org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(Utils.class);
      ourLog.error("postgreSQL error:", e);
      return null;
    }
  }

  private static TokenRecord getTokenRecordMongo(String token) {
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