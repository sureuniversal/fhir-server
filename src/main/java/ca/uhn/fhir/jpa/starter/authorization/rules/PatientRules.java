package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRules extends RuleBase {
  List<IIdType> userIds = new ArrayList<>();

  public PatientRules(String authHeader) {
    super(authHeader);
    this.denyMessage = "Patient can only access himself";
    this.type = Patient.class;
  }

  public void addResource(String id) {
    userIds.add(toIdType(id, "Patient"));
  }

  public void addResourceIds(List<IIdType> ids) {
    userIds.addAll(ids);
  }

  @Override
  public void addResourcesByPractitioner(String id) {
    addPractitioner(id);
    userIds.addAll(Search.getPatients(id, authHeader));
  }

  @Override
  public List<IAuthRule> handleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id :
      userIds) {
      ruleBuilder.allow().read().allResources().inCompartment("Patient", id);
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
