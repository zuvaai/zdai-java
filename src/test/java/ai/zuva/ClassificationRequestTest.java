package ai.zuva;

import ai.zuva.classification.ClassificationRequest;
import ai.zuva.classification.ClassificationResult;
import ai.zuva.files.File;
import ai.zuva.api.DocAIClient;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class ClassificationRequestTest {

    @Test
    void theTest(WireMockRuntimeInfo wmRuntimeInfo) throws RuntimeException {
        try {
            int port = wmRuntimeInfo.getHttpPort();
            String fileId = "c5e41av1qk1er7odm79g";
            String requestId = "c5e43kf1qk1bstse6nrg";

            // Test constructing and sending a request for a single file
            String postResponseBody = TestHelpers.resourceAsString(this, "doc-classification-request-created.json");
            stubFor(post("/api/v2/classification")
                    .withRequestBody(equalToJson("{\"file_ids\": [\"c5e41av1qk1er7odm79g\"]}"))
                    .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

            DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
            ClassificationRequest request = ClassificationRequest.createRequest(client, new File(client, fileId));

            assertEquals(requestId, request.requestId);

            // Testing getClassificationResult where processing is complete
            String getResponseBody = TestHelpers.resourceAsString(this, "doc-classification-request-complete.json");
            stubFor(get("/api/v2/classification/" + requestId)
                    .willReturn(aResponse().withStatus(200).withBody(getResponseBody)));

            ClassificationResult result = request.getStatus();

            assertTrue(result.isComplete());
            assertEquals("Real Estate Agt", result.classification);
            assertTrue(result.isContract);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}