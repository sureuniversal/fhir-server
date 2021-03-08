package ca.uhn.fhir.jpa.starter.authorization.rules;

import org.hl7.fhir.r4.model.DiagnosticReport;

public class DiagnosticReportRules extends PatientRules {
  public DiagnosticReportRules() {
    this.denyMessage = "cant access diagnostic report";
    this.type = DiagnosticReport.class;
  }
}
