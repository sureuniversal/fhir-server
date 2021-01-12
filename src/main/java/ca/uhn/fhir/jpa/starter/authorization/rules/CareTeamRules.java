package ca.uhn.fhir.jpa.starter.authorization.rules;

public class CareTeamRules extends PatientRules{
  public CareTeamRules(String auth) {
    super(auth);
  }
}
