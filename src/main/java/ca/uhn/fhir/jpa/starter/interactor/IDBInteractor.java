package ca.uhn.fhir.jpa.starter.interactor;

import ca.uhn.fhir.jpa.starter.Models.TokenRecord;

public interface IDBInteractor {
  TokenRecord getTokenRecord(String token);
}