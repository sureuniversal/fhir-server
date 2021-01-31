package ca.uhn.fhir.jpa.starter.authorization.rules;

import org.hl7.fhir.r4.model.Device;

public class DeviceRules extends PatientRules {
  public DeviceRules() {
    this.denyMessage = "Device Rule";
    this.type = Device.class;
  }
}
