package ca.uhn.fhir.jpa.starter.oauth;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.hl7.fhir.r4.model.IdType;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import java.util.Arrays;
import com.mongodb.Block;

import com.mongodb.client.MongoCursor;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;



import java.util.Arrays;

import java.util.List;

@SuppressWarnings("ConstantConditions")
public class PatientAndAdminAuthorizationInterceptor extends AuthorizationInterceptor {

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {


    IdType userIdPatientId = null;
    boolean userIsAdmin = false;
    String authHeader = theRequestDetails.getHeader("Authorization");
    if(authHeader == null){
      return new RuleBuilder()
        .denyAll()
        .build();
    }
    String authToken = authHeader.replace("Bearer ","");

    MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://root:r4SbCgq7Lj5Nqm3@34.244.193.250:27017"));
    MongoDatabase database = mongoClient.getDatabase("users");
    MongoCollection<Document> oAuthAccessTokensCollection = database.getCollection("OAuthAccessToken");
    Document authTokenDocument =  oAuthAccessTokensCollection.find(eq("accessToken",authToken)).first();
    if(authTokenDocument == null){
      throw new AuthenticationException("Missing or invalid Authorization header value");
    }

    MongoCollection<Document> usersCollection = database.getCollection("user");
    String userID = authTokenDocument.getString("uid");
    Document userDocument = usersCollection.find(eq("_id",userID)).first();
    if(userDocument == null){
      throw new AuthenticationException("Missing or invalid Authorization header value");
    }
    if(userDocument.getString("patientId") != null){
      userIdPatientId = new IdType("Patient", userDocument.getString("patientId"));
    }

    if(userDocument.getBoolean("isFhirAdmin",false) != false){
      userIsAdmin = true;
    }


    if(userIdPatientId != null){

    }
    else {
      // Throw an HTTP 401
      throw new AuthenticationException("Missing or invalid Authorization header value");
    }

    // If the user is a specific patient, we create the following rule chain:
    // Allow the user to read anything in their own patient compartment
    // Allow the user to write anything in their own patient compartment
    // If a client request doesn't pass either of the above, deny it
    if (userIdPatientId != null) {
      return new RuleBuilder()
        .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
        .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen()
        .denyAll()
        .build();
    }

    // If the user is an admin, allow everything
    if (userIsAdmin) {
      return new RuleBuilder()
        .allowAll()
        .build();
    }

    // By default, deny everything. This should never get hit, but it's
    // good to be defensive
    return new RuleBuilder()
      .denyAll()
      .build();
  }
}