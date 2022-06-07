package ai.zuva;

import ai.zuva.classification.ClassificationRequest;
import ai.zuva.classification.ClassificationResult;
import ai.zuva.files.ZdaiFile;
import ai.zuva.http.ZdaiHttpClient;
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
            stubFor(post("/classification")
                    .withRequestBody(equalToJson("{\"file_ids\": [\"c5e41av1qk1er7odm79g\"]}"))
                    .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

            ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
            ClassificationRequest request = ClassificationRequest.createClassificationRequest(client, new ZdaiFile(client, fileId));

            assertEquals(requestId, request.requestId);

            // Testing getClassificationResult where processing is complete
            String getResponseBody = TestHelpers.resourceAsString(this, "doc-classification-request-complete.json");
            stubFor(get("/classification/" + requestId)
                    .willReturn(aResponse().withStatus(200).withBody(getResponseBody)));

            ClassificationResult result = request.getClassificationResult();

            assertEquals("complete", result.status);
            assertEquals("Real Estate Agt", result.classification);
            assertTrue(result.isContract);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}