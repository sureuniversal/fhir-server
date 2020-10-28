package ca.uhn.fhir.jpa.starter.authorization.rules;

import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerRules extends GeneralRules {
  public PractitionerRules(String authHeader) {
    super(authHeader);
    type = Practitioner.class;
  }
}
