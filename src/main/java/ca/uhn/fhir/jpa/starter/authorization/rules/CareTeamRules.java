package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Util.CareTeamSearch;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.CareTeam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

  public class CareTeamRules extends PatientRules{
  public CareTeamRules() {
    this.type = CareTeam.class;
  }

  @Override
  public List<IAuthRule> handleGet() {
    var allowedIds = CareTeamSearch.GetAllowedCareTeamsForUser(this.userId);

    RuleBuilder ruleBuilder = new RuleBuilder();
    ruleBuilder.allow().read().allResources().inCompartment("CareTeam", allowedIds);

    List<IAuthRule> careTeamRules = ruleBuilder.build();
    List<IAuthRule> patientRules = super.handleGet();
    List<IAuthRule> commonRules = commonRulesGet();
    List<IAuthRule> denyRule = denyRule();

    List<IAuthRule> ruleList = new ArrayList<>();
    ruleList.addAll(careTeamRules);
    ruleList.addAll(patientRules);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    var allowedIds = CareTeamSearch.GetAllowedCareTeamsForUser(this.userId);

    RuleBuilder ruleBuilder = new RuleBuilder();
    ruleBuilder.allow().write().allResources().inCompartment("CareTeam", allowedIds);

    List<IAuthRule> careTeamRules = ruleBuilder.build();
    List<IAuthRule> patientRules = super.handlePost();
    List<IAuthRule> commonRules = commonRulesPost();
    List<IAuthRule> denyRule = denyRule();

    List<IAuthRule> ruleList = new ArrayList<>();
    ruleList.addAll(careTeamRules);
    ruleList.addAll(patientRules);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
