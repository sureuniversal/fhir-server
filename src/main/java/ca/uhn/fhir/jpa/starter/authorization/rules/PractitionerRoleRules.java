package ca.uhn.fhir.jpa.starter.authorization.rules;

import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleRules extends PractitionerRules {
  public PractitionerRoleRules() {
    this.type = PractitionerRole.class;
    this.denyMessage = "Practitioner Role Rule";
  }
}
