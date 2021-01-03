package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;

import java.util.List;

public abstract class RuleBase {
  private static final long ttl = HapiProperties.getCacheTtl(240000);
  final long recordTtl;

  protected String denyMessage;
  protected IIdType practitionerId = null;
  protected String authHeader;
  protected String userId;
  protected Class<? extends IBaseResource> type = null;
  protected boolean built = false;

  public RuleBase(String auth) {
    authHeader = auth;
    this.recordTtl = System.currentTimeMillis() + ttl;
  }


  public abstract List<IAuthRule> handleGet();

  public abstract List<IAuthRule> handlePost();

  public abstract void addResource(String id);

  public abstract void addResourceIds(List<IIdType> ids);

  public abstract void addCareTeam(List<IIdType> ids);

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
      .allow().create().resourcesOfType(type).withAnyId()
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

  public void setUserId(String userId)
  {
    this.userId = userId;
  }

  public boolean isRecordExpired(){
    return ((recordTtl - System.currentTimeMillis()) < 0);
  }

  public boolean isBuilt(){
    return built;
  }

  public void built(){
    built = true;
  }
}
