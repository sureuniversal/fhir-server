package ca.uhn.fhir.jpa.starter.oauth;

import com.google.gson.JsonObject;
import org.bson.Document;
import org.hl7.fhir.r4.model.Person;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns= {"/auth/token"}, displayName="Token")
public class Token extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      System.out.println("auth/token");
    response.setContentType("application/json+fhir");

    PrintWriter out = response.getWriter();
      String code = request.getParameter("code");
      Document userDocument = Utils.GetUserByToken(code);
      if(userDocument == null){
        System.out.println("token not found");
        response.setStatus(403);
        return;
      }
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("access_token",code);
      jsonObject.addProperty("patient",userDocument.getString("patientId"));
      jsonObject.addProperty("token_type",userDocument.getString("bearer"));
      jsonObject.addProperty("expires_in",userDocument.getString("3600"));
      jsonObject.addProperty("scope",userDocument.getString("patient/*.read"));


    //response.setCharacterEncoding("UTF-8");
      out.println(jsonObject.toString());
      out.flush();

  }
}
