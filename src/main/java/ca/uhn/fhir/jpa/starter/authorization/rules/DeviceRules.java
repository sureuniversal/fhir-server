package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceRules extends RuleBase {

  List<IIdType> deviceIds = new ArrayList<>();

  public DeviceRules(String auth) {
    super(auth);
    this.denyMessage = "Device not associated with patient";
    this.type = Device.class;
  }

  @Override
  public void addResourceIds(List<IIdType> ids) {
    deviceIds.addAll(Search.getDevices(ids, authHeader));
  }

  @Override
  public void addResourcesByPractitioner(String id) {
    addPractitioner(id);
    List<IIdType> ids = Search.getPatients(id, authHeader);
    deviceIds.addAll(Search.getDevices(ids, authHeader));
  }

  @Override
  public void addResource(String id) {
    deviceIds.addAll(Search.getDevices(id, authHeader));
  }

  @Override
  public List<IAuthRule> specificRulesGet() {
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : deviceIds) {
      ruleBuilder.allow().read().allResources().inCompartment("Device", id);
    }
    return ruleBuilder.build();
  }

  @Override
  public List<IAuthRule> specificRulesPost() {
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : deviceIds) {
      ruleBuilder.allow().write().allResources().inCompartment("Device", id);
    }
    return ruleBuilder.build();
  }
}
