package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationFlagsEnum;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor.Verdict;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IRuleApplier;
import ca.uhn.fhir.rest.server.interceptor.auth.PolicyEnum;
import com.mongodb.BasicDBObjectBuilder;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RuleImplPatient implements IAuthRule {

  String name;
  List<IIdType> patients;
  IIdType practitioner;
  List<IIdType> devices= new ArrayList<>();
  List<IIdType> deviceMetrics= new ArrayList<>();
  List<IIdType> observations= new ArrayList<>();


  public RuleImplPatient(String name, List<IIdType> patients, IIdType practitioner) {
    this.name = name;
    if(patients == null){
      this.patients = new ArrayList<>();
    } else {
      this.patients = patients;
    }
    this.practitioner = practitioner;
  }

  public RuleImplPatient(String name, IIdType patient) {
    List<IIdType> patients2 = new ArrayList<>();
    patients2.add(patient);
    this.name = name;
    this.patients = patients2;
    this.practitioner = null;
  }

  public RuleImplPatient(String name, String practitionerId, String auth) {
    this.name = name;
    patients = Search.getPatients(practitionerId, auth);
    practitioner = new IdType("Practitioner", practitionerId);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Verdict applyRule(RestOperationTypeEnum theOperation, RequestDetails theRequestDetails, IBaseResource theInputResource, IIdType theInputResourceId,
                           IBaseResource theOutputResource, IRuleApplier theRuleApplier, Set<AuthorizationFlagsEnum> theFlags, Pointcut thePointcut) {
    switch (thePointcut){
      case STORAGE_PRESHOW_RESOURCES:
      case SERVER_INCOMING_REQUEST_PRE_HANDLED:
      case SERVER_OUTGOING_RESPONSE:
      case STORAGE_CASCADE_DELETE:
        break;
      case STORAGE_PRESTORAGE_RESOURCE_CREATED:
      case STORAGE_PRESTORAGE_RESOURCE_UPDATED:
      case STORAGE_PRESTORAGE_RESOURCE_DELETED:
        return new Verdict(PolicyEnum.ALLOW, this);
    }
    switch (theOperation) {
      case ADD_TAGS:
      case DELETE_TAGS:
      case GET_TAGS:
        // These are DSTU1 operations and not relevant
        break;
      case GRAPHQL_REQUEST:
      case EXTENDED_OPERATION_SERVER:
      case EXTENDED_OPERATION_TYPE:
      case EXTENDED_OPERATION_INSTANCE:
        break;
      case DELETE:
        return idDecision(theInputResourceId);
      case PATCH:
      case CREATE:
      case UPDATE:
        return resourceDecision(theInputResource);
      case HISTORY_INSTANCE:
      case HISTORY_TYPE:
      case SEARCH_SYSTEM:
      case HISTORY_SYSTEM:
      case READ:
      case VREAD:
        if (theOutputResource == null) {
          return new Verdict(PolicyEnum.ALLOW, this);
        } else {
          return resourceDecision(theOutputResource);
        }
      case SEARCH_TYPE:
        return searchDecision(theRequestDetails);
      case TRANSACTION:
        return null;
      case METADATA:
      case GET_PAGE:
        return new Verdict(PolicyEnum.ALLOW, this);
      case VALIDATE:
      case META_ADD:
      case META:
      case META_DELETE:
        break;
    }
    return null;
  }

  Verdict resourceDecision(IBaseResource resource) {
    switch (resource.getClass().toString()) {
      case "class org.hl7.fhir.r4.model.Device":
        return patientDecision(((Device) resource).getPatient().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.DeviceMetric":
        return deviceDecision(((DeviceMetric) resource).getSource().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.Patient":
        if(practitioner == null) {
          return patientDecision(resource.getIdElement().toUnqualifiedVersionless());
        } else {
          return practitionerDecision(((Patient) resource).getGeneralPractitionerFirstRep().getReferenceElement().toUnqualifiedVersionless());
        }
      case "class org.hl7.fhir.r4.model.Observation":
        return patientDecision(((Observation) resource).getSubject().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.Practitioner":
          return practitionerDecision(resource.getIdElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.PractitionerRole":
        return practitionerDecision(((PractitionerRole)resource).getPractitioner().getReferenceElement().toUnqualifiedVersionless());
    }
    return null;
  }

  Verdict idDecision(IIdType id){
    switch (id.getResourceType()) {
      case "Device":
        return deviceDecision(id.toUnqualifiedVersionless());
      case "DeviceMetric":
        return deviceMetricDecision(id.toUnqualifiedVersionless());
      case "Patient":
        return patientDecision(id.toUnqualifiedVersionless());
      case "Observation":
        return observationDecision(id.toUnqualifiedVersionless());
      case "Practitioner":
        return practitionerDecision(id.toUnqualifiedVersionless());
      default:
        return null;
    }
  }

  Verdict searchDecision(RequestDetails theRequestDetails) {
    try {
      switch (resourceNameRequest(theRequestDetails)) {
        case "Device":
          return patientDecision(theRequestDetails.getParameters().get("patient")[0]);
        case "DeviceMetric":
          return deviceDecision(theRequestDetails.getParameters().get("source")[0]);
        case "PractitionerRole":
          return practitionerDecision(theRequestDetails.getParameters().get("practitioner")[0]);
        case "Patient":
          return practitionerDecision(theRequestDetails.getParameters().get("general-practitioner")[0]);
        default:
          return patientDecision(theRequestDetails.getParameters().get("subject")[0]);
      }
    } catch (Exception e) {
      return null;
    }
  }

  Verdict patientDecision(IIdType id) {
    if (patients.contains(id)) {
      return new Verdict(PolicyEnum.ALLOW, this);
    } else {
      if(practitioner != null) {
        try {
          Patient patient = Search.getPatient(id);
          if (patient == null) return null;
          if (patient.getGeneralPractitionerFirstRep().getReferenceElement().equals(practitioner)) {
            patients.add(patient.getIdElement().toUnqualifiedVersionless());
            return new Verdict(PolicyEnum.ALLOW, this);
          }
        } catch (Exception e) {
          return null;
        }
      }
      return null;
    }
  }


  Verdict deviceDecision(IIdType id) {
    if (devices.contains(id)) {
      return new Verdict(PolicyEnum.ALLOW, this);
    } else {
      try {
        Device device = Search.getDevice(id);
        if (device == null) return null;
        if (patientDecision(device.getPatient().getReferenceElement()) != null) {
          devices.add(device.getIdElement().toUnqualifiedVersionless());
          return new Verdict(PolicyEnum.ALLOW, this);
        }
      } catch (Exception e) {
        return null;
      }
      return null;
    }
  }

  Verdict deviceMetricDecision(IIdType id) {
    if (deviceMetrics.contains(id)) {
      return new Verdict(PolicyEnum.ALLOW, this);
    } else {
      try {
        DeviceMetric deviceMetric = Search.getDeviceMetric(id);
        if (deviceMetric == null) return null;
        if (deviceDecision(deviceMetric.getSource().getReferenceElement()) != null) {
          deviceMetrics.add(deviceMetric.getIdElement().toUnqualifiedVersionless());
          return new Verdict(PolicyEnum.ALLOW, this);
        }
      } catch (Exception e) {
        return null;
      }
      return null;
    }
  }

  Verdict observationDecision(IIdType id) {
    if (observations.contains(id)) {
      return new Verdict(PolicyEnum.ALLOW, this);
    } else {
      try {
        Observation observation = Search.getObservation(id);
        if (observation == null) return null;
        if (patientDecision(observation.getSubject().getReferenceElement()) != null) {
          observations.add(observation.getIdElement().toUnqualifiedVersionless());
          return new Verdict(PolicyEnum.ALLOW, this);
        }
      } catch (Exception e) {
        return null;
      }
      return null;
    }
  }

  Verdict practitionerDecision(IIdType id) {
    if(practitioner != null && practitioner.equals(id)) {
      return new Verdict(PolicyEnum.ALLOW, this);
    }
    return null;
  }


  Verdict patientDecision(String id) {
    return patientDecision(new IdType("Patient", id.replace("Patient/","")));
  }

  Verdict practitionerDecision(String id) {
    return practitionerDecision(new IdType("Practitioner", id.replace("Practitioner/","")));
  }
  Verdict deviceDecision(String id) {
    return deviceDecision(new IdType("Device",id.replace("Device/","")));
  }

  String resourceNameRequest(RequestDetails theRequestDetails) {
    return theRequestDetails.getResourceName();
  }
}
