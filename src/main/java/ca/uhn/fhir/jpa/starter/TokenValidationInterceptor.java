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
    headers.add(new BasicHeader("inProgress", authHeader));
    HttpClient httpClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
    ctx.getRestfulClientFactory().setHttpClient(httpClient);

    IGenericClient client = ctx.newRestfulGenericClient(theRequestDetails.getFhirServerBase());

    String token = authHeader.replace("Bearer ","");

    Document userDoc = Utils.GetUserByToken(token);

    if (userDoc != null) {
      if(userDoc.getBoolean("isPractitioner"))
      {
        if(authHeader.equals(theRequestDetails.getHeader("inProgress"))){
          return new RuleBuilder()
            .allowAll("validation in progress")
            .build();
        }
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
        return ruleBuilder.denyAll().build();
      } else {
        IIdType userIdPatientId = new IdType("Patient",userDoc.getString("_id"));

        return new RuleBuilder()
          .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
          .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen()
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
