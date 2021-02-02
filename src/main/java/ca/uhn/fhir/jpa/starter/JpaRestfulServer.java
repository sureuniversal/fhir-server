package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.Util.Search;
import ca.uhn.fhir.narrative.DefaultThymeleafNarrativeGenerator;
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
     * This server tries to dynamically generate narratives
     */
    FhirContext ctx = getFhirContext();
    ctx.setNarrativeGenerator(new DefaultThymeleafNarrativeGenerator());
    if(System.getenv("SERVER_BASE_ADDRESS") != null){
      appProperties.setServer_address(System.getenv("SERVER_BASE_ADDRESS"));
    } else {
      appProperties.setServer_address("");
    }

    Search.setClientByContext(ctx);

    TokenValidationInterceptor tokenValidationInterceptor = new TokenValidationInterceptor();
    this.registerInterceptor(tokenValidationInterceptor);

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
