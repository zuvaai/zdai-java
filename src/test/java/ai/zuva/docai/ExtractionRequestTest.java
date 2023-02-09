package ai.zuva.docai;

import static ai.zuva.docai.DocAIClient.*;
import static ai.zuva.docai.extraction.ExtractionRequest.getStatuses;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.zuva.docai.extraction.ExtractionMultipleStatuses;
import ai.zuva.docai.extraction.ExtractionRequest;
import ai.zuva.docai.extraction.ExtractionResults;
import ai.zuva.docai.files.File;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

@WireMockTest
public class ExtractionRequestTest {
  @Test
  void theTest(WireMockRuntimeInfo wmRuntimeInfo) throws RuntimeException {
    try {
      int port = wmRuntimeInfo.getHttpPort();
      String fileId = "c5e41av1qk1er7odm79g";
      String[] fieldIds =
          new String[] {
            "292b0a57-556b-4904-acfa-c3f845eb2879",
            "4d34c0dc-a3d4-4172-92d0-5fad8b3860a7",
            "5c971bd8-fc3b-4a26-8a95-674203871dfd",
            "c83868ae-269a-4a1b-b2af-c53e1f91efca",
            "f743f363-1d8b-435b-8812-204a6d883835"
          };
      String requestId = "c5e463f1qk154j5e3sjg";

      // Test constructing and sending a request for a single file
      String postRequestBody = TestHelpers.resourceAsString(this, "extraction-request.json");
      String postResponseBody =
          TestHelpers.resourceAsString(this, "extraction-request-created.json");

      stubFor(
          post("/api/v2/extraction")
              .withRequestBody(equalToJson(postRequestBody))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

      DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
      ExtractionRequest request =
          ExtractionRequest.createRequest(client, new File(client, fileId), fieldIds);

      assertEquals(requestId, request.requestId);

      // Test checking the status of a file
      String statusResponseBody =
          TestHelpers.resourceAsString(this, "extraction-status-complete.json");
      stubFor(get("/api/v2/extraction/" + requestId).willReturn(ok().withBody(statusResponseBody)));

      assertTrue(request.getStatus().isComplete());

      // Test checking the results for a file
      String textResponseBody = TestHelpers.resourceAsString(this, "extraction-results.json");
      stubFor(
          get("/api/v2/extraction/" + requestId + "/results/text")
              .willReturn(ok().withBody(textResponseBody)));

      ExtractionResults[] results = request.getResults();
      assertEquals(2, results.length);

      // Testing get Multiple Extraction Statuses
      File[] files =
          new File[] {
            new File(client, "ce7ks02b08o78qsc6qog"),
            new File(client, "ce7ks3qb08o78qsc6qsg"),
            new File(client, "ce7ks62b08o78qsc6qv0"),
            new File(client, "ce7m85s2nt5r5uan68hg")
          };

      postRequestBody = TestHelpers.resourceAsString(this, "multiple-extraction-request.json");
      postResponseBody =
          TestHelpers.resourceAsString(this, "multiple-extraction-request-created.json");

      stubFor(
          post("/api/v2/extraction")
              .withRequestBody(equalToJson(postRequestBody))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

      ExtractionRequest[] requests = ExtractionRequest.createRequests(client, files, fieldIds);

      List<String> extractionIds = new ArrayList<>();
      extractionIds.add("ce7m85s2nt5r5uan68g0");
      extractionIds.add("ce7m85s2nt5r5uan68gg");
      extractionIds.add("ce7m85s2nt5r5uan68h0");
      extractionIds.add("ce7m85s2nt5r5uan68hg");

      String getMultipleResponseBody =
          TestHelpers.resourceAsString(this, "multiple-status-response.json");
      stubFor(
          get("/api/v2/extractions?" + listToQueryParams("request_id", extractionIds))
              .willReturn(aResponse().withStatus(200).withBody(getMultipleResponseBody)));

      ExtractionMultipleStatuses statuses = getStatuses(client, extractionIds);

      assertEquals(statuses.numFound, 3);
      assertEquals(statuses.numErrors, 1);

      assertEquals(
          statuses.requestErrors.get("ce7m85s2nt5r5uan68hg").reqError.code, "request_not_found");

      assertTrue(statuses.statuses.get("ce7m85s2nt5r5uan68g0").status.isComplete());
      assertTrue(statuses.statuses.get("ce7m85s2nt5r5uan68gg").status.isProcessing());
      assertTrue(statuses.statuses.get("ce7m85s2nt5r5uan68h0").status.isComplete());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
