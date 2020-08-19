package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ca.uhn.fhir.util.UrlUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Interceptor
public class CustomLoggingInterceptor extends LoggingInterceptor {
  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger("");

  CustomLoggingInterceptor(){
    super();
  }

  @Override
  @Hook(Pointcut.SERVER_HANDLE_EXCEPTION)
  public boolean handleException(RequestDetails theRequestDetails, BaseServerResponseException theException, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
    String log ="ERROR - " + theServletRequest.getMethod() + " "
      + theServletRequest.getRequestURL().toString();
    ourLog.error(log);
    return true;
  }
  @Override
  @Hook(Pointcut.SERVER_PROCESSING_COMPLETED_NORMALLY)
  public void processingCompletedNormally(ServletRequestDetails theRequestDetails){
    HttpServletRequest myRequest = theRequestDetails.getServletRequest();
    String resourceName = theRequestDetails.getResourceName();
    if(resourceName == null) resourceName = "";
    String log = myRequest.getMethod() + " "
      + resourceName + getParams(theRequestDetails) + " "
      + getBody(theRequestDetails)
      + " headers: authorization=" + myRequest.getHeader("authorization")
      + " IP:" + myRequest.getRemoteAddr();
    ourLog.info(log);
  }

  private static String getParams(ServletRequestDetails myRequestDetails) {
    StringBuilder b = new StringBuilder();
    for (Map.Entry<String, String[]> next : myRequestDetails.getParameters().entrySet()) {
      for (String nextValue : next.getValue()) {
        if (b.length() == 0) {
          b.append('?');
        } else {
          b.append('&');
        }
        b.append(UrlUtil.escapeUrlParam(next.getKey()));
        b.append('=');
        b.append(UrlUtil.escapeUrlParam(nextValue));
      }
    }
    return URLDecoder.decode(b.toString(),Constants.CHARSET_UTF8);
  }

  private static String getBody(ServletRequestDetails myRequestDetails){
    String contentType = myRequestDetails.getServletRequest().getContentType();
    if (isNotBlank(contentType)) {
      int colonIndex = contentType.indexOf(';');
      if (colonIndex != -1) {
        contentType = contentType.substring(0, colonIndex);
      }
      contentType = contentType.trim();

      EncodingEnum encoding = EncodingEnum.forContentType(contentType);
      if (encoding != null) {
        byte[] requestContents = myRequestDetails.loadRequestContents();
        return new String(requestContents, Constants.CHARSET_UTF8);
      }
    }
    return "";
  }
}
