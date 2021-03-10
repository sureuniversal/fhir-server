package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.r4.model.Observation;

import java.util.List;

public class ObservationRules extends PatientRules {
  public ObservationRules() {
    this.denyMessage = "cant access Observation";
    this.type = Observation.class;
  }
  // sec rules updates
  // We need to check if the request body to see if the subject for the observation is referencing the sending user
  @Override
  public List<IAuthRule> handlePost() {
    return new RuleBuilder().allowAll().build();
  }

  // sec rules updates
  // override handleUpdate and give it a deny all for an observation
}