package ca.uhn.fhir.jpa.starter.oauth;

import org.bson.Document;
import org.hl7.fhir.r4.model.Person;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns= {"/auth/authorize"}, displayName="Authorize")
public class Authorize extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      System.out.println("auth check...");
      String response_type = request.getParameter("response_type");
      String client_id = request.getParameter("client_id");   //the id of the client
      String redirect_uri = request.getParameter("redirect_uri");
      String scope = request.getParameter("scope");   //what the app wants to do
      String state = request.getParameter("state");
      String launch = request.getParameter("launch");

    //the scope parameter includes the launch token - eg patient/*.read launch:7bceb3c6-66e9-46c9-8efd-9f87e76a5f9a
    //so we would pull out both scope and token, check that the token matches the one we set (actually the patient token)
    //and that the scope is acceptable to us. Should move this to a function somewhere...
    String[] arScopes =  scope.split(" ");
    String launchToken = "";
    //for (int i = 0; i < arScopes.length; i++){
      //System.out.println(arScopes[i]);
      //if (arScopes[i] != null && arScopes[i].length()>=7 && arScopes[i].substring(0,7).equals("launch:")) {
        launchToken = launch;
        Document userDocument = Utils.GetUserByToken(launchToken);
        if(userDocument == null){
           response.setStatus(403);
           return;
        }
        response.sendRedirect(redirect_uri + "?code="+launchToken+ "&state="+state);

     // }
    //}


  }
}
