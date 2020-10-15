package ca.uhn.fhir.jpa.starter.AuthorizationRules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.ArrayList;
import java.util.List;

public class PatientRule extends RuleBase {
  {
    this.denyMessage = "Patient can only access himself";
  }

  @Override
  public List<IAuthRule> HandleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> patientRule = new RuleBuilder().allow().read().allResources().inCompartment("Patient", this.userIds).build();
    List<IAuthRule> denyRule = DenyRule();
    ruleList.addAll(patientRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> HandlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> patientRule = new RuleBuilder().allow().write().allResources().inCompartment("Patient", this.userIds).build();
    List<IAuthRule> patchRule = PatchRule();
    List<IAuthRule> denyRule = DenyRule();
    ruleList.addAll(patientRule);
    ruleList.addAll(patchRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
