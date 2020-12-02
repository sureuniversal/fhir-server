package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.List;

public class CareTeamRules extends RuleBase{
  public CareTeamRules(String auth) {
    super(auth);
  }

  @Override
  public List<IAuthRule> handleGet() {
    return new RuleBuilder()
      .allowAll("")
      .build();
  }

  @Override
  public List<IAuthRule> handlePost() {
    return new RuleBuilder()
      .allowAll("")
      .build();
  }

  @Override
  public void addResource(String id) {

  }

  @Override
  public void addResourceIds(List<IIdType> ids) {

  }

  @Override
  public void addCareTeam(List<IIdType> ids) {

  }

  @Override
  public void addResourcesByPractitioner(String id) {

  }
}
