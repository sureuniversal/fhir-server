package ca.uhn.fhir.jpa.starter.oauth;

public class TokenRecord {
  String id;
  String token;
  boolean is_practitioner;
  long issuedDate;
  long expiresIn;

  public TokenRecord() {
    this.id = null;
    this.token = null;
    this.is_practitioner = false;
    this.issuedDate = -1;
    this.expiresIn = -1;
  }

  public TokenRecord(String id, String token, boolean is_practitioner, long issuedDate, long expiresIn) {
    this.id = id;
    this.token = token;
    this.is_practitioner = is_practitioner;
    this.issuedDate = issuedDate;
    this.expiresIn = expiresIn;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public boolean isIs_practitioner() {
    return is_practitioner;
  }

  public void setIs_practitioner(boolean is_practitioner) {
    this.is_practitioner = is_practitioner;
  }

  public long getIssuedDate() {
    return issuedDate;
  }

  public void setIssuedDate(long issuedDate) {
    this.issuedDate = issuedDate;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }
}
