package ca.uhn.fhir.jpa.starter.db;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Search {
  private static IGenericClient client = null;
  private static IParser parser = null;
  static final String server;

  static {
    server = HapiProperties.getServerAddress();
  }

  public static void setParser(IParser parser) {
    Search.parser = parser;
  }

  public static IGenericClient getClient() {
    return client;
  }

  public static void setClient(IGenericClient client) {
    Search.client = client;
  }

  public static void setByContext(FhirContext ctx) {

    HttpClient httpClient = HttpClientBuilder.create().build();
    ctx.getRestfulClientFactory().setHttpClient(httpClient);

    client = ctx.newRestfulGenericClient(server);
    parser = ctx.newJsonParser();
    parser.setPrettyPrint(true);
  }

  public static List<IIdType> getDeviceMetrics(List<IIdType> patientIds, String authHeader) {
    List<IIdType> retVal = new ArrayList<>();
    List<IIdType> devices = getDevices(patientIds, authHeader);
    for (var itm : devices) {
      Bundle deviceMetricBundle = (Bundle) client.search().forResource(DeviceMetric.class)
        .where(new ReferenceClientParam("source").hasId(itm))
        .withAdditionalHeader("Authorization", authHeader)
        .execute();
      for (var itm2 : deviceMetricBundle.getEntry()) {
        retVal.add(itm2.getResource().getIdElement().toUnqualifiedVersionless());
      }
    }
    return retVal;
  }

  public static List<IIdType> getDeviceMetrics(String patientId, String authHeader) {
    List<IIdType> retVal = new ArrayList<>();
    List<IIdType> devices = getDevices(patientId, authHeader);
    for (var itm : devices) {
      Bundle deviceMetricBundle = (Bundle) client.search().forResource(DeviceMetric.class)
        .where(new ReferenceClientParam("source").hasId(itm))
        .withAdditionalHeader("Authorization", authHeader)
        .execute();
      for (var itm2 : deviceMetricBundle.getEntry()) {
        retVal.add(itm2.getResource().getIdElement().toUnqualifiedVersionless());
      }
    }
    return retVal;
  }

  public static List<IIdType> getDevices(List<IIdType> patientIds, String authHeader) {
    List<IIdType> retVal = new ArrayList<>();
    for (var id : patientIds) {
      Bundle deviceBundle = (Bundle) client.search().forResource(Device.class)
        .where(new ReferenceClientParam("patient").hasId(id))
        .withAdditionalHeader("Authorization", authHeader)
        .execute();
      for (var itm : deviceBundle.getEntry()) {
        retVal.add(itm.getResource().getIdElement().toUnqualifiedVersionless());
      }
    }
    return retVal;
  }

  public static List<IIdType> getDevices(String patientId, String authHeader) {
    List<IIdType> retVal = new ArrayList<>();
    Bundle deviceBundle = (Bundle) client.search().forResource(Device.class)
      .where(new ReferenceClientParam("patient").hasId(patientId))
      .withAdditionalHeader("Authorization", authHeader)
      .execute();
    for (var itm : deviceBundle.getEntry()) {
      retVal.add(itm.getResource().getIdElement().toUnqualifiedVersionless());
    }
    return retVal;
  }

  public static List<IIdType> getPatients(String practitioner, String authHeader) {
    List<IIdType> patients = new ArrayList<>();
    Bundle patientBundle = (Bundle) client.search().forResource(Patient.class)
      .where(new ReferenceClientParam("general-practitioner").hasId(practitioner))
      .withAdditionalHeader("Authorization", authHeader)
      .execute();
    for (Bundle.BundleEntryComponent item : patientBundle.getEntry()) {
      patients.add(item.getResource().getIdElement().toUnqualifiedVersionless());
    }
    return patients;
  }

  public static boolean isPractitionerAdmin(String practitioner, String authHeader) {
    Bundle role = (Bundle) client.search().forResource(PractitionerRole.class)
      .where(new ReferenceClientParam("practitioner").hasId(practitioner))
      .withAdditionalHeader("Authorization", authHeader)
      .execute();
    for (Bundle.BundleEntryComponent itm : role.getEntry()) {
      return ((PractitionerRole) itm.getResource()).getIdentifier().get(0).getValue().equals("admin");
    }
    return false;
  }

  public static List<String> getBundleTypes(RequestDetails theRequestDetails) {
    List<String> types = new ArrayList<>();

    String request = new String(theRequestDetails.loadRequestContents());
    Bundle bundle = parser.parseResource(Bundle.class, request);
    for (var itm : bundle.getEntry()) {
      Resource resource = itm.getResource();
      Bundle.BundleEntryRequestComponent request1 = itm.getRequest();
      if (resource != null) {
        types.add( resource.fhirType());
      } else if (request1 != null && request1.getMethod() == Bundle.HTTPVerb.GET) {
        return Arrays.stream(new String[]{"GET"}).collect(Collectors.toList());
      }
    }
    return types;
  }

  public static Patient getPatient(IIdType id){
    return client.read().resource(Patient.class).withId(id).execute();
  }

  public static Device getDevice(IIdType id){
    return client.read().resource(Device.class).withId(id).execute();
  }

  public static DeviceMetric getDeviceMetric(IIdType id) {
    return client.read().resource(DeviceMetric.class).withId(id).execute();
  }

  public static Observation getObservation(IIdType id) {
    return client.read().resource(Observation.class).withId(id).execute();
  }
}
