package ca.uhn.fhir.jpa.starter.authorization.rules;

import org.hl7.fhir.r4.model.Observation;

public class ObservationRules extends PatientRules {
  public ObservationRules(String authHeader) {
    super(authHeader);
    this.denyMessage = "cant access Observation";
    this.type = Observation.class;
  }
}
