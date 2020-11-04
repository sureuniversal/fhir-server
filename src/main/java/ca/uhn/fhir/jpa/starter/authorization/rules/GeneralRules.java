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
  public List<IAuthRule> specificRulesGet() {
    return new ArrayList<>();
  }

  @Override
  public List<IAuthRule> specificRulesPost() {
    return new ArrayList<>();
  }

}
