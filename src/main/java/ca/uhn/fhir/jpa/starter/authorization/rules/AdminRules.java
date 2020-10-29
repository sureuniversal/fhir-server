package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.List;

public class AdminRules extends RuleBase{
  public AdminRules(String auth) {
    super(auth);
  }

  public AdminRules(){
    super("");
  }

  @Override
  public List<IAuthRule> handleGet() {
    return new RuleBuilder()
      .allowAll("Practitioner is admin")
      .build();
  }

  @Override
  public List<IAuthRule> handlePost() {
    return new RuleBuilder()
      .allowAll("Practitioner is admin")
      .build();
  }

  @Override
  public void addResource(String id) {

  }

  @Override
  public void addResourceIds(List<IIdType> ids) {

  }

  @Override
  public void addResourcesByPractitioner(String id) {

  }
}
