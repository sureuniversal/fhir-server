package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.oauth.Utils;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.List;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TokenValidationInterceptor.class);

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

    if(theRequestDetails.getCompleteUrl().split("\\?")[0].contains("fhir/metadata")){
      return new RuleBuilder()
        .allowAll("metadata")
        .build();
    }

    String authHeader = theRequestDetails.getHeader("Authorization");
    if (authHeader == null){
      return new RuleBuilder()
        .denyAll("no header")
        .build();
    }
    String token = authHeader.replace("Bearer ","");

    if (Utils.isTokenValid(token)) {
      return new RuleBuilder()
        .allowAll("valid token")
        .build();
    }else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }
}
