package ca.uhn.fhir.jpa.starter.db;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;

public class Search {
  private static IGenericClient client = null;
  static final String server;

  static {
    server = HapiProperties.getServerAddress();
  }

  public static IGenericClient getClient() {
    return client;
  }

  public static void setClient(IGenericClient client) {
    Search.client = client;
  }

  public static void setClientByContext(FhirContext ctx) {
    Search.setClient(ctx.newRestfulGenericClient(server));
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
  public static boolean isPractitionerAdmin(String practitioner, String authHeader){
    Bundle role =(Bundle) client.search().forResource(PractitionerRole.class)
      .where(new ReferenceClientParam("practitioner").hasId(practitioner))
      .withAdditionalHeader("Authorization", authHeader)
      .execute();
    for (Bundle.BundleEntryComponent itm : role.getEntry()) {
      return ((PractitionerRole)itm.getResource()).getIdentifier().get(0).getValue().equals("admin");
    }
    return false;
  }
}
