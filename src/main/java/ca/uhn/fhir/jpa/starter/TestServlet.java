package ca.uhn.fhir.jpa.starter;

import java.io.IOException;
import java.io.PrintWriter;

public class TestServlet extends javax.servlet.http.HttpServlet {
  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TestServlet.class);
  /*    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }*/

  @Override
  protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {
      response.setContentType("text/html");
      response.setCharacterEncoding("UTF-8");

      try (PrintWriter writer = response.getWriter()) {
          writer.println("<!DOCTYPE html><html>");
          writer.println("<head>");
          writer.println("<meta charset=\"UTF-8\" />");
          writer.println("<title>MyServlet.java:doGet(): Servlet code!</title>");
          writer.println("</head>");
          writer.println("<body>");

          writer.println("<h1>This is a simple java servlet.</h1>");

          writer.println("</body>");
          writer.println("</html>");
      }

      ourLog.info(request.getRequestURI());
      ourLog.info("referer:" + request.getHeader("referer"));

  }
}
