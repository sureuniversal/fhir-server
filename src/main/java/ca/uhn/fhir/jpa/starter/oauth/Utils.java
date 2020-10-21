package ca.uhn.fhir.jpa.starter.oauth;

public class Utils {

  private final static IDBInteractor interactor;

  static {
    if (System.getenv("FHIR_PG_TOKEN_URL") == null) {
      String connectionString = System.getenv("FHIR_MONGO_DATASOURCE_URL");
      interactor = new DBInteractorMongo(connectionString);
    } else {
      String connectionString = System.getenv("FHIR_PG_TOKEN_URL");
      String postgresUser = System.getenv("FHIR_PG_TOKEN_USER_NAME");
      String postgresPass = System.getenv("FHIR_PG_TOKEN_PASSWORD");
      interactor = new DBInteractorPostgres(connectionString,postgresUser,postgresPass);
    }
  }

  public static TokenRecord getTokenRecord(String token) {
    return interactor.getTokenRecord(token);
  }
}