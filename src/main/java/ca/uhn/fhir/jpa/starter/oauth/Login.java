package ca.uhn.fhir.jpa.starter.oauth;

import org.hl7.fhir.r4.model.Person;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns= {"/auth/login"}, displayName="Login")
public class Login extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.out.println("login");
    PrintWriter out = response.getWriter();
    Person person = new Person();
    //Here is where we check and validate username &amp;amp; password. We're going to cheat right now...
    //create a user object. This would ultimately be a FHIR obect I suspect...
  }
}
