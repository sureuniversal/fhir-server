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
    var ruleList = new ArrayList<IAuthRule>();
    var patientRule = new RuleBuilder().allow().read().resourcesOfType("Patient").inCompartment("Patient", this.userIds).build();
    var denyRule = this.DenyRule();
    ruleList.addAll(patientRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> HandlePost() {
    var ruleList = new ArrayList<IAuthRule>();
    var patientRule = new RuleBuilder().allow().write().resourcesOfType("Patient").inCompartment("Patient", this.userIds).build();
    var patchRule = this.PatchRule();
    var denyRule = this.DenyRule();
    ruleList.addAll(patientRule);
    ruleList.addAll(patchRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
