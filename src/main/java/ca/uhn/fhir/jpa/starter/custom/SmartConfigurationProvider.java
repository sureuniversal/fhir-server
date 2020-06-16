package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.model.dstu2.resource.ConceptMap;
import ca.uhn.fhir.rest.annotation.Operation;
import org.hl7.fhir.r4.model.Person;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class SmartConfigurationProvider {
  @Operation(name=".well-known/smart-configuration", manualResponse=true, manualRequest=true,idempotent = true)
  public void closureOperation(HttpServletRequest request, HttpServletResponse response) throws IOException {

    System.out.println("smart configuration provider");
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    out.println("{" +
      "  \"authorization_endpoint\": \""+ HapiProperties.getServerAddress().replace("fhir/","") + "auth/authorize"+"\"," +
      "  \"token_endpoint\": \""+HapiProperties.getServerAddress().replace("fhir/","") + "auth/token"+"\"," +
      "  \"capabilities\": [\"launch-ehr\", \"client-public\", \"client-confidential-symmetric\", \"context-ehr-patient\", \"sso-openid-connect\"]" +
      "}");
    out.flush();
  }
}
