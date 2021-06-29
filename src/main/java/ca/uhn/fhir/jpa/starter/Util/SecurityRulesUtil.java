package ca.uhn.fhir.jpa.starter.Util;

import ca.uhn.fhir.jpa.starter.authorization.rules.*;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;

import java.util.ArrayList;
import java.util.List;

public class SecurityRulesUtil {
  public static List<RuleBase> rulesFactory(RequestDetails theRequestDetails) throws Exception {
    List<RuleBase> rulesList = new ArrayList<>();
    if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.TRANSACTION){
      var bundleRes = ((Bundle)theRequestDetails.getResource()).getEntry();
      for (var item : bundleRes)
      {
        String method = item.getRequest().getMethod().getDisplay();
        RequestTypeEnum operation = convertToRequestType(method);

        String resName = item.getRequest().getUrl().split("[/\\?]")[0];
        RuleBase rule = rulesFactory(resName);
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
    switch (compartmentName) {
      case "Flag":  return new FlagRules();
      case "Observation":return new ObservationRules();
      case "CareTeam": return new CareTeamRules();
      case "Patient": return new PatientRules();
      case "Practitioner": return new PractitionerRules();
      case "DeviceMetric": return new DeviceMetricRules();
      case "Device": return new DeviceRules();
      case "metadata": return new MetadataRules();
      case "PractitionerRole": return new PractitionerRoleRules();
      case "Organization": return new OrganizationRules();
      case "DiagnosticReport": return new DiagnosticReportRules();
      case "Media": return new MediaRule();
      default:
        throw new Exception("Method does not exist");
    }
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
