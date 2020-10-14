package ca.uhn.fhir.jpa.starter.AuthorizationRules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.ArrayList;
import java.util.List;

public abstract class RuleBase {
  protected List<IIdType> userIds = new ArrayList<>();
  protected String denyMessage;
  public RuleBase()
  {
  }

  public abstract List<IAuthRule> HandleGet();

  public abstract List<IAuthRule> HandlePost();

  public void addResource(String id)
  {
    userIds.add(ToIdType(id));
  }

  public void addResourceIds(List<String> ids)
  {
    for (var id : ids)
    {
      userIds.add(ToIdType(id));
    }
  }

  public List<IAuthRule> DenyRule()
  {
    return new RuleBuilder()
      .allow().metadata().andThen()
      .allow().patch().allRequests().andThen()
      .denyAll(denyMessage)
      .build();
  }

  public static List<IAuthRule> PatchRule() {
    return new RuleBuilder().allow().patch().allRequests().build();
  }

  private static IIdType ToIdType(String id)
  {
    return new IdType("Patient", id);
  }
}
