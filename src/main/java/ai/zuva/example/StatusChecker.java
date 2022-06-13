package ai.zuva.example;

import ai.zuva.BaseRequest;
import ai.zuva.RequestStatus;

import java.time.Instant;

public class StatusChecker {
    static RequestStatus waitForStatus(BaseRequest request, long pollingIntervalSeconds, long timeoutSeconds) throws Exception {
        long tStart = Instant.now().toEpochMilli();
        System.out.print("Wait for processing");
        while (Instant.now().toEpochMilli() - tStart < timeoutSeconds * 1000) {
            RequestStatus status = request.getStatus();
            System.out.print(".");
            if (status.isComplete()|| status.isFailed()) {
                System.out.println(status.status.name());
                return status;
            }
            Thread.sleep(pollingIntervalSeconds * 1000);
        }
        System.out.println("Timed out waiting for request to be processed");
        return request.getStatus();
    }
}
