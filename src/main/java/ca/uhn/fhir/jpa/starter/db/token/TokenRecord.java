package ca.uhn.fhir.jpa.starter.db.token;

import ca.uhn.fhir.jpa.starter.db.Utils;

public class TokenRecord {
  final String id;
  final String token;
  final boolean is_practitioner;
  final long issuedDate;
  final long expiresIn;
  public long recordTtl;

  public TokenRecord(String id, String token, boolean is_practitioner, long issuedDate, long expiresIn) {
    this.id = id;
    this.token = token;
    this.is_practitioner = is_practitioner;
    this.issuedDate = issuedDate;
    this.expiresIn = expiresIn;
    this.recordTtl = System.currentTimeMillis() + Utils.ttl;
  }

  public String getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public boolean is_practitioner() {
    return is_practitioner;
  }

  public long getIssuedDate() {
    return issuedDate;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public boolean isRecordExpired(){
    return ((recordTtl - System.currentTimeMillis()) < 0);
  }
}
