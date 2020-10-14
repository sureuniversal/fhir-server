package ca.uhn.fhir.jpa.starter.AuthorizationRules;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilderRuleOpClassifierFinished;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.dstu2.model.IdType;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.ArrayList;
import java.util.List;

public abstract class RuleBase {
  ArrayList<IdType> userIds = new ArrayList();
  String denyMessage;
  public RuleBase()
  {
  }

  public abstract List<IAuthRule> HandleGet();

  public abstract List<IAuthRule> HandlePost();

  public void addResource(String id)
  {
    var idType = this.ToIdType(id);
    userIds.add(idType);
  }

  public void addResourceIds(List<String> ids)
  {
    for (var id : ids)
    {
      var idType = this.ToIdType(id);
      userIds.add(idType);
    }
  }

  public List<IAuthRule> DenyRule()
  {
    return new RuleBuilder()
      .allow().patch().allRequests()
      .andThen()
      .denyAll(this.denyMessage)
      .build();
  }

  public List<IAuthRule> PatchRule() {
    return new RuleBuilder().allow().patch().allRequests().build();
  }

  private IdType ToIdType(String id)
  {
    return new IdType("Patient", id);
  }
}
