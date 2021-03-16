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
    List<String> userIds = this.setupAllowedUsersList();

    boolean allExits = true;
    for (var allowedId : this.idsParamValues) {
      allExits = allExits && userIds.contains(allowedId);
    }

    if (allExits && this.idsParamValues.size() != 0)
    {

      List<IAuthRule> patientRule = new RuleBuilder().allow().read().allResources().withAnyId().build();
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
    IIdType inOrganization = ((Patient)inResource).getManagingOrganization().getReferenceElement().toUnqualifiedVersionless();
    if (userOrganization == inOrganization)
    {
      List<IAuthRule> patientRule = new RuleBuilder().allow().write().allResources().withAnyId().build();
      List<IAuthRule> commonRules = commonRulesGet();
      List<IAuthRule> denyRule = denyRule();

      List<IAuthRule> ruleList = new ArrayList<>();
      ruleList.addAll(patientRule);
      ruleList.addAll(commonRules);
      ruleList.addAll(denyRule);

      return ruleList;
    } else {
      return denyRule();
    }
  }

  public List<IAuthRule> handleUpdate()
  {
    IIdType inOrganization = nullId;
    try {
      inOrganization = ((Patient)inResource).getManagingOrganization().getReferenceElement().toUnqualifiedVersionless();
    } catch (Exception ignored) { }

    if(inOrganization.equals(nullId)){
      inOrganization = Search.getPatientOrganization(inResource.getIdElement().getIdPart());
    }

    if (userOrganization.equals(inOrganization))
    {
      List<IAuthRule> patientRule = new RuleBuilder().allow().write().allResources().withAnyId().build();
      List<IAuthRule> commonRules = commonRulesGet();
      List<IAuthRule> denyRule = denyRule();

      List<IAuthRule> ruleList = new ArrayList<>();
      ruleList.addAll(patientRule);
      ruleList.addAll(commonRules);
      ruleList.addAll(denyRule);

      return ruleList;
    } else {
      return denyRule();
    }
  }

  private List<String> setupAllowedUsersList()
  {
    List<IIdType> userIds = new ArrayList<>();
    List<IIdType> careTeamUsers = handleCareTeam();
    List<IIdType> organizationUsers = Search.getAllInOrganization(this.userId);

    userIds.addAll(careTeamUsers);
    userIds.addAll(organizationUsers);

    if (this.userType == UserType.patient)
    {
      userIds.add(RuleBase.toIdType(this.userId, "Patient"));
    }

    return userIds.stream().map(IIdType::getIdPart).collect(Collectors.toList());
  }
}
