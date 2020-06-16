package ca.uhn.fhir.jpa.starter.oauth;

import ca.uhn.fhir.jpa.starter.HapiProperties;
import org.bson.Document;
import org.hl7.fhir.r4.model.Person;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns= {"/auth/launch"}, displayName="Launch")
public class Launch extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("launch endpoint accessed");
    String userToken = request.getParameter("usertoken");
    String patientId = request.getParameter("patientid");
    Document userDocument = Utils.GetUserByToken(userToken);
    if(userDocument != null && (userDocument.getBoolean("isFhirAdmin",false) || (userDocument.getString("patientId") != null && userDocument.getString("patientId").equals(patientId)))){
      String url = "https://examples.smarthealthit.org/growth-chart-app/launch.html?";
      url += "iss="+ HapiProperties.getServerAddress();
      url += "&launch=" + userToken;
      response.sendRedirect(url);
    }
    else{
      response.setStatus(403);    //forbidden.
      PrintWriter out = response.getWriter();
      out.println("permission denied");
    }

  }
}
