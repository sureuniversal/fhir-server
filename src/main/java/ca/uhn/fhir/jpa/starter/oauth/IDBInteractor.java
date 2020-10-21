package ca.uhn.fhir.jpa.starter.oauth;

public interface IDBInteractor {
  TokenRecord getTokenRecord(String token);
}
