package ca.uhn.fhir.jpa.starter.Models;

import ca.uhn.fhir.jpa.starter.Util.DBUtils;

public class TokenRecord extends CacheRecord {
  final String id;
  final String token;
  final boolean is_practitioner;
  PractitionerType type = PractitionerType.noAdmin;
  final long issuedDate;
  final long expiresIn;
  final long recordTtl;
  final String[] scopes;

  public enum PractitionerType{noAdmin,organizationAdmin,superAdmin}

  public TokenRecord(String id, String token, boolean is_practitioner, long issuedDate, long expiresIn, String[] scopes) {
    this.id = id;
    this.token = token;
    this.is_practitioner = is_practitioner;
    this.issuedDate = issuedDate;
    this.expiresIn = expiresIn;
    this.scopes = scopes;
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

  public String[] getScopes() {
    return scopes;
  }

  public boolean isAdmin(){
    return type==PractitionerType.superAdmin;
  }

  public PractitionerType getType() {
    return type;
  }

  public void setType(PractitionerType type) {
    this.type = type;
  }
}
