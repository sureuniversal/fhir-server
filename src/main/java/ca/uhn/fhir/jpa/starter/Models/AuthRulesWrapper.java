package ca.uhn.fhir.jpa.starter.Models;

import ca.uhn.fhir.jpa.starter.Util.DBUtils;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;

import java.util.List;

public class AuthRulesWrapper extends CacheRecord
{
  public List<IAuthRule> rules;
  public AuthRulesWrapper(List<IAuthRule> rules)
  {
    this.rules = rules;
    this.recordTtl = System.currentTimeMillis() + DBUtils.getCacheTTL();
  }
}