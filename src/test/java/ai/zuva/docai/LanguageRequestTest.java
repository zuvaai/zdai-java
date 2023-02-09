package ai.zuva.docai;

import static ai.zuva.docai.DocAIClient.*;
import static ai.zuva.docai.language.LanguageRequest.getStatuses;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.zuva.docai.files.File;
import ai.zuva.docai.language.LanguageMultipleResults;
import ai.zuva.docai.language.LanguageRequest;
import ai.zuva.docai.language.LanguageResult;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

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
      stubFor(
          post("/api/v2/language")
              .withRequestBody(equalToJson("{\"file_ids\": [\"c5e41av1qk1er7odm79g\"]}"))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

      DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
      LanguageRequest request = LanguageRequest.createRequest(client, new File(client, fileId));

      assertEquals(requestId, request.requestId);

      // Testing getClassificationResult where processing is complete
      String getResponseBody = TestHelpers.resourceAsString(this, "language-request-complete.json");
      stubFor(
          get("/api/v2/language/" + requestId)
              .willReturn(aResponse().withStatus(200).withBody(getResponseBody)));

      LanguageResult result = request.getStatus();

      assertTrue(result.status.isComplete());
      assertEquals("English", result.language);

      // Testing get Multiple Language Results
      File[] files =
          new File[] {
            new File(client, "ce7ks02b08o78qsc6qog"),
            new File(client, "ce7ks3qb08o78qsc6qsg"),
            new File(client, "ce7ks62b08o78qsc6qv0"),
            new File(client, "ce7m85s2nt5r5uan68hg")
          };

      postResponseBody = TestHelpers.resourceAsString(this, "multiple-status-request.json");
      stubFor(
          post("/api/v2/language")
              .withRequestBody(
                  equalToJson(
                      "{\"file_ids\": [\"ce7ks02b08o78qsc6qog\", \"ce7ks3qb08o78qsc6qsg\", \"ce7ks62b08o78qsc6qv0\", \"ce7m85s2nt5r5uan68hg\"]}"))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));
      LanguageRequest[] requests = LanguageRequest.createRequests(client, files);

      List<String> languageIds = new ArrayList<>();
      languageIds.add("ce7m85s2nt5r5uan68g0");
      languageIds.add("ce7m85s2nt5r5uan68gg");
      languageIds.add("ce7m85s2nt5r5uan68h0");
      languageIds.add("ce7m85s2nt5r5uan68hg");

      String getMultipleResponseBody =
          TestHelpers.resourceAsString(this, "multiple-language-response.json");
      stubFor(
          get("/api/v2/languages?" + listToQueryParams("request_id", languageIds))
              .willReturn(aResponse().withStatus(200).withBody(getMultipleResponseBody)));

      LanguageMultipleResults results = getStatuses(client, languageIds);

      assertEquals(results.numFound, 3);
      assertEquals(results.numErrors, 1);

      assertEquals(
          results.requestErrors.get("ce7m85s2nt5r5uan68hg").reqError.code, "request_not_found");

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68g0").status.isComplete());
      assertEquals(results.statuses.get("ce7m85s2nt5r5uan68g0").language, "English");

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68gg").status.isProcessing());

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68h0").status.isComplete());
      assertEquals(results.statuses.get("ce7m85s2nt5r5uan68h0").language, "English");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
