package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.starter.db.Utils;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

@Interceptor
public class CleanCacheInterceptor {
  @Hook(Pointcut.SERVER_PROCESSING_COMPLETED)
  public void cleanCache(ServletRequestDetails theRequestDetails) {

    try {
      Utils.cleanRuleCache();
      Utils.cleanTokenCache();
    } catch (Exception e) {
      org.slf4j.LoggerFactory.getLogger(CleanCacheInterceptor.class).error("CleanCacheInterceptor:",e);
    }
  }
}
