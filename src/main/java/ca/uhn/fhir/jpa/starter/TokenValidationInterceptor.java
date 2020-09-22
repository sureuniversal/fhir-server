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
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.bson.Document;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import java.util.ArrayList;
import java.util.List;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

    FhirContext ctx = theRequestDetails.getFhirContext();

    // code review: remove this check
    if(theRequestDetails.getCompleteUrl().split("\\?")[0].contains("fhir/metadata")){
      return new RuleBuilder()
        .allow().metadata()
        .build();
    }

    String authHeader = theRequestDetails.getHeader("Authorization");
    if (authHeader == null){
      return new RuleBuilder()
        .denyAll("no authorization header")
        .build();
    }

    ArrayList<Header> headers = new ArrayList<>();
    headers.add(new BasicHeader("Authorization", authHeader));

    // code review: remove this header
    headers.add(new BasicHeader("inProgress", authHeader));


    HttpClient httpClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    ctx.getRestfulClientFactory().setHttpClient(httpClient);

    // code review: we do not need to create a new client for each request try to create one and use it for all
    // all the internal requests can be done on the http port :8080 which is closed for the outside world (just open to the cloud)
    // and so we just check if the call is internal and we allow all
    IGenericClient client = ctx.newRestfulGenericClient(theRequestDetails.getFhirServerBase());

    String token = authHeader.replace("Bearer ","");

    Document userDoc = Utils.GetUserByToken(token);

    if (userDoc != null) {
      // code review: not all records have this field make sure to check the if null condition
      if(userDoc.getBoolean("isPractitioner"))
      {

        // code review: remove this check explanation above
        if(authHeader.equals(theRequestDetails.getHeader("inProgress"))){
          return new RuleBuilder()
            .allowAll("validation in progress")
            .build();
        }

        // code review: create a new method to get the patients
        RuleBuilder ruleBuilder = new RuleBuilder();
        Bundle patientBundle = (Bundle) client.search().forResource(Patient.class)
          .where(new ReferenceClientParam("general-practitioner")
          .hasId(userDoc.getString("_id")))
          .execute();
        for (Bundle.BundleEntryComponent item: patientBundle.getEntry()){
          Resource resource = item.getResource();
          IIdType userIdPatientId = new IdType("Patient", resource.getIdElement().getIdPart());
          ruleBuilder.allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
            .allow().write().allResources().inCompartment("Patient", userIdPatientId);
        }
        IIdType userIdPractitionerId = new IdType("Practitioner",userDoc.getString("_id"));
        ruleBuilder.allow().read().allResources().inCompartment("Practitioner", userIdPractitionerId).andThen()
          .allow().write().allResources().inCompartment("Practitioner", userIdPractitionerId);

        // code review: give a name to the rule
        return ruleBuilder.denyAll().build();
      } else {
        IIdType userIdPatientId = new IdType("Patient",userDoc.getString("_id"));

        return new RuleBuilder()
          .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
          .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen()
          // code review: give a name to the rule
          .denyAll()
          .build();
      }

    } else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }
}
