package org.example.demo1;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.net.*;
public class ExceptionChecker {

    private static final String PYTHON_AI_URL = "http://127.0.0.1:8080/telemetry";

    public static void main(String[] args){
        Random random = new Random();

        for(int i=0;i<=4;i++){
            try{
                Thread.sleep(2000);

            }catch (Exception e){
                System.out.println(e.getMessage());
            }

            String randomError = generateTestFailure(random.nextInt(9));
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
            // === DATABASE_DEADLOCK VARIATIONS ===
            case 0:
                return "Transaction rolled back. Process ID 54 was victimized by another concurrent thread trying to acquire row-level persistence locks.";
            case 1:
                return "Severe: SQL Server aborted execution path. Shared lock contention on index table metadata prevented commit operations.";
            case 2:
                return "Error code 1205: Execution graph cyclic dependency detected during parallel batch ingestion. Session closed by coordinator.";

            // === SECURITY_AUTH_BREACH VARIATIONS ===
            case 3:
                return "Anomalous activity: Token decoding failed repeatedly from inbound source. Gateway dropped corrupt authorization payload.";
            case 4:
                return "Security violation: Access denied for administrator scope. Unrecognized machine fingerprint attempted high-privilege query.";
            case 5:
                return "Request dropped. Inbound payload signature does not match expected cryptographic hash rules for public traffic.";

            // === RESOURCE_EXHAUSTION VARIATIONS ===
            case 6:
                return "OutOfMemoryError imminent: Direct byte buffer allocations have surpassed the specified boundary thresholds allocation limits.";
            case 7:
                return "Performance degradation: Thread worker pool is completely saturated. 0 available executors remaining in application context.";
            case 8:
                return "System Halt: Virtual machine heap usage hovering at 99.4% allocation ceiling. Garbage collector invocation yielding 0 bytes reclaimed.";

            default:
                return "System info: Standard heartbeat ping acknowledged successfully.";
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
