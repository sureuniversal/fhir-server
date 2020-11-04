package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.DeviceMetric;

import java.util.ArrayList;
import java.util.List;

public class DeviceMetricRules extends RuleBase {

  List<IIdType> deviceMetricIds = new ArrayList<>();

  public DeviceMetricRules(String auth) {
    super(auth);
    this.denyMessage = "DeviceMetric not associated with patient";
    this.type = DeviceMetric.class;
  }

  @Override
  public void addResourceIds(List<IIdType> ids) {
    deviceMetricIds.addAll(Search.getDeviceMetrics(ids, authHeader));
  }

  @Override
  public void addResource(String id) {
    deviceMetricIds.addAll(Search.getDeviceMetrics(id, authHeader));
  }

  @Override
  public void addResourcesByPractitioner(String id) {
    addPractitioner(id);
    List<IIdType> ids = Search.getPatients(id, authHeader);
    deviceMetricIds.addAll(Search.getDeviceMetrics(ids, authHeader));
  }

  @Override
  public List<IAuthRule> specificRulesGet() {
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : deviceMetricIds) {
      ruleBuilder.allow().read().allResources().inCompartment("DeviceMetric", id);
    }
    return ruleBuilder.build();
  }

  @Override
  public List<IAuthRule> specificRulesPost() {
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : deviceMetricIds) {
      ruleBuilder.allow().write().allResources().inCompartment("DeviceMetric", id);
    }
    return ruleBuilder.build();
  }
}
