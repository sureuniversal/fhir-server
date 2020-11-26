package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.authorization.rules.RuleBase;
import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.jpa.starter.db.Utils;
import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.List;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

    if (theRequestDetails.getCompleteUrl().split("\\?")[0].contains(":8080")) {
      return new RuleBuilder()
        .allowAll("Port 8080")
        .build();
    }
    theRequestDetails.setRequestId();
    String authHeader = theRequestDetails.getHeader("Authorization");
    if (authHeader == null) {
      return new RuleBuilder()
        .denyAll("no authorization header")
        .build();
    }

    String token = authHeader.replace("Bearer ", "");

    TokenRecord tokenRecord = Utils.getTokenRecord(token);
    if (tokenRecord != null) {
      String bearerId = tokenRecord.getId();

      boolean isAdmin = false;
      boolean isPractitioner = tokenRecord.is_practitioner();

      if(isPractitioner){
        isAdmin = Search.isPractitionerAdmin(bearerId,authHeader);
      }

      RuleBase ruleBase = Utils.rulesFactory(theRequestDetails, authHeader,isAdmin);
      if (ruleBase == null) {
        return new RuleBuilder()
          .denyAll("access Denied")
          .build();
      }

      if (isPractitioner) {
        List<IIdType> careTeam = Search.getCareTeamPractitioner(bearerId,authHeader);
        ruleBase.addResourcesByPractitioner(bearerId);
        if(careTeam != null){
          ruleBase.addResourceIds(careTeam);
        }
      } else {
        List<IIdType> careTeam = Search.getCareTeamPatient(bearerId,authHeader);
        ruleBase.addResource(bearerId);
        if(careTeam != null){
          ruleBase.addCareTeam(careTeam);
        }
      }

      List<IAuthRule> rule;
      RequestTypeEnum operation = theRequestDetails.getRequestType();
      switch (operation) {
        case TRACE:
        case TRACK:
        case HEAD:
        case CONNECT:
        case OPTIONS:
        case GET:
          rule = ruleBase.handleGet();
          break;
        case PUT:
        case DELETE:
        case PATCH:
        case POST:
          rule = ruleBase.handlePost();
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
}
