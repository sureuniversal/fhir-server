package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import ca.uhn.fhir.util.UrlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/*
 * #%L
 * HAPI FHIR - Server Framework
 * %%
 * Copyright (C) 2014 - 2020 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

@Interceptor
public class CustomLoggingInterceptor {
  private static final Logger ourLog = LoggerFactory.getLogger(LoggingInterceptor.class);

  private String myErrorMessageFormat = "ERROR - ${operationType} - ${idOrResourceName}";
  private boolean myLogExceptions = true;
  private Logger myLogger = ourLog;
  private String myMessageFormat = "${operationType} - ${idOrResourceName}";
  private String myIncomingFormat = "${operationType} - ${idOrResourceName}";


  /**
   * Get the log message format to be used when logging exceptions
   */
  public String getErrorMessageFormat() {
    return myErrorMessageFormat;
  }

  @Hook(Pointcut.SERVER_HANDLE_EXCEPTION)
  public boolean handleException(RequestDetails theRequestDetails, BaseServerResponseException theException, HttpServletRequest theServletRequest, HttpServletResponse theServletResponse) {
    if (myLogExceptions) {
      // Perform any string substitutions from the message format
      StringLookup lookup = new MyLookup(theServletRequest, theException, theRequestDetails);
      StringSubstitutor subs = new StringSubstitutor(lookup, "${", "}", '\\');

      // Actually log the line
      String line = subs.replace(myErrorMessageFormat);
      myLogger.error(line);

    }
    return true;
  }

  @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
  public void requestPreHandled(ServletRequestDetails theRequestDetails){
    // Perform any string substitutions from the message format
    StringLookup lookup = new MyLookup(theRequestDetails.getServletRequest(), theRequestDetails);
    StringSubstitutor subs = new StringSubstitutor(lookup, "${", "}", '\\');

    // Actually log the line
    String line = subs.replace(myIncomingFormat);
    myLogger.info(line);
  }

  @Hook(Pointcut.SERVER_PROCESSING_COMPLETED_NORMALLY)
  public void processingCompletedNormally(ServletRequestDetails theRequestDetails) {
    // Perform any string substitutions from the message format
    StringLookup lookup = new MyLookup(theRequestDetails.getServletRequest(), theRequestDetails);
    StringSubstitutor subs = new StringSubstitutor(lookup, "${", "}", '\\');

    // Actually log the line
    String line = subs.replace(myMessageFormat);
    myLogger.info(line);
  }

  /**
   * Should exceptions be logged by this logger
   */
  public boolean isLogExceptions() {
    return myLogExceptions;
  }

  /**
   * Set the log message format to be used when logging exceptions
   */
  public void setErrorMessageFormat(String theErrorMessageFormat) {
    Validate.notBlank(theErrorMessageFormat, "Message format can not be null/empty");
    myErrorMessageFormat = theErrorMessageFormat;
  }

  /**
   * Should exceptions be logged by this logger
   */
  public void setLogExceptions(boolean theLogExceptions) {
    myLogExceptions = theLogExceptions;
  }

  public void setLogger(Logger theLogger) {
    Validate.notNull(theLogger, "Logger can not be null");
    myLogger = theLogger;
  }

  public void setLoggerName(String theLoggerName) {
    Validate.notBlank(theLoggerName, "Logger name can not be null/empty");
    myLogger = LoggerFactory.getLogger(theLoggerName);

  }

  /**
   * Sets the message format itself. See the {@link LoggingInterceptor class documentation} for information on the
   * format
   */
  public void setMessageFormat(String theMessageFormat) {
    Validate.notBlank(theMessageFormat, "Message format can not be null/empty");
    myMessageFormat = theMessageFormat;
  }

  /**
   * Sets the incoming message format itself. See the {@link LoggingInterceptor class documentation} for information on the
   * format
   */
  public void setIncomingFormat(String theIncomingFormat) {
    Validate.notBlank(theIncomingFormat, "Message format can not be null/empty");
    myIncomingFormat = theIncomingFormat;
  }

  private static final class MyLookup implements StringLookup {
    private final Throwable myException;
    private final HttpServletRequest myRequest;
    private final RequestDetails myRequestDetails;

    private MyLookup(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
      myRequest = theRequest;
      myRequestDetails = theRequestDetails;
      myException = null;
    }

    MyLookup(HttpServletRequest theServletRequest, BaseServerResponseException theException, RequestDetails theRequestDetails) {
      myException = theException;
      myRequestDetails = theRequestDetails;
      myRequest = theServletRequest;
    }

    @Override
    public String lookup(String theKey) {
      if (theKey.startsWith("requestHeader.")) {
        String val = myRequest.getHeader(theKey.substring("requestHeader.".length()));
        return StringUtils.defaultString(val);
      } else if (theKey.startsWith("remoteAddr")) {
        return StringUtils.defaultString(myRequest.getRemoteAddr());
      } else {
        switch (theKey) {
          case "operationType":
            if (myRequestDetails.getRestOperationType() != null) {
              return myRequestDetails.getRestOperationType().getCode();
            }
            return "";
          case "operationName":
            if (myRequestDetails.getRestOperationType() != null) {
              switch (myRequestDetails.getRestOperationType()) {
                case EXTENDED_OPERATION_INSTANCE:
                case EXTENDED_OPERATION_SERVER:
                case EXTENDED_OPERATION_TYPE:
                  return myRequestDetails.getOperation();
                default:
                  return "";
              }
            }
            return "";
          case "id":
            if (myRequestDetails.getId() != null) {
              return myRequestDetails.getId().getValue();
            }
            return "";
          case "servletPath":
            return StringUtils.defaultString(myRequest.getServletPath());
          case "idOrResourceName":
            if (myRequestDetails.getId() != null) {
              return myRequestDetails.getId().getValue();
            }
            if (myRequestDetails.getResourceName() != null) {
              return myRequestDetails.getResourceName();
            }
            return "";
          case "responseEncodingNoDefault": {
            RestfulServerUtils.ResponseEncoding encoding = RestfulServerUtils.determineResponseEncodingNoDefault(myRequestDetails, myRequestDetails.getServer().getDefaultResponseEncoding());
            if (encoding != null) {
              return encoding.getEncoding().name();
            }
            return "";
          }
          case "exceptionMessage":
            return myException != null ? myException.getMessage() : null;
          case "requestUrl":
            return myRequest.getRequestURL().toString();
          case "requestVerb":
            return myRequest.getMethod();
          case "requestBodyFhir": {
            String contentType = myRequest.getContentType();
            if (isNotBlank(contentType)) {
                byte[] requestContents = myRequestDetails.loadRequestContents();
                return new String(requestContents, Constants.CHARSET_UTF8);
            }
            return "";
          }
          case "processingTimeMillis":
            Date startTime = (Date) myRequest.getAttribute(RestfulServer.REQUEST_START_TIME);
            if (startTime != null) {
              long time = System.currentTimeMillis() - startTime.getTime();
              return Long.toString(time);
            }
          case "requestId":
            return myRequestDetails.getRequestId();
          case "requestParameters":
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
            return URLDecoder.decode(b.toString(), Constants.CHARSET_UTF8);
          default:
            return "!VAL!";
        }
      }
    }
  }
}
