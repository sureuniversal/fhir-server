package ca.uhn.fhir.jpa.starter.authorization.rules;

public class PractitionerRoleRules extends GeneralRules {
  public PractitionerRoleRules(String authHeader) {
    super(authHeader);
  }
}
