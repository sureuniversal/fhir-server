package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.oauth.Utils;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Interceptor
public class TokenValidationInterceptor {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(TokenValidationInterceptor.class);

  @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
  public void TokenValidation(RequestDetails request) throws Utils.TokenExpiredException,Utils.TokenNotFoundException {
    String authorizationHeader = request.getHeader("authorization");
    String token;
    if(authorizationHeader != null) {
      token = authorizationHeader.split(" ")[1];
    } else {
      ourLog.info("No authorization header");
      throw new Utils.TokenNotFoundException();
    }
    //Document tokenDocument;
    ourLog.info("Validating token:\""+token+"\"");
    try{
      Utils.AuthenticateToken(token);
    } catch (Utils.TokenExpiredException ignored) {

    }
  }
  @Hook(Pointcut.SERVER_HANDLE_EXCEPTION)
  public boolean TokenHandleException(
    RequestDetails theRequestDetails,
    BaseServerResponseException theException,
    HttpServletRequest theServletRequest,
    HttpServletResponse servletResponse) throws java.io.IOException {
    Throwable exception = theException.getCause();
    if(exception instanceof Utils.TokenNotFoundException || exception instanceof Utils.TokenExpiredException){
      ourLog.error(exception.getMessage());
      servletResponse.setStatus(401);
      servletResponse.getWriter().println("Unauthorised:"+exception.getMessage());
      return false;
    }
    return true;
  }

}
