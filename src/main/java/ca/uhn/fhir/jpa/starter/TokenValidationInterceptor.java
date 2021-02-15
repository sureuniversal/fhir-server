package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.authorization.rules.RuleImplPatient;
import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.jpa.starter.db.Utils;
import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;

import java.util.Arrays;
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

      boolean isPractitioner = tokenRecord.is_practitioner();

      if(isPractitioner && Search.isPractitionerAdmin(bearerId,authHeader)){
        return new RuleBuilder()
          .allowAll("Practitioner is admin")
          .build();
      }

      IIdType myId =  new IdType((isPractitioner)?"Practitioner":"Patient", bearerId);

      RuleImplPatient ruleImplPatient= new RuleImplPatient("",myId,isPractitioner);

      List<IAuthRule> rules = new RuleBuilder()
        .allow().metadata().andThen()
        .allow().transaction().withAnyOperation().andApplyNormalRules()
        .build();
      List<IAuthRule> afterRules;
      if (Arrays.stream(tokenRecord.getScopes()).noneMatch(s -> s.equalsIgnoreCase("w:resources:*"))) {
        List<IAuthRule> readOnlyRules = new RuleBuilder()
          .deny("read only").write().allResources().withAnyId().andThen()
          .deny("read only").delete().allResources().withAnyId()
          .build();
        rules.addAll(0,readOnlyRules);
      }
      rules.add(ruleImplPatient);
      afterRules = new RuleBuilder()
        .denyAll("Default")
        .build();
      rules.addAll(afterRules);

      return rules;

    } else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }
}
