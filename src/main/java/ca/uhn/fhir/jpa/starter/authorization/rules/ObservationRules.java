package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.ArrayList;
import java.util.List;

public class ObservationRules extends PatientRules {
  public ObservationRules(String authHeader) {
    super(authHeader);
    this.denyMessage = "cant access Observation";
  }
}
