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

    // Process authorization header - The following is a fake
    // implementation. Obviously we'd want something more real
    // for a production scenario.
    //
    // In this basic example we have two hardcoded bearer tokens,
    // one which is for a user that has access to one patient, and
    // another that has full access.
    IdType userIdPatientId = null;
    boolean userIsAdmin = false;
    Document tokenDoc = null;
    String authHeader = theRequestDetails.getHeader("Authorization");
    if (authHeader == null){
      throw new AuthenticationException("Missing or invalid Authorization header value");
    }
    String token;
    try {
      token = authHeader.split(" ")[1];
    } catch (Exception e){
      throw new AuthenticationException("Missing or invalid Authorization header value");
    }
    try {
      tokenDoc = Utils.AuthenticateToken(token);
    } catch (Utils.TokenNotFoundException e) {
      throw new AuthenticationException("Missing or invalid Authorization header value");
    } catch (Utils.TokenExpiredException ignored) {
      //throw new AuthenticationException("Token has expired");
    }
    userIsAdmin=true;

    // If the user is a specific patient, we create the following rule chain:
    // Allow the user to read anything in their own patient compartment
    // Allow the user to write anything in their own patient compartment
    // If a client request doesn't pass either of the above, deny it
    if (userIdPatientId != null) {
      return new RuleBuilder()
        .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
        .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen()
        .denyAll()
        .build();
    }

    // If the user is an admin, allow everything
    if (userIsAdmin) {
      return new RuleBuilder()
        .allowAll()
        .build();
    }

    // By default, deny everything. This should never get hit, but it's
    // good to be defensive
    return new RuleBuilder()
      .denyAll()
      .build();
  }
}
