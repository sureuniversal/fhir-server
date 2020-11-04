package ca.uhn.fhir.jpa.starter.db;

import ca.uhn.fhir.jpa.starter.authorization.rules.*;
import ca.uhn.fhir.jpa.starter.db.interactor.DBInteractorMongo;
import ca.uhn.fhir.jpa.starter.db.interactor.DBInteractorPostgres;
import ca.uhn.fhir.jpa.starter.db.interactor.IDBInteractor;
import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.r4.model.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.TRANSACTION){
        List<String> types = Search.getBundleTypes(theRequestDetails).stream().distinct().collect(Collectors.toList());
        switch (types.size()){
          case 0:
            return null;
          case 1:
            compartmentName = types.get(0);
            break;
          default:
            return new TransactionRules(authHeader,types);
        }
    }
    switch (compartmentName) {
      case "Observation":
        return new ObservationRules(authHeader);
      case "Patient":
        return new PatientRules(authHeader);
      case "DeviceMetric":
        return new DeviceMetricRules(authHeader);
      case "Device":
        return new DeviceRules(authHeader);
      case "metadata":
        return new MetadataRules(authHeader);
      case "PractitionerRole":
        return new PractitionerRoleRules(authHeader);
      case "Practitioner":
        return new PractitionerRules(authHeader);
      default:
        return null;
    }
  }
}