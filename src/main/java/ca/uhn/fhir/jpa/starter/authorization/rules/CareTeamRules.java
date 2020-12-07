package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.dstu2.model.IdType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Device;

import java.util.ArrayList;
import java.util.List;

public class CareTeamRules extends RuleBase{
  public CareTeamRules(String auth) {
    super(auth);
  }

  @Override
  public List<IAuthRule> handleGet() {
    var pId = new IdType("Patient", this.userId);
    var allow = new RuleBuilder().allow().read().allResources().inCompartment("Patient", pId).build();

    List<IAuthRule> commonRules = commonRulesGet();
    List<IAuthRule> denyRule = denyRule();

    List<IAuthRule> ruleList = new ArrayList<>();
    ruleList.addAll(allow);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    this.type = CareTeam.class;
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
