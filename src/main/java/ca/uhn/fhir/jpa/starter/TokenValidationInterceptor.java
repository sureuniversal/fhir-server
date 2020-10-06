package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.oauth.Utils;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.Document;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

      FhirContext ctx = theRequestDetails.getFhirContext();

      //debug
      HttpClient httpClient = HttpClientBuilder.create().setConnectionTimeToLive(240, TimeUnit.SECONDS).build();
      ctx.getRestfulClientFactory().setHttpClient(httpClient);

      IGenericClient client = ctx.newRestfulGenericClient(theRequestDetails.getFhirServerBase());

      Boolean isPractitioner = userDoc.getBoolean("isPractitioner");
      if(isPractitioner != null && isPractitioner)
      {
        return practitionerRules(new RuleBuilder(),client,userDoc.getString("_id"),authHeader)
          .allow().metadata().andThen()
          .allow().patch().allRequests().andThen()
          .denyAll("Practitioner can only access associated patients").build();
      } else {
        return patientRules(new RuleBuilder(),client,userDoc.getString("_id"),authHeader)
          .allow().metadata().andThen()
          .allow().patch().allRequests().andThen()
          .denyAll("Patient can only access himself")
          .build();
      }

    } else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }
  private static RuleBuilder practitionerRules(RuleBuilder ruleBuilder,IGenericClient client,String practitioner,String authHeader){
    IIdType userIdPractitionerId = new IdType("Practitioner",practitioner);

    Bundle patientBundle = (Bundle) client.search().forResource(Patient.class)
      .where(new ReferenceClientParam("general-practitioner").hasId(practitioner))
      .withAdditionalHeader("Authorization", authHeader)
      .execute();

    for (Bundle.BundleEntryComponent item: patientBundle.getEntry()){
      Resource resource = item.getResource();
      patientRules(ruleBuilder,client,resource.getIdElement().getIdPart(),authHeader);
    }
    ruleBuilder
      .allow().read().allResources().inCompartment("Practitioner", userIdPractitionerId).andThen()
      .allow().write().allResources().inCompartment("Practitioner", userIdPractitionerId);
    return ruleBuilder;
  }
  private static RuleBuilder patientRules(RuleBuilder ruleBuilder,IGenericClient client,String patient,String authHeader){
    IIdType userIdPatientId = new IdType("Patient", patient);

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
    ruleBuilder
      .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
      .allow().write().allResources().inCompartment("Patient", userIdPatientId);
    return ruleBuilder;
  }
}
