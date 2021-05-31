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


  // sec rules updates
  // We need to also check if the request has an organization parameter ex: Patient?organization=48b07dfb-1b3d-4232-b74c-5efec04ee3d7
  // follow how <ref> idsParamValues </ref> is being built in RuleBase see <ref> setUserIdsRequested </ref>
  // then we should allow the request if it has that parameter as well
  @Override
  public List<IAuthRule> handleGet() {
    var userIds = this.setupAllowedUsersList();

    var existCounter = 0;
    for (var allowedId : this.idsParamValues) {
      if(userIds.contains(allowedId))
      {
        existCounter++;
      }
    }

    if (existCounter == this.idsParamValues.size())
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

  // sec rules updates
  // We need to check if the request body for creating a Patient has a ManagingOrganization in it
  // and that it equals the organization for the user sending the request same as userId for patient and
  // see <ref> Search.getPractitionerOrganization </ref> for Practitioner
  @Override
  public List<IAuthRule> handlePost() {
    return new RuleBuilder().allowAll().build();
  }

  // sec rules updates
  // We need to check if the patient being updated has a ManagingOrganization either in the request body or by performing a search for that field in the db
  // if it is not provided, and that it equals the organization for the user sending the request same as userId for patient and
  // see <ref> Search.getPractitionerOrganization </ref> for Practitioner
  public List<IAuthRule> handleUpdate()
  {
    return new RuleBuilder().allowAll().build();
  }

  private List<String> setupAllowedUsersList()
  {
    List<IIdType> userIds = new ArrayList<>();
    var careTeamUsers = handleCareTeam();
    var organizationUsers = Search.getAllInOrganization(getAllowedOrganization().getIdPart());

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
