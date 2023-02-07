package ai.zuva.docai;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.zuva.docai.classification.ClassificationMultipleResults;
import ai.zuva.docai.classification.ClassificationRequest;
import ai.zuva.docai.classification.ClassificationResult;
import ai.zuva.docai.files.File;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

@WireMockTest
class ClassificationRequestTest {

  @Test
  void theTest(WireMockRuntimeInfo wmRuntimeInfo) throws RuntimeException {
    try {
      int port = wmRuntimeInfo.getHttpPort();
      String fileId = "c5e41av1qk1er7odm79g";
      String requestId = "c5e43kf1qk1bstse6nrg";

      // Test constructing and sending a request for a single file
      String postResponseBody =
          TestHelpers.resourceAsString(this, "doc-classification-request-created.json");
      stubFor(
          post("/api/v2/classification")
              .withRequestBody(equalToJson("{\"file_ids\": [\"c5e41av1qk1er7odm79g\"]}"))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

      DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
      ClassificationRequest request =
          ClassificationRequest.createRequest(client, new File(client, fileId));

      assertEquals(requestId, request.requestId);

      // Testing getClassificationResult where processing is complete
      String getResponseBody =
          TestHelpers.resourceAsString(this, "doc-classification-request-complete.json");
      stubFor(
          get("/api/v2/classification/" + requestId)
              .willReturn(aResponse().withStatus(200).withBody(getResponseBody)));

      ClassificationResult result = request.getStatus();

      assertTrue(result.isComplete());
      assertEquals("Real Estate Agt", result.classification);
      assertTrue(result.isContract);

      // Testing get Multiple Classification Results
      File[] files =
          new File[] {
            new File(client, "ce7ks02b08o78qsc6qog"),
            new File(client, "ce7ks3qb08o78qsc6qsg"),
            new File(client, "ce7ks62b08o78qsc6qv0"),
            new File(client, "ce7m85s2nt5r5uan68hg")
          };
      postResponseBody =
          TestHelpers.resourceAsString(this, "doc-multiple-classification-request.json");
      stubFor(
          post("/api/v2/classification")
              .withRequestBody(
                  equalToJson(
                      "{\"file_ids\": [\"ce7ks02b08o78qsc6qog\", \"ce7ks3qb08o78qsc6qsg\", \"ce7ks62b08o78qsc6qv0\", \"ce7m85s2nt5r5uan68hg\"]}"))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));
      ClassificationRequest[] requests = ClassificationRequest.createRequests(client, files);

      Map<String, String> classificationsQueryParams = new HashMap<>();
      classificationsQueryParams.put("request_id", "ce7m85s2nt5r5uan68g0");
      classificationsQueryParams.put("request_id", "ce7m85s2nt5r5uan68gg");
      classificationsQueryParams.put("request_id", "ce7m85s2nt5r5uan68h0");
      classificationsQueryParams.put("request_id", "ce7m85s2nt5r5uan68hg");

      String getMultipleResponseBody =
          TestHelpers.resourceAsString(this, "doc-multiple-classification-response.json");
      stubFor(
          get("/api/v2/classifications&" + client.mapToQueryParams(classificationsQueryParams))
              .willReturn(aResponse().withStatus(200).withBody(getMultipleResponseBody)));

      ClassificationMultipleResults results = requests[0].getStatuses();

      assertEquals(results.numFound, 3);
      assertEquals(results.numErrors, 1);

      assertEquals(
          results.requestErrors.get("ce7m85s2nt5r5uan68hg").reqError.code, "request_not_found");

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68g0").status.isComplete());
      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68g0").isContract);
      assertEquals(
          results.statuses.get("ce7m85s2nt5r5uan68g0").classification, "Intellectual Property Agt");

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68gg").status.isProcessing());

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68h0").status.isComplete());
      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68h0").isContract);
      assertEquals(
          results.statuses.get("ce7m85s2nt5r5uan68h0").classification, "Intellectual Property Agt");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
