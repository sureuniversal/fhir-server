package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;

import java.util.List;

public abstract class RuleBase {
  protected String denyMessage;
  protected IIdType practitionerId = null;
  protected String authHeader;
  protected Class<? extends IBaseResource> type = null;

  public RuleBase(String auth) {
    authHeader = auth;
  }


  public abstract List<IAuthRule> handleGet();

  public abstract List<IAuthRule> handlePost();

  public abstract void addResource(String id);

  public abstract void addResourceIds(List<IIdType> ids);

  public abstract void addResourcesByPractitioner(String id);

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
