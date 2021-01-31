package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRules extends RuleBase {
  public PatientRules() {
    this.denyMessage = "Patient Rule";
    this.type = Patient.class;
  }

  @Override
  public List<IAuthRule> handleGet() {
    var userIds = this.setupAllowedUsersList();
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : userIds) {
      if (id.getResourceType().compareTo("Patient") == 0)
      {
        ruleBuilder.allow().read().allResources().inCompartment("Patient", id);
      }
      else
      {
        ruleBuilder.allow().read().allResources().inCompartment("Practitioner", id);
      }
    }

    if (this.userType == UserType.practitioner) {
      List<IAuthRule> practitionerRule =
        new RuleBuilder().allow().read().allResources().inCompartment("Practitioner", RuleBase.toIdType(this.userId, "Practitioner")).build();

      ruleList.addAll(practitionerRule);
    }

    List<IAuthRule> patientRule = ruleBuilder.build();
    List<IAuthRule> commonRules = commonRulesGet();
    List<IAuthRule> denyRule = denyRule();

    ruleList.addAll(patientRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    var userIds = this.setupAllowedUsersList();
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : userIds) {
      if (id.getResourceType().compareTo("Patient") == 0)
      {
        ruleBuilder.allow().write().allResources().inCompartment("Patient", id);
      }
      else
      {
        ruleBuilder.allow().write().allResources().inCompartment("Practitioner", id);
      }
    }

    if (this.userType == UserType.practitioner) {
      List<IAuthRule> practitionerRule =
        new RuleBuilder().allow().write().allResources().inCompartment("Practitioner", RuleBase.toIdType(this.userId, "Practitioner")).build();

      ruleList.addAll(practitionerRule);
    }

    List<IAuthRule> patientRule = ruleBuilder.build();
    List<IAuthRule> commonRules = commonRulesPost();
    List<IAuthRule> denyRule = denyRule();

    ruleList.addAll(patientRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  private List<IIdType> setupAllowedUsersList()
  {
    List<IIdType> userIds = new ArrayList<>();
    var careTeamUsers = handleCareTeam();
    userIds.addAll(careTeamUsers);

    if (this.userType == UserType.patient)
    {
      userIds.add(RuleBase.toIdType(this.userId, "Patient"));
    }

    return userIds;
  }
}
