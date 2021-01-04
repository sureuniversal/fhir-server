package ca.uhn.fhir.jpa.starter.db.token;

import ca.uhn.fhir.jpa.starter.HapiProperties;

public class TokenRecord {
  private static final long ttl = HapiProperties.getCacheTtl(240000);
  final String id;
  final String token;
  final boolean is_practitioner;
  final long issuedDate;
  final long expiresIn;
  final long recordTtl;

  public TokenRecord(String id, String token, boolean is_practitioner, long issuedDate, long expiresIn) {
    this.id = id;
    this.token = token;
    this.is_practitioner = is_practitioner;
    this.issuedDate = issuedDate;
    this.expiresIn = expiresIn;
    this.recordTtl = System.currentTimeMillis() + ttl;
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
