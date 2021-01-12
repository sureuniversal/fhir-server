package ca.uhn.fhir.jpa.starter.db;
import ca.uhn.fhir.jpa.starter.HapiProperties;
import ca.uhn.fhir.jpa.starter.authorization.rules.*;
import ca.uhn.fhir.jpa.starter.db.interactor.DBInteractorPostgres;
import ca.uhn.fhir.jpa.starter.db.interactor.IDBInteractor;
import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {

  private final static IDBInteractor interactor;
  private final static Map<String,TokenRecord> tokenCache = new ConcurrentHashMap<>();
  private final static Map<String,RuleBase> ruleCache = new ConcurrentHashMap<>();
  public static final long ttl = HapiProperties.getCacheTtl(240000);
  public static Timer cacheTimer = new Timer("cache Timer",true);

  static {
      String connectionString = System.getenv("FHIR_PG_TOKEN_URL");
      String postgresUser = System.getenv("FHIR_PG_TOKEN_USER_NAME");
      String postgresPass = System.getenv("FHIR_PG_TOKEN_PASSWORD");
      interactor = new DBInteractorPostgres(connectionString, postgresUser, postgresPass);
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
      },ttl,ttl);
  }

  public static TokenRecord getTokenRecord(String token) {
    var record = tokenCache.get(token);
    if (record == null)
    {
      record = interactor.getTokenRecord(token);
      tokenCache.put(token,record);
    }

    return record;
  }

  public static RuleBase rulesFactory(RequestDetails theRequestDetails, String authHeader,boolean isAdmin) {
    if(isAdmin){
      return new AdminRules(authHeader);
    }

    if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.TRANSACTION){
      return new AdminRules(authHeader);
    }

    String compartmentName = theRequestDetails.getRequestPath().split("/")[0];
    var cacheKey = authHeader + '-' + compartmentName;
    var cachedRule = ruleCache.get(cacheKey);
    if (cachedRule != null)
    {
      var recordTtl = cachedRule.recordTtl;
      if ((recordTtl - System.currentTimeMillis()) > 999)
      {
        return cachedRule;
      }
      else
      {
        ruleCache.remove(recordTtl);
      }
    }

    RuleBase res;
    switch (compartmentName) {
      case "Flag":
        res = new FlagRules(authHeader);
        break;
      case "Observation":
        res = new ObservationRules(authHeader);
        break;
      case "CareTeam":
        res = new CareTeamRules(authHeader);
        break;
      case "Patient":
        res = new PatientRules(authHeader);
        break;
      case "Practitioner":
        res = new PractitionerRules(authHeader);
        break;
      case "DeviceMetric":
        if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.SEARCH_TYPE){
          res = new AdminRules(authHeader);
        } else {
          res = new DeviceMetricRules(authHeader);
        }
        break;
      case "Device":
        if(theRequestDetails.getRestOperationType() == RestOperationTypeEnum.SEARCH_TYPE){
          res = new AdminRules(authHeader);
        } else {
          res = new DeviceRules(authHeader);
        }
        break;
      case "metadata":
        res = new MetadataRules(authHeader);
        break;
      case "PractitionerRole":
        res = new PractitionerRoleRules(authHeader);
        break;
      default:
        res = null;
        break;
    }

    ruleCache.put(cacheKey, res);
    return res;
  }

  public static void cleanTokenCache(){
    List<String> removeList = new ArrayList<>();
    tokenCache.forEach((k, v) -> {
      if (v.isRecordExpired()) {
        removeList.add(k);
      }
    });

    removeList.forEach(tokenCache::remove);
  }

  public static void cleanRuleCache() {
    List<String> removeList = new ArrayList<>();
    ruleCache.forEach((k, v) -> {
      if (v.isRecordExpired()) {
        removeList.add(k);
      }
    });

   removeList.forEach(ruleCache::remove);
  }
}