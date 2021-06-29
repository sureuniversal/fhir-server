package ca.uhn.fhir.jpa.starter.authorization.rules;

import ca.uhn.fhir.jpa.starter.Util.Search;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Media;

import java.util.List;

public class MediaRule extends PatientRules {

  public MediaRule()
  {
    this.type = Media.class;
    this.denyMessage = "Media Rule";
  }

  @Override
  public List<IAuthRule> handlePost() {
    String errorMsg = "User has no organization!";
    IIdType userOrganization = this.GetUserOrganization();
    if (this.requestResource != null && userOrganization != null)
    {
      Media resource = (Media) this.requestResource;
      if (resource.getSubject() == null || !resource.getSubject().hasReference())
      {
        return new RuleBuilder().denyAll("Subject is missing from the body").build();
      }

      var subjectId = resource.getSubject().getReferenceElement().getIdPart();
      var subjectOrg = Search.getPatientOrganization(subjectId);
      if (subjectOrg != null && subjectOrg.hasIdPart() && subjectOrg.getIdPart().compareTo(userOrganization.getIdPart()) == 0)
      {
        return new RuleBuilder().allowAll().build();
      }

      errorMsg = "This user can not add media for the specified subject";
    }

    return new RuleBuilder().denyAll(errorMsg).build();
  }
}
