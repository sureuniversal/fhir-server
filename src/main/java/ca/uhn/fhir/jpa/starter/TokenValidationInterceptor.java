package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.authorization.rules.*;
import ca.uhn.fhir.jpa.starter.oauth.TokenRecord;
import ca.uhn.fhir.jpa.starter.oauth.Utils;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

    if(theRequestDetails.getCompleteUrl().split("\\?")[0].contains(":8080")) {
      return new RuleBuilder()
        .allowAll("Port 8080")
        .build();
    }

    String authHeader = theRequestDetails.getHeader("Authorization");
    if (authHeader == null){
      return new RuleBuilder()
        .denyAll("no authorization header")
        .build();
    }

    String token = authHeader.replace("Bearer ","");

    TokenRecord tokenRecord = Utils.getTokenRecord(token);

    if (tokenRecord != null) {
      String bearerId = tokenRecord.getId();
      FhirContext ctx = theRequestDetails.getFhirContext();

      IGenericClient client = ctx.newRestfulGenericClient(theRequestDetails.getFhirServerBase());

      boolean isPractitioner = tokenRecord.is_practitioner();
      List<IIdType> patients = isPractitioner ? getPatientsList(client, bearerId, authHeader) : new ArrayList<>();

      RuleBase ruleBase = RuleBase.rulesFactory(theRequestDetails);
      if (ruleBase == null) {
        return new RuleBuilder()
          .denyAll("access Denied")
          .build();
      }

      if (isPractitioner) {
        ruleBase.addResourceIds(patients);
        ruleBase.addPractitioner(bearerId);
      } else {
        ruleBase.addResource(bearerId);
      }

      List<IAuthRule> rule;
      RequestTypeEnum operation = theRequestDetails.getRequestType();
      switch (operation){
        case TRACE:
        case TRACK:
        case HEAD:
        case CONNECT:
        case OPTIONS:
        case GET:
          rule = ruleBase.handleGet();
          break;
        case PUT:
        case DELETE:
        case PATCH:
        case POST:
          rule = ruleBase.handlePost();
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + operation);
      }

      return rule;

    } else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }

  private static List<IIdType> getPatientsList(IGenericClient client,String practitioner,String authHeader) {
    List<IIdType> patients = new ArrayList<>();
    Bundle patientBundle = (Bundle) client.search().forResource(Patient.class)
      .where(new ReferenceClientParam("general-practitioner").hasId(practitioner))
      .withAdditionalHeader("Authorization", authHeader)
      .execute();
    for (Bundle.BundleEntryComponent item: patientBundle.getEntry()){
      patients.add(item.getResource().getIdElement().toUnqualifiedVersionless());
    }

    return patients;
  }
}
