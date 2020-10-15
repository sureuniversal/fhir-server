package ca.uhn.fhir.jpa.starter.AuthorizationRules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.ArrayList;
import java.util.List;

public class PatientRules extends RuleBase {
  {
    this.denyMessage = "Patient can only access himself";
  }

  @Override
  public List<IAuthRule> HandleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id :
      userIds) {
      ruleBuilder.allow().read().allResources().inCompartment("Patient",id);
    }
    List<IAuthRule> patientRule = ruleBuilder.build();
    List<IAuthRule> denyRule = DenyRule();
    if(practitionerId != null){
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().read().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }
    ruleList.addAll(patientRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> HandlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id :
      userIds) {
      ruleBuilder.allow().write().allResources().inCompartment("Patient",id);
    }
    List<IAuthRule> patientRule = ruleBuilder.build();
    List<IAuthRule> patchRule = PatchRule();
    List<IAuthRule> denyRule = DenyRule();
    if(practitionerId != null){
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().write().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }
    ruleList.addAll(patientRule);
    ruleList.addAll(patchRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
