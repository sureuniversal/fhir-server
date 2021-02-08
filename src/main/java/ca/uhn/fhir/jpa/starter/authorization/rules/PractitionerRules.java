package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.Practitioner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PractitionerRules extends RuleBase {
  public PractitionerRules() {
    this.type = Practitioner.class;
    this.denyMessage = "Practitioner Rule";
  }

  @Override
  public List<IAuthRule> handleGet() {
    List<IAuthRule> practitionerRule =
      new RuleBuilder().allow().read().allResources().inCompartment("Practitioner",  RuleBase.toIdType(this.userId, "Practitioner")).build();

    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> commonRules = commonRulesGet();
    List<IAuthRule> denyRule = denyRule();

    ruleList.addAll(practitionerRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    List<IAuthRule> practitionerRule =
      new RuleBuilder().allow().write().allResources().inCompartment("Practitioner",  RuleBase.toIdType(this.userId, "Practitioner")).build();

    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> commonRules = commonRulesPost();
    List<IAuthRule> denyRule = denyRule();

    ruleList.addAll(practitionerRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
