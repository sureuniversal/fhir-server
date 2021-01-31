package ca.uhn.fhir.jpa.starter.Util;

import ca.uhn.fhir.jpa.starter.Models.CacheRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CacheUtil {
  public static CacheRecord getCacheEntry(Map<String, CacheRecord> cache, String cacheKey){
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
