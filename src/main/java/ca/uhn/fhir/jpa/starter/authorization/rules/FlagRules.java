package ca.uhn.fhir.jpa.starter.authorization.rules;

import org.hl7.fhir.r4.model.Flag;

public class FlagRules extends PatientRules {
  public FlagRules() {
    this.denyMessage = "cant access Flag";
    this.type = Flag.class;
  }
}
