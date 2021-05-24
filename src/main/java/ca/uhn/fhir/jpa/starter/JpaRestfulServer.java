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

    Search.setClientByContext(ctx);

    TokenValidationInterceptor tokenValidationInterceptor = new TokenValidationInterceptor();
    this.registerInterceptor(tokenValidationInterceptor);

  }

}
