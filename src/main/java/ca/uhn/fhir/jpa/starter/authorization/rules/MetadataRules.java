package ca.uhn.fhir.jpa.starter.authorization.rules;

public class MetadataRules extends GeneralRules{
  public MetadataRules(String authHeader) {
    super(authHeader);
  }
}
