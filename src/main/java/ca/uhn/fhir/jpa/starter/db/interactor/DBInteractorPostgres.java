package ca.uhn.fhir.jpa.starter.db.interactor;

import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;

import java.sql.*;

public class DBInteractorPostgres implements IDBInteractor {

  private final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(DBInteractorPostgres.class);

  private Statement postgresStm;
  private Connection postgresCon;

  public DBInteractorPostgres(String connectionString, String postgresUser, String postgresPass) {
    try {
      Class.forName("org.postgresql.Driver");
      postgresCon = DriverManager.getConnection(connectionString, postgresUser, postgresPass);
      postgresStm = postgresCon.createStatement();
    } catch (SQLException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public TokenRecord getTokenRecord(String token) {
    try {
      postgresStm = postgresCon.createStatement();
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
      postgresStm.close();
      return new TokenRecord(userId, token, isPractitioner, issued, expire);
    } catch (SQLException e) {
      ourLog.error("postgreSQL error:", e);
      return null;
    }
  }
}
