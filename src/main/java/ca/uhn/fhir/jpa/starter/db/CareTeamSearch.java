package ca.uhn.fhir.jpa.starter.db;

import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CareTeam;

import java.util.ArrayList;
import java.util.List;

public class CareTeamSearch extends Search{
  public static List<IIdType> GetAllowedCareTeamsForUser(String id)
  {
    List<IIdType> allowedAsParticipant = getAllowedCareTeamAsParticipant(id);
    List<IIdType> allowedAsSubject = getAllowedCareTeamAsSubject(id);

    ArrayList<IIdType> result = new ArrayList<IIdType>();
    result.addAll(allowedAsParticipant);
    result.addAll(allowedAsSubject);

    return result;
  }

  public static List<IIdType> getAllowedCareTeamAsParticipant(String id){
    List<IIdType> retVal = new ArrayList<>();
    Bundle careTeamBundle = (Bundle) client.search().forResource(CareTeam.class)
      .where(new ReferenceClientParam("participant").hasId(id))
      .execute();

    for (var item : careTeamBundle.getEntry()) {
      retVal.add(item.getResource().getIdElement().toUnqualifiedVersionless());
    }

    return retVal;
  }

  public static List<IIdType> getAllowedCareTeamAsSubject(String id){
    List<IIdType> retVal = new ArrayList<>();
    Bundle careTeamBundle = (Bundle) client.search().forResource(CareTeam.class)
      .where(new ReferenceClientParam("subject").hasId(id))
      .execute();

    for (var item : careTeamBundle.getEntry()) {
      retVal.add(item.getResource().getIdElement().toUnqualifiedVersionless());
    }

    return retVal;
  }

  public static List<IIdType> getAllUsersInCareTeams(List<String> ids){
    List<IIdType> retVal = new ArrayList<>();
    Bundle bundle = (Bundle) client.search().forResource(CareTeam.class)
      .where(new ReferenceClientParam("_id").hasAnyOfIds(ids))
      .execute();

    for (var item : bundle.getEntry()) {
      CareTeam careTeam = (CareTeam) item.getResource();
      IIdType subjectId = careTeam.getSubject().getReferenceElement();
      retVal.add(subjectId);

      List<CareTeam.CareTeamParticipantComponent> participants = careTeam.getParticipant();
      for (var participant : participants)
      {
        IIdType participantId = participant.getMember().getReferenceElement();
        retVal.add(participantId);
      }
    }

    return retVal;
  }
}
