package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.Models.AuthRulesWrapper;
import ca.uhn.fhir.jpa.starter.Models.TokenRecord;
import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.jpa.starter.Util.CacheUtil;
import ca.uhn.fhir.jpa.starter.Util.DBUtils;
import ca.uhn.fhir.jpa.starter.Util.Search;
import ca.uhn.fhir.jpa.starter.Util.SecurityRulesUtil;
import ca.uhn.fhir.jpa.starter.authorization.rules.RuleBase;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {
  private final static Map ruleCache = new ConcurrentHashMap<String, AuthRulesWrapper> ();
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
      DBUtils.getCacheTTL(),
      DBUtils.getCacheTTL());
  }

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
    if (theRequestDetails.getCompleteUrl().split("\\?")[0].contains(":8080")) {
      return new RuleBuilder()
        .allowAll("Port 8080")
        .build();
    }
    theRequestDetails.getParameters();
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
      tokenRecord = DBUtils.getTokenRecord(token);
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

    boolean isAdmin = tokenRecord.isAdmin;
    boolean isPractitioner = tokenRecord.is_practitioner();
    String userId = tokenRecord.getId();
    String[] scopes = tokenRecord.getScopes();

    if (isAdmin)
    {
      return new RuleBuilder()
        .allowAll("Admin")
        .build();
    }

//    if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.TRANSACTION){
//      return new RuleBuilder().allowAll().build();
//    }

    List<RuleBase>  ruleBase;
    try {
      ruleBase = SecurityRulesUtil.rulesFactory(theRequestDetails);
    } catch (Exception e) {
      throw new IllegalStateException(e.getMessage());
    }

    List<IAuthRule> rulesList = new ArrayList();
    for (var rule : ruleBase)
    {
      var cacheKey = CacheUtil.getCacheEntryForRequest(theRequestDetails, rule, authHeader);
      var cachedRule = getCachedRuleIfExists(cacheKey);
      if (cachedRule != null)
      {
        return cachedRule.rules;
      }

      var userType = isPractitioner ? UserType.practitioner : UserType.patient;
      rule.setupUser(userId, userType);
      rule.setUserIdsRequested(theRequestDetails);

      var result = HandleRule(rule,scopes);
      ruleCache.put(cacheKey, new AuthRulesWrapper(result));
      rulesList.addAll(result);
    }

    if (rulesList.isEmpty())
    {
      return new RuleBuilder()
        .denyAll("no Operation")
        .build();
    }

    return rulesList;
  }

  private List<IAuthRule> HandleRule(RuleBase rule, String[] scopes)
  {
    switch (rule.requestType) {
      case TRACE:
      case TRACK:
      case HEAD:
      case CONNECT:
      case OPTIONS:
      case GET:
        return rule.handleGet();
      case PUT:
      case DELETE:
      case PATCH:
      case POST:
//        if(Arrays.stream(scopes).noneMatch(s -> s.equals("w:resources:*")))
//        {
//          return new RuleBuilder()
//            .denyAll("Readonly can't post")
//            .build();
//        }
        return rule.handlePost();
      default:
        throw new IllegalStateException("Operation Unknown");
    }
  }

  private static TokenRecord getCachedTokenIfExists(String cacheKey) {
    return  (TokenRecord) CacheUtil.getCacheEntry(tokenCache, cacheKey);
  }

  private static AuthRulesWrapper getCachedRuleIfExists(String cacheKey) {
    return (AuthRulesWrapper) CacheUtil.getCacheEntry(ruleCache, cacheKey);
  }

  public static void cleanTokenCache(){
    CacheUtil.cleanCache(tokenCache);
  }

  public static void cleanRuleCache() {
    CacheUtil.cleanCache(ruleCache);
  }

}
