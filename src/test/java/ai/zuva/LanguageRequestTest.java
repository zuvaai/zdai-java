package ai.zuva;

import ai.zuva.files.File;
import ai.zuva.api.DocAIClient;
import ai.zuva.language.LanguageRequest;
import ai.zuva.language.LanguageResult;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WireMockTest
public class LanguageRequestTest {
    @Test
    void theTest(WireMockRuntimeInfo wmRuntimeInfo) throws RuntimeException {
        try {
            int port = wmRuntimeInfo.getHttpPort();
            String fileId = "c5e41av1qk1er7odm79g";
            String requestId = "c5e45a8vsl2ss5f0vmdg";

            // Test constructing and sending a request
            String postResponseBody = TestHelpers.resourceAsString(this, "language-request-created.json");
            stubFor(post("/api/v2/language")
                    .withRequestBody(equalToJson("{\"file_ids\": [\"c5e41av1qk1er7odm79g\"]}"))
                    .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

            DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
            LanguageRequest request = LanguageRequest.createRequest(client, new File(client, fileId));

            assertEquals(requestId, request.requestId);

            // Testing getClassificationResult where processing is complete
            String getResponseBody = TestHelpers.resourceAsString(this, "language-request-complete.json");
            stubFor(get("/api/v2/language/" + requestId)
                    .willReturn(aResponse().withStatus(200).withBody(getResponseBody)));

            LanguageResult result = request.getStatus();

            assertTrue(result.status.isComplete());
            assertEquals("English", result.language);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
