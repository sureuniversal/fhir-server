package ca.uhn.fhir.jpa.starter.Util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;

public class Search {
  protected static IGenericClient client = null;
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

  public static List<IIdType> getDevices(List<IIdType> patientIds) {
    List<IIdType> retVal = new ArrayList<>();
    var ids = new ArrayList<String>();
    for (var id : patientIds) {
      ids.add(id.getIdPart());
    }

    Bundle deviceBundle = (Bundle) client.search().forResource(Device.class)
      .where(new ReferenceClientParam("patient").hasAnyOfIds(ids))
      .execute();

    for (var itm : deviceBundle.getEntry()) {
      retVal.add(itm.getResource().getIdElement().toUnqualifiedVersionless());
    }

    return retVal;
  }

  public static List<IIdType> getDeviceMetricsForDeviceList(List<IIdType> deviceIds)
  {
    List<IIdType> retVal = new ArrayList<>();
    var ids = new ArrayList<String>();
    for (var id : deviceIds) {
      ids.add(id.getIdPart());
    }

    Bundle deviceBundle = (Bundle) client.search().forResource(DeviceMetric.class)
      .where(new ReferenceClientParam("source").hasAnyOfIds(ids))
      .execute();

    for (var itm : deviceBundle.getEntry()) {
      retVal.add(itm.getResource().getIdElement().toUnqualifiedVersionless());
    }

    return retVal;
  }

  public static List<IIdType> getPatients(String practitionerId) {
    List<IIdType> patients = new ArrayList<>();
    Bundle patientBundle = (Bundle) client.search().forResource(Patient.class)
      .where(new ReferenceClientParam("general-practitioner").hasId(practitionerId))
      .execute();
    for (Bundle.BundleEntryComponent item : patientBundle.getEntry()) {
      patients.add(item.getResource().getIdElement().toUnqualifiedVersionless());
    }
    return patients;
  }


  public static boolean isPractitionerAdmin(String practitioner){
    Bundle role = (Bundle) client.search().forResource(PractitionerRole.class)
      .where(new ReferenceClientParam("practitioner").hasId(practitioner))
      .execute();

    for (Bundle.BundleEntryComponent itm : role.getEntry()) {
      var identifiers = ((PractitionerRole)itm.getResource()).getIdentifier();
      for (var identifier : identifiers)
      {
        if (identifier.getValue().equals("admin"))
        {
          return true;
        }
      }
    }

    return false;
  }
}
