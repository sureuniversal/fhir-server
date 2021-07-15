package ca.uhn.fhir.jpa.starter.db.interactor;

import ca.uhn.fhir.jpa.starter.db.token.TokenRecord;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DBInteractorLoopback implements IDBInteractor{
  private final String loopbackUrl;

  public DBInteractorLoopback(String loopbackUrl) {
    this.loopbackUrl = loopbackUrl;
  }

  @Override
  public TokenRecord getTokenRecord(String token) {
    try {
      URL url = new URL(loopbackUrl+"getUserInfoByAccessToken?access_token="+token);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      if(con.getResponseCode() == 401){
        return null;
      }

      JSONTokener tokener = new JSONTokener(con.getInputStream());
      JSONObject json = new JSONObject(tokener);
      con.disconnect();
      String id = (String) json.get("userId");
      Boolean isPractitioner = (Boolean) json.get("isPractitioner");
      String status = json.get("status").equals(JSONObject.NULL) ? null:(String) json.get("status");
      return new TokenRecord(id,token,isPractitioner,0,0,null,status);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
