package ca.uhn.fhir.jpa.starter.authorization.rules;

import org.hl7.fhir.r4.model.PractitionerRole;

public class PractitionerRoleRules extends GeneralRules {
  public PractitionerRoleRules(String authHeader) {
    super(authHeader);
    this.type = PractitionerRole.class;
  }
}
