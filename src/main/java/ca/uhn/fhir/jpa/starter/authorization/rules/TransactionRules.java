package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;

public class TransactionRules extends RuleBase {
  List<IIdType> deviceMetricIds = new ArrayList<>();
  List<IIdType> deviceIds = new ArrayList<>();
  List<IIdType> patientIds = new ArrayList<>();
  List<String> types;

  public TransactionRules(String auth, List<String> ruleTypes) {
    super(auth);
    types = ruleTypes;
    type = Bundle.class;
  }

  @Override
  public List<IAuthRule> handleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    if (types.contains("Observation") || types.contains("Patient")) {
      for (var id : patientIds) {
        ruleBuilder.allow().read().allResources().inCompartment("Patient", id);
      }
    }
    if (types.contains("Device")) {
      for (var id : deviceIds) {
        ruleBuilder.allow().read().allResources().inCompartment("Device", id);
      }
    }
    if (types.contains("DeviceMetric")) {
      for (var id : deviceMetricIds) {
        ruleBuilder.allow().read().allResources().inCompartment("DeviceMetric", id);
      }
    }
    List<IAuthRule> rules = ruleBuilder.build();
    List<IAuthRule> commonRules = commonRulesGet();
    List<IAuthRule> denyRule = denyRule();
    if (practitionerId != null) {
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().read().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
    }
    ruleList.addAll(rules);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    if (types.contains("Observation") || types.contains("Patient")) {
      for (var id : patientIds) {
        ruleBuilder.allow().write().allResources().inCompartment("Patient", id);
      }
      ruleBuilder.allow().create().resourcesOfType(Patient.class).withAnyId();
      ruleBuilder.allow().create().resourcesOfType(Observation.class).withAnyId();
    }
    if (types.contains("Device")) {
      for (var id : deviceIds) {
        ruleBuilder.allow().write().allResources().inCompartment("Device", id);
      }
      ruleBuilder.allow().create().resourcesOfType(Device.class).withAnyId();
    }
    if (types.contains("DeviceMetric")) {
      for (var id : deviceMetricIds) {
        ruleBuilder.allow().write().allResources().inCompartment("DeviceMetric", id);
      }
      ruleBuilder.allow().create().resourcesOfType(DeviceMetric.class).withAnyId();
    }
    List<IAuthRule> rules = ruleBuilder.build();
    List<IAuthRule> commonRules = commonRulesPost();
    List<IAuthRule> denyRule = denyRule();
    if (practitionerId != null) {
      List<IAuthRule> practitionerRule = new RuleBuilder().allow().write().allResources().inCompartment("Practitioner", practitionerId).build();
      ruleList.addAll(practitionerRule);
      ruleBuilder.allow().create().resourcesOfType(Practitioner.class).withAnyId();
    }
    ruleList.addAll(rules);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public void addResource(String id) {
    if (types.contains("Observation") || types.contains("Patient")) {
      patientIds.add(toIdType(id, "Patient"));
    }
    if (types.contains("Device")) {
      deviceIds.addAll(Search.getDevices(id, authHeader));
    }
    if (types.contains("DeviceMetric")) {
      deviceMetricIds.addAll(Search.getDeviceMetrics(id, authHeader));
    }
  }

  @Override
  public void addResourceIds(List<IIdType> id) {
    if (types.contains("Observation") || types.contains("Patient")) {
      patientIds.addAll(id);
    }
    if (types.contains("Device")) {
      deviceIds.addAll(Search.getDevices(id, authHeader));
    }
    if (types.contains("DeviceMetric")) {
      deviceMetricIds.addAll(Search.getDeviceMetrics(id, authHeader));
    }
  }

  @Override
  public void addResourcesByPractitioner(String id) {
    addPractitioner(id);
    List<IIdType> idList = Search.getPatients(id, authHeader);
    if (types.contains("Observation") || types.contains("Patient")) {
      patientIds.addAll(idList);
    }
    if (types.contains("Device")) {
      deviceIds.addAll(Search.getDevices(idList, authHeader));
    }
    if (types.contains("DeviceMetric")) {
      deviceMetricIds.addAll(Search.getDeviceMetrics(idList, authHeader));
    }
  }
}
