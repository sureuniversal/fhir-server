package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Util.CareTeamSearch;
import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.jpa.starter.Util.Search;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class RuleBase {
  protected String denyMessage;
  protected String userId;
  protected UserType userType;
  protected IIdType userOrganization;

  public RequestTypeEnum requestType;
  protected List<String> idsParamValues;
  protected IBaseResource inResource;

  private final String[] userIdsParamName = new String[]{ "subject", "participant", "_has","_id","PractitionerRole","practitioner","organization" };

  public Class<? extends IBaseResource> type;

  public IIdType nullId = new IdType();

  public RuleBase() {}

  public abstract List<IAuthRule> handleGet();

  public abstract List<IAuthRule> handlePost();

  public abstract List<IAuthRule> handleUpdate();

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
    Map<String, String[]> params = theRequestDetails.getParameters();
    this.idsParamValues = new ArrayList<>();
    if (params != null && !params.isEmpty())
    {
      for(String name : this.userIdsParamName)
      {
        String[] value = params.get(name);
        if (value != null)
        {
          switch (name){
            case "organization":
              List<IIdType> orgIds = Search.getAllInOrganization(this.userId);
              List<String> orgStrs = orgIds.stream().map(IIdType::getIdPart).collect(Collectors.toList());
              this.idsParamValues.addAll(orgStrs);
              break;
            default:
              String[] arr = value[0].split(",");
              List<String> valArr = Arrays.asList(arr);
              this.idsParamValues.addAll(valArr);
          }
        }
      }
    }
    else if (theRequestDetails.getId() != null)
    {
      try {
        var id = theRequestDetails.getId();
        this.idsParamValues.add(id.getIdPart());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setupUser(String userId, UserType userType)
  {
    this.userId = userId;
    this.userType = userType;
    if(userType == UserType.practitioner){
      userOrganization = Search.getPractitionerOrganization(userId);
    } else {
      userOrganization = Search.getPatientOrganization(userId);
    }

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

  public void setResourceRequested(RequestDetails theRequestDetails)
  {
    inResource = theRequestDetails.getResource();
  }
}