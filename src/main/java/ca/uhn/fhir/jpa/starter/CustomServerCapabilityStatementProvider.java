package ca.uhn.fhir.jpa.starter;


import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import ca.uhn.fhir.jpa.provider.r4.JpaConformanceProviderR4;
import ca.uhn.fhir.jpa.searchparam.registry.ISearchParamRegistry;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.hl7.fhir.r4.model.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class CustomServerCapabilityStatementProvider extends JpaConformanceProviderR4 {


  public CustomServerCapabilityStatementProvider(JpaRestfulServer jpaRestfulServer, IFhirSystemDao<Bundle, Meta> systemDao, DaoConfig bean, ISearchParamRegistry theSearchParamRegistry) {
    super(jpaRestfulServer,systemDao,bean,theSearchParamRegistry);
  }

  @Override
  public CapabilityStatement getServerConformance(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
      CapabilityStatement capabilityStatement = super.getServerConformance(theRequest, theRequestDetails);
      CapabilityStatement.CapabilityStatementRestComponent capabilityStatementRestComponent = capabilityStatement.getRest().get(0);
      CapabilityStatement.CapabilityStatementRestSecurityComponent capabilityStatementRestSecurityComponent = new CapabilityStatement.CapabilityStatementRestSecurityComponent();

      Extension extensionParent =  new Extension();
      extensionParent.setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");

      Extension firstChild = new Extension();
      firstChild.setUrl("authorize");
      firstChild.setValue(new UriType(HapiProperties.getServerAddress().replace("fhir/","") + "auth/authorize"));

      Extension secondChild = new Extension();
      secondChild.setUrl("token");
      secondChild.setValue(new UriType(HapiProperties.getServerAddress().replace("fhir/","") +"auth/token"));

      extensionParent.addExtension(firstChild);
      extensionParent.addExtension(secondChild);

      capabilityStatementRestSecurityComponent.addExtension(extensionParent);
      capabilityStatementRestComponent.setSecurity(capabilityStatementRestSecurityComponent);

      CodeableConcept service = new CodeableConcept();
      Coding coding = new Coding();
      coding.setSystem("http://hl7.org/fhir/restful-security-service");
      coding.setCode("SMART-on-FHIR");
      coding.setDisplay("SMART-on-FHIR");
      service.addCoding(coding);
      service.setText("OAuth2 using SMART-on-FHIR profile (see http://docs.smarthealthit.org)");
      capabilityStatementRestSecurityComponent.addService(service);

      return  capabilityStatement;


  }
}
