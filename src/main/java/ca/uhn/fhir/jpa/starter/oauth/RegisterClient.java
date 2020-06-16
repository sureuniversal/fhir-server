package ca.uhn.fhir.jpa.starter.oauth;

import com.google.gson.JsonObject;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns= {"/auth/register"}, displayName="RegisterClient")
public class RegisterClient extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      System.out.println("auth/register");
      response.setHeader("Content-Type","application/json+fhir;charset=UTF-8");
      PrintWriter out = response.getWriter();
      String verificationToken = request.getParameter("verificationToken");
      Document clientDocument = Utils.GetClientByToken(verificationToken);
      if(clientDocument == null){
        System.out.println("token not found");
        response.setStatus(403);
        return;
      }
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("client_id",clientDocument.getString("clientId"));
      jsonObject.addProperty("client_secret",clientDocument.getString("clientSecret"));
      jsonObject.addProperty("client_secret_expires_at","0");


      //response.setContentType("Content-Type","application/json+fhir");
    //response.setCharacterEncoding("UTF-8");

      out.println(jsonObject.toString());
      out.flush();

  }
}
