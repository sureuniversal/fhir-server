package ca.uhn.fhir.jpa.starter.db;

import ca.uhn.fhir.jpa.starter.authorization.rules.*;
import ca.uhn.fhir.jpa.starter.db.interactor.DBInteractorMongo;
import ca.uhn.fhir.jpa.starter.db.interactor.DBInteractorPostgres;
import ca.uhn.fhir.jpa.starter.db.interactor.IDBInteractor;
import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;

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
      interactor = new DBInteractorPostgres(connectionString, postgresUser, postgresPass);
    }
  }

  public static TokenRecord getTokenRecord(String token) {
    return interactor.getTokenRecord(token);
  }

  public static RuleBase rulesFactory(RequestDetails theRequestDetails, String authHeader,boolean isAdmin) {
    if(isAdmin){
      return new AdminRules(authHeader);
    }
    String compartmentName = theRequestDetails.getRequestPath().split("/")[0];
    switch (compartmentName) {
      case "Observation":
        return new ObservationRules(authHeader);
      case "CareTeam":
      case "Patient":
      case "Practitioner":
        return new PatientRules(authHeader);
      case "DeviceMetric":
        if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.SEARCH_TYPE){
          return new AdminRules(authHeader);
        }
        return new DeviceMetricRules(authHeader);
      case "Device":
        if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.SEARCH_TYPE){
          return new AdminRules(authHeader);
        }
        return new DeviceRules(authHeader);
      case "metadata":
        return new MetadataRules(authHeader);
      case "PractitionerRole":
        return new PractitionerRoleRules(authHeader);
      default:
        return null;
    }
  }
}