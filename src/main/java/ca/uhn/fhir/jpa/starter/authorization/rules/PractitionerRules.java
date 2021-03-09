package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.jpa.starter.Util.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Practitioner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PractitionerRules extends RuleBase {
  public PractitionerRules() {
    this.type = Practitioner.class;
    this.denyMessage = "Practitioner Rule";
  }

  @Override
  public List<IAuthRule> handleGet() {
    var ids = this.setupAllowedIdList();

    var existCounter = 0;
    for (var allowedId : this.idsParamValues) {
      if(ids.contains(allowedId))
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

  @Override
  public List<IAuthRule> handlePost() {
    var ids = this.setupAllowedIdList();

    var existCounter = 0;
    for (var allowedId : this.idsParamValues) {
      if(ids.contains(allowedId))
      {
        existCounter++;
      }
    }

    if (existCounter == this.idsParamValues.size())
    {
      var allow = new RuleBuilder().allow().write().allResources().withAnyId();

      List<IAuthRule> patientRule = allow.build();
      List<IAuthRule> commonRules = commonRulesPost();
      List<IAuthRule> denyRule = denyRule();

      List<IAuthRule> ruleList = new ArrayList<>();
      ruleList.addAll(patientRule);
      ruleList.addAll(commonRules);
      ruleList.addAll(denyRule);

      return ruleList;
    }

    return denyRule();
  }

  private List<String> setupAllowedIdList()
  {
    List<IIdType> allowedIds = new ArrayList<>();
    var careTeamUsers = handleCareTeam();
    allowedIds.addAll(careTeamUsers);

    String orgId = null;
    if (this.userType == UserType.patient)
    {
      orgId = this.userId;
    }
    else
    {
      var id = Search.getPractitionerOrganization(this.userId);
      if (id != null) {
        orgId = id.getIdPart();
      }
    }

    if (orgId != null)
    {
      var organizationUsers = Search.getAllInOrganization(orgId);
      allowedIds.addAll(organizationUsers);
      allowedIds.add(this.toIdType(orgId, "Organization"));
    }

    var idsList = allowedIds.stream().map(e -> e.getIdPart()).collect(Collectors.toList());
    return idsList;
  }
}
