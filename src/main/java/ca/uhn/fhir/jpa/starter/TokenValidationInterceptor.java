package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.jpa.starter.Models.UserType;
import ca.uhn.fhir.jpa.starter.authorization.rules.RuleImplPatient;
import ca.uhn.fhir.jpa.starter.db.Search;
import ca.uhn.fhir.jpa.starter.db.Utils;
import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.IdType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Interceptor
public class TokenValidationInterceptor extends AuthorizationInterceptor {
  private static final Map<String, CacheRecord<List<IAuthRule>>> ruleCache = new ConcurrentHashMap<>();
  private static final Timer cacheTimer = new Timer("cache Timer",true);

  static
  {
    cacheTimer.schedule(new TimerTask() {
                          @Override
                          public void run() {
                            try {
                              cleanRuleCache();
                            } catch (Exception e) {
                              org.slf4j.LoggerFactory.getLogger("cacheTimer").error("cacheTimer:", e);
                            }
                          }
                        },
      Utils.getCacheTtl(),
      Utils.getCacheTtl());
  }

  private static class CacheRecord<T> {
    private final long recordTtl;

    private final T record;

    public T getRecord() {
      return record;
    }
    public CacheRecord(T record)
    {
      this.record = record;
      this.recordTtl = System.currentTimeMillis() + Utils.getCacheTtl();
    }
    public boolean isRecordExpired(){
      return ((recordTtl - System.currentTimeMillis()) < 0);
    }
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

    {
      CacheRecord<List<IAuthRule>> rule = ruleCache.get(authHeader);
      if(rule != null){
        return rule.getRecord();
      }
    }

    String token = authHeader.replace("Bearer ", "");

    TokenRecord tokenRecord = Utils.getTokenRecord(token);

    if (tokenRecord != null) {

      String bearerId = tokenRecord.getId();

      boolean isPractitioner = tokenRecord.is_practitioner();

      UserType myType;

      if(tokenRecord.is_practitioner()){
        UserType userType = Search.getPractitionerType(new IdType("Practitioner",tokenRecord.getId()));
        if (userType == null)
        {
          return new RuleBuilder()
            .denyAll("Practitioner has no Role!")
            .build();
        }

        myType = userType;
      }
      else {
        myType = UserType.patient;
      }

      if(isPractitioner && tokenRecord.getType() == UserType.superAdmin){
        return new RuleBuilder()
          .allowAll("Practitioner is admin")
          .build();
      }

      IIdType myId =  new IdType((isPractitioner)?"Practitioner":"Patient", bearerId);

      RuleImplPatient ruleImplPatient= new RuleImplPatient("",myId,isPractitioner,myType);

      List<IAuthRule> rule = new RuleBuilder()
        .allow().metadata().andThen()
        .allow().transaction().withAnyOperation().andApplyNormalRules()
        .build();
      List<IAuthRule> afterRules;
      if (Arrays.stream(tokenRecord.getScopes()).noneMatch(s -> s.equalsIgnoreCase("w:resources:*"))) {
        List<IAuthRule> readOnlyRules = new RuleBuilder()
          .deny("read only").write().allResources().withAnyId().andThen()
          .deny("read only").delete().allResources().withAnyId()
          .build();
        rule.addAll(0,readOnlyRules);
      }
      rule.add(ruleImplPatient);
      afterRules = new RuleBuilder()
        .denyAll("Default")
        .build();
      rule.addAll(afterRules);

      ruleCache.put(authHeader, new CacheRecord<>(rule));

      return rule;

    } else {
      return new RuleBuilder()
        .denyAll("invalid token")
        .build();
    }
  }
  private static void cleanRuleCache() {
    List<String> removeList = new ArrayList<>();
    ruleCache.forEach((k, v) -> {
      if (v.isRecordExpired()) {
        removeList.add(k);
      }
    });

    removeList.forEach(ruleCache::remove);
  }
}
