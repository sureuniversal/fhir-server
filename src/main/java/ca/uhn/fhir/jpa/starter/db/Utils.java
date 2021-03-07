package ca.uhn.fhir.jpa.starter.db;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.jpa.starter.db.interactor.DBInteractorPostgres;
import ca.uhn.fhir.jpa.starter.db.interactor.IDBInteractor;
import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;

public class Utils {

  private final static IDBInteractor interactor;

  private final static long cacheTtl;

  static {
    String connectionString = HapiProperties.getTokenDataSourceUrl();
    String postgresUser = HapiProperties.getDataSourceUsername();
    String postgresPass = HapiProperties.getDataSourcePassword();
    interactor = new DBInteractorPostgres(connectionString, postgresUser, postgresPass);

    cacheTtl = HapiProperties.getCacheTtl(240000);
  }

  public static long getCacheTtl() {
    return cacheTtl;
  }

  public static TokenRecord getTokenRecord(String token) {
    return interactor.getTokenRecord(token);
  }

}