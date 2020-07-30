package ca.uhn.fhir.jpa.starter;


import ca.uhn.fhir.jpa.starter.oauth.Utils;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns= {"/getUserIdByAccessToken"}, displayName="getUserIdByAccessToken")
public class getUserIdByAccessToken extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String token = request.getParameter("access_token");
    PrintWriter out = response.getWriter();
    if(token == null){
      response.setStatus(400);
      out.println("Request is malformed");
      return;
    }
    Document tokenDocument;
    try {
      tokenDocument = Utils.AuthenticateToken(token);
    } catch (Utils.TokenNotFoundException e) {
      response.setStatus(401);
      out.println("Access token not found");
      return;
    } catch (Utils.TokenExpiredException e) {
      response.setStatus(401);
      out.println("Access token has expired");
      return;
    }
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    out.print("{\n" +
      "   \"userId\": \""+tokenDocument.getString("uid")+"\"\n" +
      "}");
    out.flush();
  }
}