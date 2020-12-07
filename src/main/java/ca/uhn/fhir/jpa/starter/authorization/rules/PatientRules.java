package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.db.CareTeamSearch;
import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRules extends RuleBase {
  List<IIdType> userIds = new ArrayList<>();
  List<IIdType> practitionerIds = new ArrayList<>();

  public PatientRules(String authHeader) {
    super(authHeader);
    this.denyMessage = "Patient Rule";
    this.type = Patient.class;
  }

  public void addResource(String id) {
    var patientsCreated = Search.getPatientsCreatedByPatientWithId(id);
    userIds.addAll(patientsCreated);
    userIds.add(toIdType(id, "Patient"));
  }

  public void addResourceIds(List<IIdType> ids) {
    userIds.addAll(ids);
  }

  @Override
  public void addCareTeam(List<IIdType> ids) {
    for (var itm : ids) {
        if (itm.getResourceType().equals("Patient")) {
          userIds.add(itm);
        } else {
          practitionerIds.add(itm);
        }
      }
  }

  @Override
  public void addResourcesByPractitioner(String id) {
    var patientsCreated = Search.getPatientsCreatedByPatientWithId(id);
    userIds.addAll(patientsCreated);
    addPractitioner(id);
    userIds.addAll(Search.getPatients(id, authHeader));
  }

  public void handleCareTeam()
  {
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
  }

  @Override
  public List<IAuthRule> handleGet() {
    this.handleCareTeam();
    var myId = RuleBase.toIdType(this.userId, "Patient");
    userIds.add(myId);
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id :
      userIds) {
      ruleBuilder.allow().read().allResources().inCompartment("Patient", id);
    }

    for (var id :
      practitionerIds) {
      ruleBuilder.allow().read().allResources().inCompartment("Practitioner", id);
    }

    List<IAuthRule> patientRule = ruleBuilder.build();
    List<IAuthRule> commonRules = commonRulesGet();
    List<IAuthRule> denyRule = denyRule();
    if (practitionerId != null) {
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().read().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }
    ruleList.addAll(patientRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    return new RuleBuilder()
      .allowAll("")
      .build();
  }
}
