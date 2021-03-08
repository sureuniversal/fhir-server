package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Util.CareTeamSearch;
import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class RuleBase {
  protected String denyMessage;
  protected String userId;
  protected UserType userType;

  public RequestTypeEnum requestType;
  protected List<String> userIdsParamValue;
  private String[] userIdsParamName = new String[]{ "subject", "participant" };

  public Class<? extends IBaseResource> type;

  public RuleBase() {}

  public abstract List<IAuthRule> handleGet();

  public abstract List<IAuthRule> handlePost();

  protected List<IAuthRule> commonRulesGet() {
    return new RuleBuilder()
      .allow().metadata().andThen().allow().transaction().withAnyOperation().andApplyNormalRules().andThen()
      .allow().patch().allRequests()
      .build();
  }

  protected List<IAuthRule> commonRulesPost() {
    return new RuleBuilder()
      .allow().metadata().andThen()
      .allow().patch().allRequests().andThen()
      .allow().create().resourcesOfType(type).withAnyId()
      .build();
  }

  protected List<IAuthRule> denyRule() {
    return new RuleBuilder()
      .denyAll(denyMessage)
      .build();
  }

  protected static IIdType toIdType(String id, String resourceType) {
    return new IdType(resourceType, id);
  }

  public void setUserIdsRequested(RequestDetails theRequestDetails)
  {
    var params = theRequestDetails.getParameters();
    this.userIdsParamValue = new ArrayList<>();
    if (params != null && !params.isEmpty())
    {
      for(var name : this.userIdsParamName)
      {
        var value = params.get(name);
        if (value != null)
        {
          var arr = value[0].split(",");
          var valArr = Arrays.asList(arr);
          this.userIdsParamValue.addAll(valArr);
        }
      }
    }
    else
    {
      var id = theRequestDetails.getId();
      this.userIdsParamValue.add(id.getIdPart());
    }
  }

  public void setupUser(String userId, UserType userType)
  {
    this.userId = userId;
    this.userType = userType;
  }

  public void setOperation(RequestTypeEnum requestType)
  {
    this.requestType = requestType;
  }

  protected List<IIdType> handleCareTeam()
  {
    List<IIdType> userIds = new ArrayList<>();
    var allowedIds = CareTeamSearch.GetAllowedCareTeamsForUser(this.userId);
    var ids = new ArrayList<String>();
    for (var entry : allowedIds)
    {
      var id = entry.getIdPart();
      if (id != null) {
        ids.add(entry.getIdPart());
      }
    }

    if (!ids.isEmpty()) {
      var allowedToReadUsers = CareTeamSearch.getAllUsersInCareTeams(ids);
      userIds.addAll(allowedToReadUsers);
    }

    return userIds;
  }
}