package ca.uhn.fhir.jpa.starter.Models;

import ca.uhn.fhir.jpa.starter.Util.DBUtils;

public class TokenRecord extends CacheRecord {
  final String id;
  final String token;
  final boolean is_practitioner;
  public boolean isAdmin = false;
  final long issuedDate;
  final long expiresIn;

  public TokenRecord(String id, String token, boolean is_practitioner, long issuedDate, long expiresIn) {
    this.id = id;
    this.token = token;
    this.is_practitioner = is_practitioner;
    this.issuedDate = issuedDate;
    this.expiresIn = expiresIn;
    this.recordTtl = System.currentTimeMillis() + DBUtils.getCacheTTL();
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
