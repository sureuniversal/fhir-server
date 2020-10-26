package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.ArrayList;
import java.util.List;

public class GeneralRules extends RuleBase {
  public GeneralRules(String authHeader) {
    super(authHeader);
    denyMessage = "Only general rules allowed";
  }

  @Override
  public void addResourceIds(List<IIdType> ids) {
  }

  @Override
  public void addResourcesByPractitioner(String id) {
    addPractitioner(id);
  }

  @Override
  public void addResource(String id) {
  }

  @Override
  public List<IAuthRule> handleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> commonRules = commonRules();
    List<IAuthRule> denyRule = denyRule();
    if (practitionerId != null) {
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().read().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> commonRules = commonRules();
    List<IAuthRule> denyRule = denyRule();
    if (practitionerId != null) {
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().write().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

}
