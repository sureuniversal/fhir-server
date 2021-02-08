package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Util.CareTeamSearch;
import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;


import java.util.ArrayList;
import java.util.List;

public abstract class RuleBase {
  protected String denyMessage;
  protected String userId;
  protected UserType userType;

  public RequestTypeEnum requestType;

  public Class<? extends IBaseResource> type;

  public RuleBase() {}

  public abstract List<IAuthRule> handleGet();

  public abstract List<IAuthRule> handlePost();

  public List<IAuthRule> commonRulesGet() {
    return new RuleBuilder()
      .allow().metadata().andThen()
      .allow().patch().allRequests()
      .build();
  }
  public List<IAuthRule> commonRulesPost() {
    return new RuleBuilder()
      .allow().metadata().andThen()
      .allow().patch().allRequests().andThen()
      .allow().create().resourcesOfType(type).withAnyId()
      .build();
  }

  public List<IAuthRule> denyRule() {
    return new RuleBuilder()
      .denyAll(denyMessage)
      .build();
  }

  public static IIdType toIdType(String id, String resourceType) {
    return new IdType(resourceType, id);
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