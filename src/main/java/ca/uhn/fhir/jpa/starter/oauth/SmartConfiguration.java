package ca.uhn.fhir.jpa.starter.oauth;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import org.hl7.fhir.r4.model.Person;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns= {"/.well-known/smart-configuration"}, displayName="smart-configuration")
public class SmartConfiguration extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("get well know configuration");
    PrintWriter out = response.getWriter();
    Person person = new Person();
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
