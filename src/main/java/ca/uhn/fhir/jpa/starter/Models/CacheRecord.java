package ca.uhn.fhir.jpa.starter.Models;

public class CacheRecord{
  public long recordTtl;
  public boolean isRecordExpired(){
    return ((recordTtl - System.currentTimeMillis()) < 0);
  }
}