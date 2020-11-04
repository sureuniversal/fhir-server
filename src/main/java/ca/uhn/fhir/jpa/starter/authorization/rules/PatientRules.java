package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientRules extends RuleBase {
  List<IIdType> patientIds = new ArrayList<>();

  public PatientRules(String authHeader) {
    super(authHeader);
    this.denyMessage = "Patient can only access himself";
    this.type = Patient.class;
  }

  public void addResource(String id) {
    patientIds.add(toIdType(id, "Patient"));
  }

  public void addResourceIds(List<IIdType> ids) {
    patientIds.addAll(ids);
  }

  @Override
  public void addResourcesByPractitioner(String id) {
    addPractitioner(id);
    patientIds.addAll(Search.getPatients(id, authHeader));
  }

  @Override
  public List<IAuthRule> specificRulesGet() {
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : patientIds) {
      ruleBuilder.allow().read().allResources().inCompartment("Patient", id);
    }
    return ruleBuilder.build();
  }

  @Override
  public List<IAuthRule> specificRulesPost() {
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : patientIds) {
      ruleBuilder.allow().write().allResources().inCompartment("Patient", id);
    }
    return ruleBuilder.build();
  }
}
