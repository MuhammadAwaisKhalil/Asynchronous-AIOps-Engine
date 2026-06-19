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

        for(int i=0;i<=6;i++){
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
            // === DATABASE_DEADLOCK VARIATIONS (Label 0) ===
            case 0:
                return "SQL engine halted: Process victimized by deadlock monitor. Thread was waiting to acquire row-level lock held by another session.";
            case 1:
                return "Transaction aborted: Mutex acquisition timed out during bulk update. Multiple workers blocked by intersecting foreign keys.";
            case 2:
                return "Database cyclic dependency warning: Execution graph stalled. Coordinator rolling back transaction due to shared lock contention.";

            // === SECURITY_AUTH_BREACH VARIATIONS (Label 1) ===
            case 3:
                return "Gateway security drop: Malformed JWT payload detected. Cryptographic signature does not match the active environment key.";
            case 4:
                return "Intrusion detection tripped: Unrecognized machine fingerprint attempted rapid sequential access. Rate limit enforced.";
            case 5:
                return "WAF alert: Inbound request blocked. Potential injection payload sanitized before reaching the authentication layer.";

            // === RESOURCE_EXHAUSTION VARIATIONS (Label 2) ===
            case 6:
                return "System halt imminent: OS refused memory allocation. Virtual machine heap hovering at maximum allocation ceiling.";
            case 7:
                return "Connection pool drained: HikariPool-1 has 0 active connections remaining. Request timed out waiting for resource clearance.";
            case 8:
                return "OS boundary reached: File descriptor limit exceeded. The kernel refused to allocate further socket connections to the process.";

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
