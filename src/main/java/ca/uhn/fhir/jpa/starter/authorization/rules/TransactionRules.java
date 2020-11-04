package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionRules extends RuleBase {
  List<RuleBase> rules;

  public TransactionRules(String auth, List<String> ruleTypes) {
    super(auth);
    List<RuleBase> ruleList = new ArrayList<>();
    for (String type :
      ruleTypes) {
      switch (type) {
        case "Observation":
        case "Patient":
          ruleList.add(new PatientRules(authHeader));
          break;
        case "DeviceMetric":
          ruleList.add(new DeviceMetricRules(authHeader));
          break;
        case "Device":
          ruleList.add(new DeviceRules(authHeader));
          break;
        case "metadata":
        case "PractitionerRole":
        case "Practitioner":
          ruleList.add(new GeneralRules(authHeader));
          break;
      }
    }
    rules = new ArrayList<>(ruleList.stream().collect(Collectors.toMap(Object::getClass, p -> p, (p, q) -> p)).values());//unique by class
    type = Bundle.class;
    denyMessage = "Transaction not permitted";
  }

  @Override
  public List<IAuthRule> specificRulesGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    for (RuleBase rule : rules) {
      ruleList.addAll(rule.specificRulesGet());
    }
    return ruleList;
  }

  @Override
  public List<IAuthRule> specificRulesPost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    for (RuleBase rule : rules) {
      ruleList.addAll(rule.specificRulesPost());
      ruleList.addAll(rule.createRules());
    }
    return ruleList;
  }

  @Override
  public void addResource(String id) {
    for (RuleBase rule : rules) {
      rule.addResource(id);
    }
  }

  @Override
  public void addResourceIds(List<IIdType> ids) {
    for (RuleBase rule : rules) {
      rule.addResourceIds(ids);
    }
  }

  @Override
  public void addResourcesByPractitioner(String id) {
    addPractitioner(id);
    for (RuleBase rule : rules) {
      rule.addResourcesByPractitioner(id);
    }
  }
}
