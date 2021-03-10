package ca.uhn.fhir.jpa.starter.Util;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    HttpClient httpClient = HttpClientBuilder.create().build();
    ctx.getRestfulClientFactory().setHttpClient(httpClient);

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
    var roles = getPractitionerRole(practitioner);
    var isAdmin = roles.stream().anyMatch(e ->
    {
      var identifier = e.getIdentifier();
      return identifier.stream().anyMatch(id -> id.getValue().equals("admin"));
    });

    return isAdmin;
  }

  public static IIdType getPractitionerOrganization(String practitioner){
    var roles = getPractitionerRole(practitioner);
    if (roles.isEmpty())
    {
      return null;
    }

    var role = roles.get(0);
    return role.getOrganization().getReferenceElement();
  }

  public static List<PractitionerRole> getPractitionerRole(String practitioner){
    Bundle role = (Bundle) client.search().forResource(PractitionerRole.class)
      .where(new ReferenceClientParam("practitioner").hasId(practitioner))
      .execute();

    var practitionerRoles = role.getEntry().stream()
      .map(e -> (PractitionerRole) e.getResource())
      .collect(Collectors.toList());

    return practitionerRoles;
  }

  public static List<IIdType> getAllInOrganization(String organizationId)
  {
    CacheControlDirective s = new CacheControlDirective();
    s.setNoStore(true);
    s.setNoCache(true);

    Bundle patientsList = (Bundle) client.search().forResource(Patient.class)
      .where(new ReferenceClientParam("organization").hasId(organizationId)).cacheControl(s)
      .execute();
   
    Bundle practitionerList = (Bundle) client.search().forResource(PractitionerRole.class)
      .where(new ReferenceClientParam("organization").hasId(organizationId)).cacheControl(s)
      .execute();

    var patientIds =
      patientsList.getEntry().stream().map(e -> e.getResource().getIdElement()).collect(Collectors.toList());

    var practitionerIds =
      practitionerList.getEntry().stream().map(e -> ((PractitionerRole) e.getResource()).getPractitioner().getReferenceElement()).collect(Collectors.toList());

    List<IIdType> ids = new ArrayList<>();
    ids.addAll(practitionerIds);
    ids.addAll(patientIds);

    return ids;
  }
}
