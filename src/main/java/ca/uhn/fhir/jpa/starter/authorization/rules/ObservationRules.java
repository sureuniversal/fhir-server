package ca.uhn.fhir.jpa.starter.authorization.rules;

public class ObservationRules extends PatientRules {
  public ObservationRules(String authHeader) {
    super(authHeader);
    this.denyMessage = "cant access Observation";
  }
}
