package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.oauth.Utils;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.bson.Document;
import org.hl7.fhir.r4.model.IdType;

import java.util.List;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TokenValidationInterceptor.class);

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

    String authHeader = theRequestDetails.getHeader("Authorization");
    
    // If the Authorization header is missing just deny entrance try not to use exceptions
    // new RuleBuilder()
    // .denyAll()
    // .build();
    if (authHeader == null){
      throw new AuthenticationException("Missing or invalid Authorization header value");
    }
    
    // Get rid of the exception and replace it with a simpler if statement
    String token;
    try {
      token = authHeader.split(" ")[1];
    } catch (Exception e){
      throw new AuthenticationException("Missing or invalid Authorization header value");
    }

    // If the user is an admin, allow everything
    if (Utils.isTokenValid(token)) {
      return new RuleBuilder()
        .allowAll()
        .build();
    }else {
       // Remove the exception and use
      // new RuleBuilder()
      // .denyAll()
      // .build();
      throw new AuthenticationException("Missing or invalid Authorization header value");
    }
  }
}
