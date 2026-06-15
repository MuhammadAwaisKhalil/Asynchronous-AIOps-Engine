package org.example.demo1;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.net.*;
public class ExceptionChecker {

    private static final String PYTHON_AI_URL = "http://127.0.0.1:5000/telemetry";

    public static void main(String[] args){
        Random random = new Random();

        for(int i=0;i<=4;i++){
            try{
                Thread.sleep(2000);

            }catch (Exception e){
                System.out.println(e.getMessage());
            }

            String randomError = generateTestFailure(random.nextInt(3));
            System.out.println("Error caught\n"+randomError);
            giveErrorToAI(i,randomError);

            try{
                Thread.sleep(5000);

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private static String generateTestFailure(int errorType) {
        switch (errorType) {
            case 0:
                return "Error: DB pool connection dropped during multi-row batch update. Lock clearance failed.";
            case 1:
                return "Alert: Client signature validation failed on public gateway endpoint. Terminating session footprint.";
            case 2:
                return "System Warning: Core execution environment memory buffer has reached maximum capacity ceiling.";
            default:
                return "System configuration warning: localized latency detected.";
        }
    }
    private static void handleAIResopnse(String AIresponse){
        switch (AIresponse) {
            case "DATABASE_DEADLOCK":
                System.out.println(" [SELF-HEALING] Flushing the Hibernate database pool...");
                System.out.println(" [SELF-HEALING] Re-routing traffic to read-replica database node.");
                break;

            case "SECURITY_AUTH_BREACH":
                System.out.println("️ [INPUT SANITIZATION] Dropping connection payload instantly.");
                System.out.println("️ [NETWORK SECURITY] Adding source IP to Cloudflare black hole list.");
                break;

            case "RESOURCE_EXHAUSTION":
                System.out.println("️ [RESOURCE MITIGATION] Forcing a system Garbage Collection cycle...");
                System.out.println("️ [CLUSTER MANAGEMENT] Spinning up an auto-scaled secondary microservice pod.");
                break;

            default:
                System.out.println("️ [LOGGED] No immediate risk. Writing event to standard telemetry database.");
                break;
        }
    }
    private static void giveErrorToAI(int logID,String log){
        try{
            String cleandLog = log.replace("\"","\\\"");

            String jsonFormat = String.format("{\"logID\":%d, \"logText\":\"%s\"}",logID,cleandLog);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(PYTHON_AI_URL))
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonFormat))
                    .build();

            System.out.println("Sending packet to AI model");

//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            String aiDecision = response.body().trim();
//            handleAIResopnse(aiDecision);
            // Sending an Asynch call to the python server that predicts error type and logs the error
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(aiResponse->{
                        String response = aiResponse.trim();
                        handleAIResopnse(response);
                    }).exceptionally(e->{
                        System.out.println("Error in asynchronous data exchange from here to python server");
                        return null;
                    });



        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
