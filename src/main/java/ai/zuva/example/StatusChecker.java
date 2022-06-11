package ai.zuva.example;

import ai.zuva.RequestStatus;

import java.time.Instant;

interface StatusCheck {
    RequestStatus checkStatus() throws Exception;
}

public class StatusChecker {
    static RequestStatus waitForStatus(StatusCheck checker, long pollingIntervalSeconds, long timeoutSeconds) throws Exception {
        long tStart = Instant.now().toEpochMilli();
        System.out.print("Wait for processing");
        while (Instant.now().toEpochMilli() - tStart < timeoutSeconds * 1000) {
            RequestStatus status = checker.checkStatus();
            System.out.print(".");
            if (status.isComplete()|| status.isFailed()) {
                System.out.println(status.status.name());
                return status;
            }
            Thread.sleep(pollingIntervalSeconds * 1000);
        }
        System.out.println("Timed out waiting for request to be processed");
        return checker.checkStatus();
    }
}
