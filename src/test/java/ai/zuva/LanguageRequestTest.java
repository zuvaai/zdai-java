package ai.zuva;

import ai.zuva.http.ZdaiHttpClient;
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
            stubFor(post("/language")
                    .withRequestBody(equalToJson("{\"file_ids\": [\"c5e41av1qk1er7odm79g\"]}"))
                    .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

            ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
            LanguageRequest request = new LanguageRequest(client, fileId);

            assertEquals(requestId, request.requestId);

            // Testing getClassificationResult where processing is complete
            String getResponseBody = TestHelpers.resourceAsString(this, "language-request-complete.json");
            stubFor(get("/language/" + requestId)
                    .willReturn(aResponse().withStatus(200).withBody(getResponseBody)));

            LanguageResult result = request.getResult();

            assertEquals("complete", result.status);
            assertEquals("English", result.language);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
