package ca.uhn.fhir.jpa.starter.Util;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.jpa.starter.Models.TokenRecord;
import ca.uhn.fhir.jpa.starter.interactor.DBInteractorPostgres;
import ca.uhn.fhir.jpa.starter.interactor.IDBInteractor;

public class DBUtils {
  private final static IDBInteractor interactor;

  static
  {
      var connectionString = System.getenv("FHIR_PG_DATASOURCE_URL") + "/users";
      var postgresUser = System.getenv("FHIR_PG_DATASOURCE_USER_NAME");
      var postgresPass = System.getenv("FHIR_PG_DATASOURCE_PASSWORD");
      interactor = new DBInteractorPostgres(connectionString, postgresUser, postgresPass);
  }

  public static TokenRecord getTokenRecord(String token) {
      return interactor.getTokenRecord(token);
  }

  public static long getCacheTTL()
  {
    return HapiProperties.getCacheTtl(240000);
  }
 }