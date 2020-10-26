package ca.uhn.fhir.jpa.starter.authorization.rules;

public class PractitionerRules extends GeneralRules {
  public PractitionerRules(String authHeader) {
    super(authHeader);
  }
}
