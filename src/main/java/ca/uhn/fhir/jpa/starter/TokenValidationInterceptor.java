package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.AuthorizationRules.*;
import ca.uhn.fhir.jpa.starter.oauth.Utils;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.Document;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

    if(theRequestDetails.getCompleteUrl().split("\\?")[0].contains(":8080")) {
      return new RuleBuilder()
        .allowAll("Port 8080")
        .build();
    }

    String authHeader = theRequestDetails.getHeader("Authorization");
    if (authHeader == null){
      return new RuleBuilder()
        .denyAll("no authorization header")
        .build();
    }

    String token = authHeader.replace("Bearer ","");

    Document userDoc = Utils.GetUserByToken(token);

    if (userDoc != null) {
      String bearerId = userDoc.getString("_id");
      FhirContext ctx = theRequestDetails.getFhirContext();

      IGenericClient client = ctx.newRestfulGenericClient(theRequestDetails.getFhirServerBase());

      Boolean isPractitioner = userDoc.getBoolean("isPractitioner");
      if (isPractitioner == null) isPractitioner = false;
      List<IIdType> patients = isPractitioner ? getPatientsList(client, bearerId, authHeader) : new ArrayList<>();

      RuleBase ruleBase = GetRuleBuilder(theRequestDetails);
      if (ruleBase == null) {
        return new RuleBuilder()
          .denyAll("access Denied")
          .build();
      }

      if (isPractitioner) {
        ruleBase.addResourceIds(patients);
        ruleBase.addPractitioner(bearerId);
      } else {
        ruleBase.addResource(bearerId);
      }

      List<IAuthRule> rule;
      RequestTypeEnum operation = theRequestDetails.getRequestType();
      switch (operation){
        case TRACE:
        case TRACK:
        case HEAD:
        case CONNECT:
        case OPTIONS:
        case GET:
          rule = ruleBase.HandleGet();
          break;
        case PUT:
        case DELETE:
        case PATCH:
        case POST:
          rule = ruleBase.HandlePost();
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + operation);
      }

      return rule;

    } else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }

  private static RuleBase GetRuleBuilder(RequestDetails theRequestDetails)
  {
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
        return new PractitionerRules();
    }

    return null;
  }

  private static List<IIdType> getPatientsList(IGenericClient client,String practitioner,String authHeader) {
    List<IIdType> patients = new ArrayList<>();
    Bundle patientBundle = (Bundle) client.search().forResource(Patient.class)
      .where(new ReferenceClientParam("general-practitioner").hasId(practitioner))
      .withAdditionalHeader("Authorization", authHeader)
      .execute();
    for (Bundle.BundleEntryComponent item: patientBundle.getEntry()){
      patients.add(item.getResource().getIdElement().toUnqualifiedVersionless());
    }

    return patients;
  }

  public static IAuthRuleBuilder deviceMetricRules(IAuthRuleBuilder ruleBuilder,IGenericClient client,String patient,String authHeader) {
    Bundle deviceBundle = (Bundle)client.search().forResource(Device.class)
      .where(new ReferenceClientParam("patient").hasId(patient))
      .withAdditionalHeader("Authorization", authHeader)
      .prettyPrint()
      .execute();
    for (Bundle.BundleEntryComponent item2: deviceBundle.getEntry()){
      Resource resource2 = item2.getResource();
      IIdType userIdDeviceId = new IdType("Device", resource2.getIdElement().getIdPart());

      Bundle deviceMetricBundle = (Bundle) client.search().forResource(DeviceMetric.class)
        .where(new ReferenceClientParam("source").hasId(userIdDeviceId))
        .prettyPrint()
        .execute();
      for (Bundle.BundleEntryComponent item3: deviceMetricBundle.getEntry()){
        Resource resource3 = item3.getResource();
        IIdType userIdDeviceMetricId = new IdType("DeviceMetric", resource3.getIdElement().getIdPart());
        ruleBuilder
          .allow().read().resourcesOfType("DeviceMetric").inCompartment("DeviceMetric", userIdDeviceMetricId).andThen()
          .allow().write().resourcesOfType("DeviceMetric").inCompartment("DeviceMetric", userIdDeviceMetricId);
      }
      ruleBuilder
        .allow().read().resourcesOfType("Device").inCompartment("Device", userIdDeviceId).andThen()
        .allow().write().resourcesOfType("Device").inCompartment("Device", userIdDeviceId);
    }
    return ruleBuilder;
  }
}
