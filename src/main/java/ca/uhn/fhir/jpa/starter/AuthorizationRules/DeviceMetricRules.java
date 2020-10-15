package ca.uhn.fhir.jpa.starter.AuthorizationRules;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DeviceMetric;

import java.util.ArrayList;
import java.util.List;

public class DeviceMetricRules extends RuleBase {
  {
    this.denyMessage = "DeviceMetric not associated with patient";
  }

  List<IIdType> deviceMetricIds = new ArrayList<>();
  IGenericClient client;

  public DeviceMetricRules(IGenericClient client1){
    super();
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
        Bundle deviceMetricBundle = (Bundle) client.search().forResource(DeviceMetric.class)
          .where(new ReferenceClientParam("source").hasId(itm.getResource().getIdElement().toUnqualifiedVersionless()))
          .prettyPrint()
          .execute();
        for (var itm2: deviceMetricBundle.getEntry()){
          deviceMetricIds.add(itm2.getResource().getIdElement().toUnqualifiedVersionless());
        }
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
      Bundle deviceMetricBundle = (Bundle) client.search().forResource(DeviceMetric.class)
        .where(new ReferenceClientParam("source").hasId(itm.getResource().getIdElement().toUnqualifiedVersionless()))
        .prettyPrint()
        .execute();
      for (var itm2: deviceMetricBundle.getEntry()){
        deviceMetricIds.add(itm2.getResource().getIdElement().toUnqualifiedVersionless());
      }
    }
  }

  @Override
  public List<IAuthRule> HandleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : deviceMetricIds) {
      ruleBuilder.allow().read().allResources().inCompartment("DeviceMetric", id);
    }
    List<IAuthRule> deviceRule = ruleBuilder.build();
    List<IAuthRule> denyRule = DenyRule();
    ruleList.addAll(deviceRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> HandlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    RuleBuilder ruleBuilder = new RuleBuilder();
    for (var id : deviceMetricIds) {
      ruleBuilder.allow().write().allResources().inCompartment("DeviceMetric", id);
    }
    List<IAuthRule> deviceRule = ruleBuilder.build();
    List<IAuthRule> patchRule = PatchRule();
    List<IAuthRule> denyRule = DenyRule();
    ruleList.addAll(deviceRule);
    ruleList.addAll(patchRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
