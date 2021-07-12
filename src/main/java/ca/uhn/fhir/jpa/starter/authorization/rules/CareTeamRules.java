package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.jpa.starter.Util.CareTeamSearch;
import ca.uhn.fhir.jpa.starter.Util.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CareTeam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CareTeamRules extends PatientRules{
  public CareTeamRules() {
    this.type = CareTeam.class;
  }

  @Override
  public List<IAuthRule> handleGet() {
    var operationAllowed = super.isOperationAllowed();
    var allowedCareTeamList = this.setupAllowedCareTeamList().stream().map(e -> e.getIdPart()).collect(Collectors.toList());

    var existCounter = 0;
    for (var allowedId : this.idsParamValues) {
      if(allowedCareTeamList.contains(allowedId))
      {
        existCounter++;
      }

      if(this.userId.compareTo(allowedId) == 0)
      {
        existCounter++;
      }
    }

    if (existCounter >= this.idsParamValues.size() || operationAllowed)
    {
      return new RuleBuilder().allowAll().build();
    }

    return denyRule();
  }

  // Can create a careTeam if the caller is a patient and is the userId or if it is the organization admin for the subject
  @Override
  public List<IAuthRule> handlePost() {
    return new RuleBuilder().allowAll().build();
  }

  private List<IIdType> setupAllowedCareTeamList()
  {
    if (this.userType == UserType.organizationAdmin)
    {
      List<IIdType> careTeamList = new ArrayList<>();
      var organizationUsers = Search.getAllPatientsInOrganization(getAllowedOrganization().getIdPart());
      for (var user : organizationUsers)
      {
        var allowed = CareTeamSearch.getAllowedCareTeamAsSubject(user.getIdPart());
        careTeamList.addAll(allowed);
      }

      return careTeamList;
    }
    else if (this.userType == UserType.patient)
    {
      return CareTeamSearch.getAllowedCareTeamAsSubject(this.userId);
    }
    else
    {
      return CareTeamSearch.getAllowedCareTeamAsParticipant(this.userId);
    }
  }
}
