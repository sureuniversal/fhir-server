package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;

import java.util.ArrayList;
import java.util.List;

public abstract class RuleBase {
  protected String denyMessage;
  protected IIdType practitionerId = null;
  protected String authHeader;
  protected Class<? extends IBaseResource> type = null;

  public RuleBase(String auth) {
    authHeader = auth;
  }


  public List<IAuthRule> handleGet(){
    List<IAuthRule> ruleList = new ArrayList<>();

    List<IAuthRule> rules = specificRulesGet();
    List<IAuthRule> commonRules = commonRulesGet();
    List<IAuthRule> denyRule = denyRule();

    if (practitionerId != null) {
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().read().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }

    ruleList.addAll(rules);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  public List<IAuthRule> handlePost(){
    List<IAuthRule> ruleList = new ArrayList<>();

    List<IAuthRule> rules = specificRulesPost();
    List<IAuthRule> commonRules = commonRulesPost();
    List<IAuthRule> denyRule = denyRule();

    if (practitionerId != null) {
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().write().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }

    ruleList.addAll(rules);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  public abstract void addResource(String id);

  public abstract void addResourceIds(List<IIdType> ids);

  public abstract void addResourcesByPractitioner(String id);

  public abstract List<IAuthRule> specificRulesGet();

  public abstract List<IAuthRule> specificRulesPost();

  public void addPractitioner(String id) {
    practitionerId = toIdType(id, "Practitioner");
  }

  public List<IAuthRule> commonRulesGet() {
    return new RuleBuilder()
      .allow().metadata().andThen()
      .allow().patch().allRequests()
      .build();
  }
  public List<IAuthRule> commonRulesPost() {
    return new RuleBuilder()
      .allow().metadata().andThen()
      .allow().patch().allRequests().andThen()
      .allow().create().resourcesOfType(type).withAnyId().andThen()
      .allow().transaction().withAnyOperation().andApplyNormalRules()
      .build();
  }

  public List<IAuthRule> denyRule() {
    return new RuleBuilder()
      .denyAll(denyMessage)
      .build();
  }

  public static IIdType toIdType(String id, String resourceType) {
    return new IdType(resourceType, id);
  }

}
