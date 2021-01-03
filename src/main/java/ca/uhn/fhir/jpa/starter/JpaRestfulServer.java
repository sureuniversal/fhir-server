package ca.uhn.fhir.jpa.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();

    /*
     * Add some logging for each request
     */
    CustomLoggingInterceptor loggingInterceptor = new CustomLoggingInterceptor();
    loggingInterceptor.setLoggerName(HapiProperties.getLoggerName());
    loggingInterceptor.setMessageFormat(HapiProperties.getLoggerFormat());
    loggingInterceptor.setErrorMessageFormat(HapiProperties.getLoggerErrorFormat());
    loggingInterceptor.setIncomingFormat(HapiProperties.getLoggerIncomingFormat());
    loggingInterceptor.setLogExceptions(HapiProperties.getLoggerLogExceptions());
    //loggingInterceptor.setLogRequestSummary(true);
    //loggingInterceptor.setLogRequestBody(true);
    this.registerInterceptor(loggingInterceptor);

  }

}
