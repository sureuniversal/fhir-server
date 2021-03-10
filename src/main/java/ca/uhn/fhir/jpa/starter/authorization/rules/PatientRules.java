package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.jpa.starter.Util.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PatientRules extends RuleBase {
  public PatientRules() {
    this.denyMessage = "Patient Rule";
    this.type = Patient.class;
  }

  @Override
  public List<IAuthRule> handleGet() {
    var userIds = this.setupAllowedUsersList();

    var allExits = true;
    for (var allowedId : this.idsParamValues) {
      allExits = allExits & userIds.contains(allowedId);
    }

    if (allExits)
    {
      var allow = new RuleBuilder().allow().read().allResources().withAnyId();

      List<IAuthRule> patientRule = allow.build();
      List<IAuthRule> commonRules = commonRulesGet();
      List<IAuthRule> denyRule = denyRule();

      List<IAuthRule> ruleList = new ArrayList<>();
      ruleList.addAll(patientRule);
      ruleList.addAll(commonRules);
      ruleList.addAll(denyRule);

      return ruleList;
    }

    return denyRule();
  }

  @Override
  public List<IAuthRule> handlePost() {
    return new RuleBuilder().allowAll().build();
  }

  public List<IAuthRule> handleUpdate()
  {
    return new RuleBuilder().allowAll().build();
//    var userIds = this.setupAllowedUsersList();
//
//    var allExits = true;
//    for (var allowedId : this.idsParamValues) {
//      allExits = allExits & userIds.contains(allowedId);
//    }
//
//    if (allExits)
//    {
//      var allow = new RuleBuilder().allow().write().allResources().withAnyId();
//
//      List<IAuthRule> patientRule = allow.build();
//      List<IAuthRule> commonRules = commonRulesPost();
//      List<IAuthRule> denyRule = denyRule();
//
//      List<IAuthRule> ruleList = new ArrayList<>();
//      ruleList.addAll(patientRule);
//      ruleList.addAll(commonRules);
//      ruleList.addAll(denyRule);
//
//      return ruleList;
//    }
//
//    return denyRule();
  }

  private List<String> setupAllowedUsersList()
  {
    List<IIdType> userIds = new ArrayList<>();
    var careTeamUsers = handleCareTeam();
    var organizationUsers = Search.getAllInOrganization(this.userId);

    userIds.addAll(careTeamUsers);
    userIds.addAll(organizationUsers);

    if (this.userType == UserType.patient)
    {
      userIds.add(RuleBase.toIdType(this.userId, "Patient"));
    }

    var idsList = userIds.stream().map(e -> e.getIdPart()).collect(Collectors.toList());
    return idsList;
  }
}
