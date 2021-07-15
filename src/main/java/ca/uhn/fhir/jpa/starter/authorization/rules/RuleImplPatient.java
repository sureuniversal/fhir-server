package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationFlagsEnum;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor.Verdict;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.IRuleApplier;
import ca.uhn.fhir.rest.server.interceptor.auth.PolicyEnum;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RuleImplPatient implements IAuthRule {

  String name;
  List<IIdType> patients;
  List<IIdType> practitioners;
  IIdType myId;
  IIdType myOrganization;
  List<IIdType> devices= new ArrayList<>();
  boolean isPractitioner;
  UserType myType;

  public RuleImplPatient(String name, IIdType id, boolean isPractitioner,UserType userType) {
    this.name = name;
    patients = new ArrayList<>();
    practitioners = new ArrayList<>();
    this.myId = id;
    this.isPractitioner = isPractitioner;
    if(isPractitioner) {
      practitioners.add(id);
    }else{
      patients.add(id);
    }
    myOrganization = Search.getOrganization(id);
    myType =userType;

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
        if(theInputResource.getClass() == Patient.class)  return patientUpdateDecision(theInputResource);
        return resourceDecision(theInputResource);
      case HISTORY_INSTANCE:
      case HISTORY_TYPE:
      case SEARCH_SYSTEM:
      case HISTORY_SYSTEM:
      case READ:
      case VREAD:
      case GET_PAGE:
        if (theOutputResource == null) {
          return new Verdict(PolicyEnum.ALLOW, this);
        } else {
          return resourceDecision(theOutputResource);
        }
      case SEARCH_TYPE:
        if (theOutputResource == null) {
          return new Verdict(PolicyEnum.ALLOW, this);
          //return searchDecision(theRequestDetails);
        } else {
          return resourceDecision(theOutputResource);
        }
      case TRANSACTION:
      case METADATA:
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
      case "class org.hl7.fhir.r4.model.CareTeam":
        return patientDecision(((CareTeam) resource).getSubject().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.Device":
        return patientDecision(((Device) resource).getPatient().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.DeviceMetric":
        return deviceDecision(((DeviceMetric) resource).getSource().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.Patient":
        return patientDecision(resource.getIdElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.Observation":
        return patientDecision(((Observation) resource).getSubject().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.Practitioner":
          return practitionerDecision(resource.getIdElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.PractitionerRole":
        return practitionerDecision(((PractitionerRole)resource).getPractitioner().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.Organization":
        return organizationDecision(resource.getIdElement().toUnqualifiedVersionless().getIdPart());
      case "class org.hl7.fhir.r4.model.Flag":
        return patientDecision(((Flag) resource).getSubject().getReferenceElement().toUnqualifiedVersionless());
      case "class org.hl7.fhir.r4.model.DiagnosticReport":
        return patientDecision(((DiagnosticReport) resource).getSubject().getReferenceElement().toUnqualifiedVersionless());
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
      case "Organization":
        return organizationDecision(id.toUnqualifiedVersionless().getIdPart());
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
          if(theRequestDetails.getParameters().get("general-practitioner") != null) {
            return practitionerDecision(theRequestDetails.getParameters().get("general-practitioner")[0]);
          }
          return new Verdict(PolicyEnum.ALLOW,this);
        case "CareTeam":
          if(theRequestDetails.getParameters().get("participant") != null) {
            return patientPractitionerDecision(theRequestDetails.getParameters().get("participant")[0].split(","));
          }
        case "Organization":
        case "Flag":
        default:
          return patientDecision(theRequestDetails.getParameters().get("subject")[0].split(","));
      }
    } catch (Exception e) {
      return null;
    }
  }

  Verdict patientDecision(IIdType id) {
    if (patients.contains(id)) {
      return new Verdict(PolicyEnum.ALLOW, this);
    } else {
      if((isPractitioner && Search.isPractitionerHasPatient(myId, id))||
        (myType == UserType.organizationAdmin && Search.isInOrganization(id,myOrganization))||
        Search.isInCareTeam(myId,id)) {
        patients.add(id.toUnqualifiedVersionless());
        return new Verdict(PolicyEnum.ALLOW, this);
      }
      return null;
    }
  }

  Verdict organizationDecision(IIdType id) {
    if(myOrganization == null){
      myOrganization = Search.getOrganization(id);
    }
    if (myOrganization == id)
      return new Verdict(PolicyEnum.ALLOW,this);
    else
      return null;
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
    try {
      DeviceMetric deviceMetric = Search.getDeviceMetric(id);
      if (deviceMetric == null) return null;
      if (deviceDecision(deviceMetric.getSource().getReferenceElement()) != null) {
        return new Verdict(PolicyEnum.ALLOW, this);
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  Verdict observationDecision(IIdType id) {
    try {
      Observation observation = Search.getObservation(id);
      if (observation == null) return null;
      if (patientDecision(observation.getSubject().getReferenceElement()) != null) {
        return new Verdict(PolicyEnum.ALLOW, this);
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  Verdict practitionerDecision(IIdType id) {
    if (practitioners.contains(id)) {
      return new Verdict(PolicyEnum.ALLOW, this);
    } else {
      if((myType == UserType.organizationAdmin && Search.isInOrganization(id,myOrganization))||
        Search.isInCareTeam(myId,id)) {
        practitioners.add(id.toUnqualifiedVersionless());
        return new Verdict(PolicyEnum.ALLOW, this);
      }
      return null;
    }
  }


  Verdict patientDecision(String id) {
    return patientDecision(new IdType("Patient", id.replace("Patient/","")));
  }
  Verdict patientDecision(String[] ids) {
    for (String id :
      ids) {
      if(patientDecision(id) == null){
        return null;
      }
    }
    return new Verdict(PolicyEnum.ALLOW, this);
  }

  Verdict practitionerDecision(String id) {
    return practitionerDecision(new IdType("Practitioner", id.replace("Practitioner/", "")));
  }

  Verdict patientPractitionerDecision(String id) {
    if (practitioners.contains(new IdType("Practitioner", id))||
      patients.contains(new IdType("Patient", id))) {
      return new Verdict(PolicyEnum.ALLOW, this);
    } else {
      Verdict verdict = patientDecision(id);
      if (verdict != null) {
        return verdict;
      }
      return practitionerDecision(id);
    }
  }

  Verdict patientPractitionerDecision(String[] ids) {
    for (String id :
      ids) {
      if(patientPractitionerDecision(id) == null){
        return null;
      }
    }
    return new Verdict(PolicyEnum.ALLOW, this);
  }

  Verdict organizationDecision(String id) {
    return organizationDecision(new IdType("Organization", id.replace("Organization/","")));
  }
  Verdict deviceDecision(String id) {
    return deviceDecision(new IdType("Device",id.replace("Device/","")));
  }

  String resourceNameRequest(RequestDetails theRequestDetails) {
    return theRequestDetails.getResourceName();
  }

  Verdict patientUpdateDecision(IBaseResource patient){
    if(resourceDecision(patient) != null
      || !Search.isPatientExists(patient.getIdElement().toUnqualifiedVersionless())){
      return new Verdict(PolicyEnum.ALLOW, this);
    }
    return null;
  }

}
