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
//  private static Connection postgreCon = null;
  private static Statement postgreStm = null;
  private static final boolean isPostgre;

  static
  {
    isPostgre = System.getenv("FHIR_PG_TOKEN_URL") == null;
    if(isPostgre) {
      String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
      MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
      usersDB = mongoClient.getDatabase("users");
    } else {
      try {
        String connectionString =System.getenv("FHIR_PG_TOKEN_URL");
        Connection postgreCon = DriverManager.getConnection(connectionString);
        postgreCon.setAutoCommit(false);
        postgreStm = postgreCon.createStatement();
      } catch (SQLException e) {
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

  public static org.bson.Document GetUserByToken(String authToken) {
    Document authTokenDocument;
    authTokenDocument = AuthenticateToken(authToken);

    if (authTokenDocument != null) {
      return GetUserByID(authTokenDocument.getString("uid"));
    }
    return null;
  }

  public static TokenRecord getTokenRecord(String token){
    if(isPostgre){
      return getTokenRecordPostgre(token);
    }
    return getTokenRecordMongo(token);
  }

  public static TokenRecord getTokenRecordPostgre(String token){
    try {
      ResultSet resultSet = postgreStm.executeQuery("select u.\"id\", u.ispractitioner, o.accesstoken, o.issuedat, o.expiresin"+
        "from \"public\".oauthaccesstoken o,\"public\".user u "+
        "where o.uid = u.\"id\" and o.accesstoken = '"+token+"';");
      String userId = resultSet.getString("id");
      boolean isPractitioner = resultSet.getBoolean("ispractitioner");
      long issued = -1;
      long expire = -1;
      try {
        issued = resultSet.getDate("issuedat").getTime()/1000;
        expire = resultSet.getLong("expiresin");
      } catch (SQLException e) {
        e.printStackTrace();
      }
      return new TokenRecord(userId,token,isPractitioner,issued,expire);
    } catch (SQLException e) {
      org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(Utils.class);
      ourLog.error("postgreSQL error:",e);
      return null;
    }
  }

  public static TokenRecord getTokenRecordMongo(String token){
    Document authTokenDocument;
    authTokenDocument = AuthenticateToken(token);

    if (authTokenDocument != null) {
      Document userDocument = GetUserByID(authTokenDocument.getString("uid"));
      String userId = userDocument.getString("_id");
      Boolean isPractitioner = userDocument.getBoolean("isPractitioner");
      if (isPractitioner == null) isPractitioner = false;
      long issued = -1;
      long expire = -1;
      try {
        issued = authTokenDocument.getDate("issuedAt").getTime()/1000;
        expire = authTokenDocument.getInteger("expiresIn");
      } catch (NullPointerException e) {
        e.printStackTrace();
      }
      return new TokenRecord(userId,token,isPractitioner,issued,expire);
    }
    return null;
  }
}