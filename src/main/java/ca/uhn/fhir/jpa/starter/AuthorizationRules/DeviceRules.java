package ca.uhn.fhir.jpa.starter.AuthorizationRules;

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
  {
    this.denyMessage = "Patient can only access himself";
  }

  List<IIdType> deviceIds = new ArrayList<>();
  IGenericClient client;

  public DeviceRules(IGenericClient client1){
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
        deviceIds.add(itm.getResource().getIdElement().toUnqualifiedVersionless());
      }
    }
  }

  @Override
  public List<IAuthRule> HandleGet() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> patientRule = new RuleBuilder().allow().read().allResources().inCompartment("Device", deviceIds).build();
    List<IAuthRule> denyRule = DenyRule();
    ruleList.addAll(patientRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }

  @Override
  public List<IAuthRule> HandlePost() {
    List<IAuthRule> ruleList = new ArrayList<>();
    List<IAuthRule> patientRule = new RuleBuilder().allow().write().allResources().inCompartment("Patient", this.userIds).build();
    List<IAuthRule> patchRule = PatchRule();
    List<IAuthRule> denyRule = DenyRule();
    ruleList.addAll(patientRule);
    ruleList.addAll(patchRule);
    ruleList.addAll(denyRule);

    return ruleList;
  }
}
