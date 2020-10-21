package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.ArrayList;
import java.util.List;

public abstract class RuleBase {
  protected List<IIdType> userIds = new ArrayList<>();
  protected String denyMessage;
  protected IIdType practitionerId = null;
  public RuleBase()
  {
  }

  public static RuleBase rulesFactory(RequestDetails theRequestDetails){
    String compartmentName = theRequestDetails.getRequestPath().split("/")[0];
    switch (compartmentName)
    {
      case "Observation":
      case "Patient":
        return new PatientRules();
      case  "DeviceMetric":
        return new DeviceMetricRules(theRequestDetails.getFhirContext().newRestfulGenericClient(theRequestDetails.getFhirServerBase()));
      case  "Device":
        return new DeviceRules(theRequestDetails.getFhirContext().newRestfulGenericClient(theRequestDetails.getFhirServerBase()));
      case "metadata":
      case "PractitionerRole":
      case "Practitioner":
        return new GeneralRules();
      default:
        return null;
    }
  }

  public abstract List<IAuthRule> handleGet();

  public abstract List<IAuthRule> handlePost();

  public void addResource(String id)
  {
    userIds.add(toIdType(id,"Patient"));
  }

  public void addPractitioner(String id) {
    practitionerId = toIdType(id,"Practitioner");
  }
  public void addResourceIds(List<IIdType> ids)
  {
    userIds.addAll(ids);
  }

  public List<IAuthRule> commonRules()
  {
    return new RuleBuilder()
      .allow().metadata().andThen()
      .allow().patch().allRequests()
      .build();
  }

  public List<IAuthRule> denyRule()
  {
    return new RuleBuilder()
      .denyAll(denyMessage)
      .build();
  }

  private static IIdType toIdType(String id, String resourceType)
  {
    return new IdType(resourceType, id);
  }

}
