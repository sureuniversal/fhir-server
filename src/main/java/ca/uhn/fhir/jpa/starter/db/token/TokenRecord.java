package ca.uhn.fhir.jpa.starter.db.token;

public class TokenRecord {
  final String id;
  final String token;
  final boolean is_practitioner;
  final long issuedDate;
  final long expiresIn;

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
}
