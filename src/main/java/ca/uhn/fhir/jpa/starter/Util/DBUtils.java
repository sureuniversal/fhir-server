package ca.uhn.fhir.jpa.starter.Util;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.jpa.starter.Models.TokenRecord;
import ca.uhn.fhir.jpa.starter.interactor.DBInteractorLoopback;
import ca.uhn.fhir.jpa.starter.interactor.DBInteractorPostgres;
import ca.uhn.fhir.jpa.starter.interactor.IDBInteractor;

public class DBUtils {
  private final static IDBInteractor interactor;

  static
  {
    if(System.getenv("LOOPBACK_URL")!=null){
      interactor = new DBInteractorLoopback(System.getenv("LOOPBACK_URL"));
    } else {
      var connectionString = System.getenv("FHIR_PG_DATASOURCE_URL") + "/" + System.getenv("FHIR_PG_DATASOURCE_TOKEN_DB");
      var postgresUser = System.getenv("FHIR_PG_DATASOURCE_USER_NAME");
      var postgresPass = System.getenv("FHIR_PG_DATASOURCE_PASSWORD");
      interactor = new DBInteractorPostgres(connectionString, postgresUser, postgresPass);
    }
  }

  public static TokenRecord getTokenRecord(String token) {
      return interactor.getTokenRecord(token);
  }

  public static long getCacheTTL()
  {
    return HapiProperties.getCacheTtl(240000);
  }
 }