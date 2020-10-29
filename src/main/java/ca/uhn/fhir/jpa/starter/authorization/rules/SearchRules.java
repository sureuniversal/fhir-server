package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.List;
import java.util.Map;

public class SearchRules extends RuleBase{
  Map<String,String []> parameters;

  public SearchRules(String auth,Map<String,String []> param) {
    super(auth);
    denyMessage = "Search";
    parameters = param;
  }

  @Override
  public List<IAuthRule> handleGet() {
    return new RuleBuilder()
      .allowAll("Search")
      .build();
  }

  @Override
  public List<IAuthRule> handlePost() {
    return new RuleBuilder()
      .allowAll("Search")
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
