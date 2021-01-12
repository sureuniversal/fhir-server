package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.jpa.starter.db.Utils;
import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {
  private final static Map ruleCache = new ConcurrentHashMap<String,  AuthRulesWrapper> ();
  private final static Map tokenCache = new ConcurrentHashMap<String, TokenRecord>();
  public static Timer cacheTimer = new Timer("cache Timer",true);

  static
  {
    cacheTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          cleanRuleCache();
          cleanTokenCache();
        } catch (Exception e) {
          org.slf4j.LoggerFactory.getLogger("cacheTimer").error("cacheTimer:", e);
        }
      }
    },
      Utils.getCacheTTL(),
      Utils.getCacheTTL());
  }

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
    if (theRequestDetails.getCompleteUrl().split("\\?")[0].contains(":8080")) {
      return new RuleBuilder()
        .allowAll("Port 8080")
        .build();
    }

    String authHeader = theRequestDetails.getHeader("Authorization");
    if (authHeader == null) {
      return new RuleBuilder()
        .denyAll("no authorization header")
        .build();
    }

    String token = authHeader.replace("Bearer ", "");
    TokenRecord tokenRecord = getCachedTokenIfExists(token);
    if (tokenRecord == null)
    {
      tokenRecord = Utils.getTokenRecord(token);
      if (tokenRecord == null) {
        return new RuleBuilder()
          .denyAll("invalid token")
          .build();
      }

      if(tokenRecord.is_practitioner()){
        tokenRecord.isAdmin = Search.isPractitionerAdmin(tokenRecord.getId());
      }

      tokenCache.put(token, tokenRecord);
    }

    var compartmentName = theRequestDetails.getRequestPath().split("/")[0];
    var operation = theRequestDetails.getRequestType();
    var cacheKey = authHeader + '-' + compartmentName + '-' + operation;
    var cachedRule = getCachedRuleIfExists(cacheKey);
    if (cachedRule != null)
    {
      return cachedRule.rules;
    }

    boolean isAdmin = tokenRecord.isAdmin;
    boolean isPractitioner = tokenRecord.is_practitioner();
    String userId = tokenRecord.getId();

    if (isAdmin)
    {
      return new RuleBuilder()
        .allowAll("Admin")
        .build();
    }

    var ruleBase =  Utils.rulesFactory(theRequestDetails, authHeader);
    if (ruleBase == null) {
      return new RuleBuilder()
        .denyAll("access Denied")
        .build();
    }

    if (isPractitioner) {
      ruleBase.addResourcesByPractitioner(userId);
    } else {
      ruleBase.addResource(userId);
    }

    ruleBase.setUserId(userId);
    List<IAuthRule> rule;
    switch (operation) {
      case TRACE:
      case TRACK:
      case HEAD:
      case CONNECT:
      case OPTIONS:
      case GET:
        rule = ruleBase.handleGet();
        break;
      case PUT:
      case DELETE:
      case PATCH:
      case POST:
        rule = ruleBase.handlePost();
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + operation);
    }

    ruleCache.put(cacheKey, new AuthRulesWrapper(rule));
    return rule;
  }

  public static class CacheRecord{
    public long recordTtl;
    public boolean isRecordExpired(){
      return ((recordTtl - System.currentTimeMillis()) < 0);
    }
  }

  private class AuthRulesWrapper extends CacheRecord
  {
    public List<IAuthRule> rules;
    public AuthRulesWrapper(List<IAuthRule> rules)
    {
      this.rules = rules;
      this.recordTtl = System.currentTimeMillis() + Utils.getCacheTTL();
    }
  }

  private static TokenRecord getCachedTokenIfExists(String cacheKey) {
    return  (TokenRecord) getCacheEntry(tokenCache, cacheKey);
  }

  private static AuthRulesWrapper getCachedRuleIfExists(String cacheKey) {
    return (AuthRulesWrapper) getCacheEntry(ruleCache, cacheKey);
  }

  private static CacheRecord getCacheEntry(Map<String, CacheRecord> cache, String cacheKey){
    var cachedRule = cache.get(cacheKey);
    if (cachedRule != null) {
      var recordTtl = cachedRule.recordTtl;
      if ((recordTtl - System.currentTimeMillis()) > 999) {
        return cachedRule;
      } else {
        cache.remove(recordTtl);
      }
    }

    return null;
  }

  public static void cleanTokenCache(){
    cleanCache(tokenCache);
  }

  public static void cleanRuleCache() {
    cleanCache(ruleCache);
  }

  public static void cleanCache(Map<String, CacheRecord> cache){
    List<String> removeList = new ArrayList<>();
    cache.forEach((k, v) -> {
      if (v.isRecordExpired()) {
        removeList.add(k);
      }
    });

    removeList.forEach(cache::remove);
  }
}
