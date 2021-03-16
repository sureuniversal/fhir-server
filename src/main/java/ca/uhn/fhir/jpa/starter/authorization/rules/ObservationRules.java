package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.Observation;

import java.util.ArrayList;
import java.util.List;

public class ObservationRules extends PatientRules {
  public ObservationRules() {
    this.denyMessage = "cant access Observation";
    this.type = Observation.class;
  }

  @Override
  public List<IAuthRule> handlePost() {
    String observationSubject = ((Observation)inResource).getSubject().getReferenceElement().getIdPart();
    if (userId.equalsIgnoreCase(observationSubject))
    {
      List<IAuthRule> observationRule = new RuleBuilder().allow().write().allResources().withAnyId().build();
      List<IAuthRule> commonRules = commonRulesGet();
      List<IAuthRule> denyRule = denyRule();

      List<IAuthRule> ruleList = new ArrayList<>();
      ruleList.addAll(observationRule);
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