package ca.uhn.fhir.jpa.starter.Util;

import ca.uhn.fhir.jpa.starter.authorization.rules.*;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;

import java.util.ArrayList;
import java.util.List;

public class SecurityRulesUtil {
  public static List<RuleBase> rulesFactory(RequestDetails theRequestDetails) throws Exception {
    var rulesList = new ArrayList<RuleBase>();
    if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.TRANSACTION){
      var bundleRes = ((Bundle)theRequestDetails.getResource()).getEntry();
      for (var item : bundleRes)
      {
        var method = item.getRequest().getMethod();
        var operation = convertToRequestType(method);

        var resName = item.getResource().getResourceName();
        var rule = rulesFactory(resName);
        rule.setOperation(operation);
        rulesList.add(rule);
      }
    }
    else
    {
      String compartmentName = theRequestDetails.getRequestPath().split("/")[0];
      var operation = theRequestDetails.getRequestType();

      var rule = rulesFactory(compartmentName);
      rule.setOperation(operation);
      rulesList.add(rule);
    }

    return rulesList;
  }

  private static RuleBase rulesFactory(String compartmentName) throws Exception {
    RuleBase res;
    switch (compartmentName) {
      case "Flag":
        res = new FlagRules();
        break;
      case "Observation":
        res = new ObservationRules();
        break;
      case "CareTeam":
        res = new CareTeamRules();
        break;
      case "Patient":
        res = new PatientRules();
        break;
      case "Practitioner":
        res = new PractitionerRules();
        break;
      case "DeviceMetric":
        res = new DeviceMetricRules();
        break;
      case "Device":
        res = new DeviceRules();
        break;
      case "metadata":
        res = new MetadataRules();
        break;
      case "PractitionerRole":
        res = new PractitionerRoleRules();
        break;
      default:
        throw new Exception("Method does not exist");
    }

    return res;
  }

  private static RequestTypeEnum convertToRequestType(String method)
  {
    var methodNormalized = method.toUpperCase();
    switch (methodNormalized)
    {
      case "POST": return RequestTypeEnum.POST;
      case "PATCH": return RequestTypeEnum.PATCH;
      default:
        return RequestTypeEnum.GET;
    }
  }
}
