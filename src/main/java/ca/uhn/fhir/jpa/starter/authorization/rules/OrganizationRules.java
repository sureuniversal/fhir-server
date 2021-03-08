package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.jpa.starter.Util.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;

import java.util.ArrayList;
import java.util.List;

public class OrganizationRules extends RuleBase{

  @Override
  public List<IAuthRule> handleGet() {
    var allowedOrgId = this.getAllowedOrganization();
    List<IAuthRule> OrganizationRule =
      new RuleBuilder().allow().read().allResources().inCompartment("Organization",  allowedOrgId).build();

    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> commonRules = commonRulesGet();
    List<IAuthRule> denyRule = denyRule();

    ruleList.addAll(OrganizationRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    List<IAuthRule> OrganizationRule =
      new RuleBuilder().allow().write().allResources().inCompartment("Organization",  RuleBase.toIdType(this.userId, "Organization")).build();

    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> commonRules = commonRulesPost();
    List<IAuthRule> denyRule = denyRule();

    ruleList.addAll(OrganizationRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  private IIdType getAllowedOrganization()
  {
    if (this.userType == UserType.patient)
    {
      return RuleBase.toIdType(this.userId, "Organization");
    }

    var org = Search.getPractitionerOrganization(this.userId);
    return org;
  }
}
