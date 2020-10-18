package ca.uhn.fhir.jpa.starter.AuthorizationRules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.ArrayList;
import java.util.List;

public class PractitionerRules extends RuleBase {

  {
    denyMessage = "Not a Practitioner or not authorized";
  }

  @Override
  public List<IAuthRule> HandleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> denyRule = DenyRule();
    if(practitionerId != null){
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().read().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> HandlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> patchRule = PatchRule();
    List<IAuthRule> denyRule = DenyRule();
    if(practitionerId != null){
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().write().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }
    ruleList.addAll(patchRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
