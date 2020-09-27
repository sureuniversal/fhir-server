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
    return new RuleBuilder()
        .allowAll("Port 8080")
        .build();
//     if(theRequestDetails.getCompleteUrl().split("\\?")[0].contains(":8080")) {
//       return new RuleBuilder()
//         .allowAll("Port 8080")
//         .build();
//     }

//     String authHeader = theRequestDetails.getHeader("Authorization");
//     if (authHeader == null){
//       return new RuleBuilder()
//         .denyAll("no authorization header")
//         .build();
//     }

//     String token = authHeader.replace("Bearer ","");

//     Document userDoc = Utils.GetUserByToken(token);

//     if (userDoc != null) {
//       Boolean isPractitioner = userDoc.getBoolean("isPractitioner");
//       if(isPractitioner != null && isPractitioner)
//       {
//         FhirContext ctx = theRequestDetails.getFhirContext();

//         ArrayList<Header> headers = new ArrayList<>();
//         headers.add(new BasicHeader("Authorization", authHeader));

//         HttpClient httpClient = HttpClientBuilder.create().setDefaultHeaders(headers).build();
//         ctx.getRestfulClientFactory().setHttpClient(httpClient);
//         IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8080/hapi-fhir-jpaserver/fhir/");

//         RuleBuilder ruleBuilder = new RuleBuilder();
//         Bundle patientBundle = getPractitionerPatients(client,userDoc.getString("_id"));

//         for (Bundle.BundleEntryComponent item: patientBundle.getEntry()){
//           Resource resource = item.getResource();

//           IIdType userIdPatientId = new IdType("Patient", resource.getIdElement().getIdPart());
//           ruleBuilder
//             .allow().metadata().andThen()
//             .allow().patch().allRequests().andThen()
//             .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
//             .allow().write().allResources().inCompartment("Patient", userIdPatientId);
//         }

//         IIdType userIdPractitionerId = new IdType("Practitioner",userDoc.getString("_id"));
//         ruleBuilder
//           .allow().metadata().andThen()
//           .allow().patch().allRequests().andThen()
//           .allow().read().allResources().inCompartment("Practitioner", userIdPractitionerId).andThen()
//           .allow().write().allResources().inCompartment("Practitioner", userIdPractitionerId);

//         return ruleBuilder.denyAll("Practitioner can only access associated patients").build();
//       } else {
//         IIdType userIdPatientId = new IdType("Patient",userDoc.getString("_id"));

//         return new RuleBuilder()
//           .allow().metadata().andThen()
//           .allow().patch().allRequests().andThen()
//           .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
//           .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen()
//           .denyAll("Patient can only access himself")
//           .build();
//       }

//     } else {
//       return new RuleBuilder()
//         .denyAll("invalid token")
//         .build();
//     }
  }

  Bundle getPractitionerPatients(IGenericClient client, String practitionerId) {
    return (Bundle) client.search().forResource(Patient.class)
      .where(new ReferenceClientParam("general-practitioner")
        .hasId(practitionerId))
      .execute();
  }
}
