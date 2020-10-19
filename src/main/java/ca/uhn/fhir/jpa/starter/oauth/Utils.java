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
    isPostgre = System.getenv("FHIR_PG_TOKEN_URL") != null;
    if(!isPostgre) {
      String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
      MongoClient mongoClient = new MongoClient(new MongoClientURI(connectionString));
      usersDB = mongoClient.getDatabase("users");
    } else {
      try {
        Class.forName("org.postgresql.Driver");
        String connectionString =System.getenv("FHIR_PG_TOKEN_URL");
        String postgreUser =System.getenv("FHIR_PG_TOKEN_USER_NAME");
        String postgrePass =System.getenv("FHIR_PG_TOKEN_PASSWORD");
        Connection postgreCon = DriverManager.getConnection(connectionString,postgreUser,postgrePass);
        //postgreCon.setAutoCommit(false);
        postgreStm = postgreCon.createStatement();
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

  // code-review: remove unneeded methods
  public static org.bson.Document GetUserByToken(String authToken) {
    Document authTokenDocument;
    authTokenDocument = AuthenticateToken(authToken);

    if (authTokenDocument != null) {
      return GetUserByID(authTokenDocument.getString("uid"));
    }
    return null;
  }

  // code-review: try a method not to use  if(isPostgre) since we dont want it in every method we might implement
  public static TokenRecord getTokenRecord(String token){
    if(isPostgre){
      return getTokenRecordPostgre(token);
    }
    return getTokenRecordMongo(token);
  }

  // code-review: both getTokenRecordPostgre and getTokenRecordMongo share 90% of the logic try to rewrite them using as less code duplication as possible
  private static TokenRecord getTokenRecordPostgre(String token){
    try {
      // code-review: why are you using CROSS JOIN use JOIN instead
      // also try to make the query more readable in the code
      ResultSet resultSet = postgreStm.executeQuery("select u.\"id\", u.ispractitioner, o.accesstoken, o.issuedat, o.expiresin from \"public\".oauthaccesstoken o,\"public\".user u where o.uid = u.\"id\"and o.accesstoken = '"+token+"';");
      if(!resultSet.next()) return null;
      String userId = resultSet.getString("id");
      boolean isPractitioner = resultSet.getBoolean("ispractitioner");
      long issued = -1;
      long expire = -1;

      // code-review: no need for try catch just do a simple if condition to decide and also you are not throwing in the catch so no matter what the result you will go to the return statement
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

  private static TokenRecord getTokenRecordMongo(String token){
    Document authTokenDocument;
    authTokenDocument = AuthenticateToken(token);

    if (authTokenDocument != null) {
      Document userDocument = GetUserByID(authTokenDocument.getString("uid"));
      String userId = userDocument.getString("_id");
      Boolean isPractitioner = userDocument.getBoolean("isPractitioner");
      if (isPractitioner == null) isPractitioner = false;
      long issued = -1;
      long expire = -1;

      // code-review: no need for try catch just do a simple if condition to decide and also you are not throwing in the catch so no matter what the result you will go to the return statement
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