package ai.zuva.examples;

import java.time.Instant;

interface StatusCheck {
    String checkStatus() throws Exception;
}

public class StatusChecker {
    static String waitForStatus(StatusCheck checker, long pollingIntervalSeconds, long timeoutSeconds) throws Exception {
        long tStart = Instant.now().toEpochMilli();
        System.out.print("Wait for processing");
        while (Instant.now().toEpochMilli() - tStart < timeoutSeconds * 1000) {
            String status = checker.checkStatus();
            System.out.print(".");
            if (status.equals("complete") || status.equals("failed")) {
                System.out.println(status);
                return status;
            }
            Thread.sleep(pollingIntervalSeconds * 1000);
        }
        System.out.println("Timed out waiting for request to be processed");
        return checker.checkStatus();
    }
}
