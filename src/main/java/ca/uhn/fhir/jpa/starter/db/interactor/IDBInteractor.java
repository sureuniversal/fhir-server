package ca.uhn.fhir.jpa.starter.db.interactor;

import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;

public interface IDBInteractor {
  TokenRecord getTokenRecord(String token);
}
