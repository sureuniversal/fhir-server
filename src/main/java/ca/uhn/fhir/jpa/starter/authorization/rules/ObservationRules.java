package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.ArrayList;
import java.util.List;

public class ObservationRules extends RuleBase {
  public ObservationRules() {
    this.denyMessage = "cant access Observation";
  }

  @Override
  public List<IAuthRule> handleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> patientRule = new RuleBuilder().allow().read().allResources().inCompartment("Patient", this.userIds).build();
    List<IAuthRule> commonRules = commonRules();
    List<IAuthRule> denyRule = denyRule();
    ruleList.addAll(patientRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> patientRule = new RuleBuilder().allow().write().allResources().inCompartment("Patient", this.userIds).build();
    List<IAuthRule> commonRules = commonRules();
    List<IAuthRule> denyRule = denyRule();
    ruleList.addAll(patientRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
