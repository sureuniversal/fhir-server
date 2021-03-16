package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.Flag;

import java.util.ArrayList;
import java.util.List;

public class FlagRules extends PatientRules {
  public FlagRules() {
    this.denyMessage = "cant access Flag";
    this.type = Flag.class;
  }

  @Override
  public List<IAuthRule> handlePost() {
    String flagSubject = ((Flag)inResource).getSubject().getReferenceElement().getIdPart();
    if (userId.equalsIgnoreCase(flagSubject))
    {
      List<IAuthRule> flagRule = new RuleBuilder().allow().write().allResources().withAnyId().build();
      List<IAuthRule> commonRules = commonRulesGet();
      List<IAuthRule> denyRule = denyRule();

      List<IAuthRule> ruleList = new ArrayList<>();
      ruleList.addAll(flagRule);
      ruleList.addAll(commonRules);
      ruleList.addAll(denyRule);

      return ruleList;
    } else {
      return denyRule();
    }
  }

  @Override
  public List<IAuthRule> handleUpdate(){
    return denyRule();
  }
}
