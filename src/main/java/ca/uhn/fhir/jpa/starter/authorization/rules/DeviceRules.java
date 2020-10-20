package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceRules extends RuleBase {

  List<IIdType> deviceIds = new ArrayList<>();
  IGenericClient client;

  public DeviceRules(IGenericClient client1){
    super();
    this.denyMessage = "Device not associated with patient";
    client=client1;
  }

  @Override
  public void addResourceIds(List<IIdType> ids) {
    super.addResourceIds(ids);
    for (var id : ids) {
      Bundle deviceBundle = (Bundle)client.search().forResource(Device.class)
        .where(new ReferenceClientParam("patient").hasId(id))
        .prettyPrint()
        .execute();
      for (var itm: deviceBundle.getEntry()){
        deviceIds.add(itm.getResource().getIdElement().toUnqualifiedVersionless());
      }
    }
  }

  @Override
  public void addResource(String id) {
    super.addResource(id);
    Bundle deviceBundle = (Bundle)client.search().forResource(Device.class)
      .where(new ReferenceClientParam("patient").hasId(id))
      .prettyPrint()
      .execute();
    for (var itm: deviceBundle.getEntry()){
      deviceIds.add(itm.getResource().getIdElement().toUnqualifiedVersionless());
    }
  }

  @Override
  public List<IAuthRule> handleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : deviceIds) {
      ruleBuilder.allow().read().allResources().inCompartment("Device", id);
    }
    List<IAuthRule> deviceRule = ruleBuilder.build();
    List<IAuthRule> commonRules = commonRules();
    List<IAuthRule> denyRule = denyRule();
    ruleList.addAll(deviceRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> handlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : deviceIds) {
      ruleBuilder.allow().write().allResources().inCompartment("Device", id);
    }
    List<IAuthRule> deviceRule = ruleBuilder.build();
    List<IAuthRule> commonRules = commonRules();
    List<IAuthRule> denyRule = denyRule();
    ruleList.addAll(deviceRule);
    ruleList.addAll(commonRules);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
