package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.AuthorizationRules.DeviceRules;
import ca.uhn.fhir.jpa.starter.AuthorizationRules.ObservationRules;
import ca.uhn.fhir.jpa.starter.AuthorizationRules.PatientRule;
import ca.uhn.fhir.jpa.starter.AuthorizationRules.RuleBase;
import ca.uhn.fhir.jpa.starter.oauth.Utils;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bson.Document;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

    // Why do we need this
    URL myUrl;
    try {
      myUrl = new URL(theRequestDetails.getCompleteUrl());
    } catch (MalformedURLException e) {
      return new RuleBuilder()
        .denyAll("Malformed URL")
        .build();
    }

    if(myUrl.getPort()==8080) {
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

    Document userDoc = Utils.GetUserByToken(token);

    if (userDoc != null) {
      String bearerId = userDoc.getString("_id");
      FhirContext ctx = theRequestDetails.getFhirContext();

      IGenericClient client = ctx.newRestfulGenericClient(theRequestDetails.getFhirServerBase());

      IAuthRuleBuilder ruleBuilder;
      Boolean isPractitioner = userDoc.getBoolean("isPractitioner");
      if(isPractitioner == null) isPractitioner = false;
      List<String> patients = isPractitioner ? getPatientsList(client,bearerId,authHeader):new ArrayList<>();

      var ruleBase = this.GetRuleBuilder(theRequestDetails);
      if  (ruleBase == null)
      {
        return new RuleBuilder()
          .denyAll("access Denied")
          .build();
      }

      if (isPractitioner)
      {
        ruleBase.addResourceIds(patients);
      }
      else
      {
        ruleBase.addResource(bearerId);
      }

      List<IAuthRule> rule;
      var operation = theRequestDetails.getOperation();
      if (operation.compareTo("Get") == 0)
      {
        rule = ruleBase.HandleGet();
      }
      else
      {
        rule = ruleBase.HandlePost();
      }

      return rule;





      String[] path = myUrl.getPath().substring(myUrl.getPath().indexOf("/fhir/")+"/fhir/".length()).split("/");
      if(!isQuery(myUrl.getQuery())){ //restful (no query(?…))
        if(path.length < 2){ //plain (no extra /)
          switch (path[0]){
            case "metadata":
              return new RuleBuilder().allow("metadata").metadata().build();
            case "PractitionerRole":
              if(isPractitioner){
                return new RuleBuilder()
                  .allow().read().resourcesOfType("Practitioner").inCompartment("Practitioner", new IdType("Practitioner", bearerId)).andThen()
                  .allow().write().resourcesOfType("Practitioner").inCompartment("Practitioner",new IdType("Practitioner", bearerId)).andThen()
                  .allow().metadata().andThen()
                  .allow().patch().allRequests().andThen()
                  .denyAll("PractitionerRole")
                  .build();
              } else {
                return new RuleBuilder()
                  .denyAll("not a practitioner")
                  .build();
              }
            default:
              if(isPractitioner){
                try {
                  ruleBuilder = new RuleBuilder();
                  Method deviceRulesMethod = this.getClass().getMethod("deviceRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
                  practitionerPatientsRules(patients,deviceRulesMethod, ruleBuilder,client,authHeader);
                  Method patientRulesMethod = this.getClass().getMethod("patientRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
                  practitionerPatientsRules(patients,patientRulesMethod, ruleBuilder,client,authHeader);
                  practitionerRules(ruleBuilder,client,bearerId,authHeader);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                  return new RuleBuilder()
                    .denyAll("Exception:"+ e.getMessage())
                    .build();
                }
              } else {
                ruleBuilder = deviceMetricRules(new RuleBuilder(),client,bearerId,authHeader);
              }
              return ruleBuilder
                .allow().metadata().andThen()
                .allow().patch().allRequests().andThen()
                .denyAll("unknown resource")
                .build();
          }
        }
        switch (path[0]){
          case "Practitioner":
            String practitionerId = path[1];
            if (practitionerId.equals(bearerId)){
              return new RuleBuilder()
                .allow().read().resourcesOfType("Practitioner").inCompartment("Practitioner", new IdType("Practitioner", practitionerId)).andThen()
                .allow().write().resourcesOfType("Practitioner").inCompartment("Practitioner",new IdType("Practitioner", practitionerId)).andThen()
                .allow().metadata().andThen()
                .allow().patch().allRequests().andThen()
                .denyAll("Practitioner")
                .build();
            } else {
              return new RuleBuilder()
                .denyAll("not a practitioner")
                .build();
            }
          case "Observation":
            String observationId = path[1];
            Observation observation = client.read().resource(Observation.class).withId(observationId).execute();
            String obsPatientId = observation.getSubject().getReference().replace("Patient/","");
            if (obsPatientId.equals(bearerId)||patients.contains(obsPatientId)) {
              return new RuleBuilder()
                .allow().read().resourcesOfType("Observation").inCompartment("Observation", observation.getIdElement().toUnqualifiedVersionless()).andThen()
                .allow().write().resourcesOfType("Observation").inCompartment("Observation", observation.getIdElement().toUnqualifiedVersionless()).andThen()
                .allow().metadata().andThen()
                .allow().patch().allRequests().andThen()
                .denyAll("Observation")
                .build();
            }
          case "Patient":
            String patientId = path[1];
            if (patientId.equals(bearerId)||patients.contains(patientId)){
              return new RuleBuilder()
                .allow().read().resourcesOfType("Patient").inCompartment("Patient", new IdType("Patient", patientId)).andThen()
                .allow().write().resourcesOfType("Patient").inCompartment("Patient",new IdType("Patient", patientId)).andThen()
                .allow().metadata().andThen()
                .allow().patch().allRequests().andThen()
                .denyAll("Patient can only access himself")
                .build();
            }
          case "DeviceMetric":
            String deviceMetricId = path[1];
            DeviceMetric deviceMetric = client.read().resource(DeviceMetric.class).withId(deviceMetricId).execute();
            Device metDevice = client.read().resource(Device.class).withId(deviceMetric.getSource().getReference()).execute();
            String metPatientId = metDevice.getPatient().getReference().replace("Patient/","");
            if (metPatientId.equals(bearerId)||patients.contains(metPatientId)){
              return new RuleBuilder()
                .allow().read().resourcesOfType("DeviceMetric").inCompartment("DeviceMetric", deviceMetric.getIdElement().toUnqualifiedVersionless() ).andThen()
                .allow().write().resourcesOfType("DeviceMetric").inCompartment("DeviceMetric",deviceMetric.getIdElement().toUnqualifiedVersionless() ).andThen()
                .allow().metadata().andThen()
                .allow().patch().allRequests().andThen()
                .denyAll("DeviceMetric")
                .build();
            } else {
              return new RuleBuilder()
                .denyAll("DeviceMetric does not belong to patient")
                .build();
            }
          case "Device":
            String deviceId = path[1];
            Device device = client.read().resource(Device.class).withId(deviceId).execute();
            String devPatientId = device.getPatient().getReference().replace("Patient/","");
            if (devPatientId.equals(bearerId)||patients.contains(devPatientId)){
              return new RuleBuilder()
                .allow().read().resourcesOfType("Device").inCompartment("Device", device.getIdElement().toUnqualifiedVersionless() ).andThen()
                .allow().write().resourcesOfType("Device").inCompartment("Device",device.getIdElement().toUnqualifiedVersionless() ).andThen()
                .allow().metadata().andThen()
                .allow().patch().allRequests().andThen()
                .denyAll("Device")
                .build();
            } else {
              return new RuleBuilder()
                .denyAll("Device does not belong to patient")
                .build();
            }
          default:
            if(isPractitioner){
              try {
                ruleBuilder = new RuleBuilder();
                Method deviceRulesMethod = this.getClass().getMethod("deviceRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
                practitionerPatientsRules(patients,deviceRulesMethod, ruleBuilder,client,authHeader);
                Method patientRulesMethod = this.getClass().getMethod("patientRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
                practitionerPatientsRules(patients,patientRulesMethod, ruleBuilder,client,authHeader);
                practitionerRules(ruleBuilder,client,bearerId,authHeader);
              } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                return new RuleBuilder()
                  .denyAll("Exception:"+ e.getMessage())
                  .build();
              }
            } else {
              ruleBuilder = deviceMetricRules(new RuleBuilder(),client,bearerId,authHeader);
            }
            return ruleBuilder
              .allow().metadata().andThen()
              .allow().patch().allRequests().andThen()
              .denyAll("unknown resource")
              .build();
        }
      }
      switch (path[0]){ //query
        case "Practitioner":
        case "Observation":
        case "Patient":
        case "PractitionerRole":
          if(isPractitioner){
            try {
              Method patientRulesMethod = this.getClass().getMethod("patientRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
              ruleBuilder = practitionerPatientsRules(patients,patientRulesMethod, new RuleBuilder(),client,authHeader);
              practitionerRules(ruleBuilder,client,bearerId,authHeader);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
              return new RuleBuilder()
                .denyAll("Exception:"+ e.getMessage())
                .build();
            }
          } else {
            ruleBuilder = patientRules(new RuleBuilder(),client,bearerId,authHeader);
          }
          return ruleBuilder
            .allow().metadata().andThen()
            .allow().patch().allRequests().andThen()
            .denyAll("Patient can only access himself")
            .build();
        case "DeviceMetric":
          if(isPractitioner){
            try {
              Method deviceMetricRulesMethod = this.getClass().getMethod("deviceMetricRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
              ruleBuilder = practitionerPatientsRules(patients,deviceMetricRulesMethod, new RuleBuilder(),client,authHeader);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
              return new RuleBuilder()
                .denyAll("Exception:"+ e.getMessage())
                .build();
            }
          } else {
            ruleBuilder = deviceMetricRules(new RuleBuilder(),client,bearerId,authHeader);
          }
          return ruleBuilder
            .allow().metadata().andThen()
            .allow().patch().allRequests().andThen()
            .denyAll("DeviceMetric")
            .build();
        case "Device":
          if(isPractitioner){
            try {
              Method deviceRulesMethod = this.getClass().getMethod("deviceRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
              ruleBuilder = practitionerPatientsRules(patients,deviceRulesMethod, new RuleBuilder(),client,authHeader);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
              return new RuleBuilder()
                .denyAll("Exception:"+ e.getMessage())
                .build();
            }
          } else {
            ruleBuilder = deviceRules(new RuleBuilder(),client,bearerId,authHeader);
          }
          return ruleBuilder
            .allow().metadata().andThen()
            .allow().patch().allRequests().andThen()
            .denyAll("Device")
            .build();
        default:
          if(isPractitioner){
            try {
              ruleBuilder = new RuleBuilder();
              Method deviceRulesMethod = this.getClass().getMethod("deviceRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
              practitionerPatientsRules(patients,deviceRulesMethod, ruleBuilder,client,authHeader);
              Method patientRulesMethod = this.getClass().getMethod("patientRules",IAuthRuleBuilder.class,IGenericClient.class,String.class,String.class);
              practitionerPatientsRules(patients,patientRulesMethod, ruleBuilder,client,authHeader);
              practitionerRules(ruleBuilder,client,bearerId,authHeader);
            } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
              return new RuleBuilder()
                .denyAll("Exception:"+ e.getMessage())
                .build();
            }
          } else {
            ruleBuilder = deviceMetricRules(new RuleBuilder(),client,bearerId,authHeader);
          }
          return ruleBuilder
            .allow().metadata().andThen()
            .allow().patch().allRequests().andThen()
            .denyAll("unknown resource")
            .build();
      }
    } else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }

  private RuleBase GetRuleBuilder(RequestDetails theRequestDetails)
  {
    var compartmentName = theRequestDetails.getCompartmentName();
    switch (compartmentName)
    {
      case "Patient": return new PatientRule();
      case  "Device": return new DeviceRules();
    }

    return null;
  }

  private static boolean isQuery(String query){
    if (query == null)
      return false;
    for (String itm :
      query.split("&")) {
      if(itm.equals("")) continue;
      if (itm.charAt(0) != '_')
        return true;
    }
    return false;
  }

  private static List<String> getPatientsList(IGenericClient client,String practitioner,String authHeader) {
    List<String> patients = new ArrayList<>();
    Bundle patientBundle = (Bundle) client.search().forResource(Patient.class)
      .where(new ReferenceClientParam("general-practitioner").hasId(practitioner))
      .withAdditionalHeader("Authorization", authHeader)
      .execute();
    for (Bundle.BundleEntryComponent item: patientBundle.getEntry()){
      patients.add(item.getResource().getIdElement().getIdPart());
    }

    return patients;
  }

  private static IAuthRuleBuilder practitionerPatientsRules(List<String> patients, Method method, IAuthRuleBuilder ruleBuilder, IGenericClient client, String authHeader) throws InvocationTargetException, IllegalAccessException {
    for (String patient : patients) {
      method.invoke(null,ruleBuilder,client,patient,authHeader);
    }
    return ruleBuilder;
  }

  private static IAuthRuleBuilder practitionerRules(IAuthRuleBuilder ruleBuilder,IGenericClient client,String practitioner,String authHeader){
    IIdType userIdPractitionerId = new IdType("Practitioner",practitioner);

    return ruleBuilder
      .allow().read().allResources().inCompartment("Practitioner", userIdPractitionerId).andThen()
      .allow().write().allResources().inCompartment("Practitioner", userIdPractitionerId).andThen();
  }
  public static IAuthRuleBuilder patientRules(IAuthRuleBuilder ruleBuilder,IGenericClient client,String patient,String authHeader){
    IIdType userIdPatientId = new IdType("Patient", patient);
    return ruleBuilder
      .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
      .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen();
  }
  public static IAuthRuleBuilder deviceRules(IAuthRuleBuilder ruleBuilder,IGenericClient client,String patient,String authHeader){
    Bundle deviceBundle = (Bundle)client.search().forResource(Device.class)
      .where(new ReferenceClientParam("patient").hasId(patient))
      .withAdditionalHeader("Authorization", authHeader)
      .prettyPrint()
      .execute();
    for (Bundle.BundleEntryComponent item2: deviceBundle.getEntry()){
      Resource resource2 = item2.getResource();
      IIdType userIdDeviceId = new IdType("Device", resource2.getIdElement().getIdPart());
      ruleBuilder
        .allow().read().resourcesOfType("Device").inCompartment("Device", userIdDeviceId).andThen()
        .allow().write().resourcesOfType("Device").inCompartment("Device", userIdDeviceId);
    }
    return ruleBuilder;
  }

  public static IAuthRuleBuilder deviceMetricRules(IAuthRuleBuilder ruleBuilder,IGenericClient client,String patient,String authHeader) {
    Bundle deviceBundle = (Bundle)client.search().forResource(Device.class)
      .where(new ReferenceClientParam("patient").hasId(patient))
      .withAdditionalHeader("Authorization", authHeader)
      .prettyPrint()
      .execute();
    for (Bundle.BundleEntryComponent item2: deviceBundle.getEntry()){
      Resource resource2 = item2.getResource();
      IIdType userIdDeviceId = new IdType("Device", resource2.getIdElement().getIdPart());

      Bundle deviceMetricBundle = (Bundle) client.search().forResource(DeviceMetric.class)
        .where(new ReferenceClientParam("source").hasId(userIdDeviceId))
        .prettyPrint()
        .execute();
      for (Bundle.BundleEntryComponent item3: deviceMetricBundle.getEntry()){
        Resource resource3 = item3.getResource();
        IIdType userIdDeviceMetricId = new IdType("DeviceMetric", resource3.getIdElement().getIdPart());
        ruleBuilder
          .allow().read().resourcesOfType("DeviceMetric").inCompartment("DeviceMetric", userIdDeviceMetricId).andThen()
          .allow().write().resourcesOfType("DeviceMetric").inCompartment("DeviceMetric", userIdDeviceMetricId);
      }
      ruleBuilder
        .allow().read().resourcesOfType("Device").inCompartment("Device", userIdDeviceId).andThen()
        .allow().write().resourcesOfType("Device").inCompartment("Device", userIdDeviceId);
    }
    return ruleBuilder;
  }
}
