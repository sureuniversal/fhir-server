package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.oauth.Utils;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.IdType;

import java.util.List;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
    String authHeader = theRequestDetails.getHeader("Authorization");
    if (authHeader == null){
      return new RuleBuilder()
        .denyAll("no header")
        .build();
    }

    String token = authHeader.replace("Bearer ","");
    var userId = Utils.getUserIdIdExists(token);
    if (userId != null) {
      var userIdPatient = new IdType("Patient", userId);
      var userIdPractitioner = new IdType("Practitioner", userId);
      var observationPatient = new IdType("Observation", userId);

      return
        new RuleBuilder()
          .allow().read().allResources().inCompartment("Patient", userIdPatient).andThen()
          .allow().write().allResources().inCompartment("Patient", userIdPatient).andThen()
          .allow().read().allResources().inCompartment("Practitioner", userIdPractitioner).andThen()
          .allow().write().allResources().inCompartment("Practitioner", userIdPractitioner).andThen()
          .allowAll()
          .build();
    }else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }
}
