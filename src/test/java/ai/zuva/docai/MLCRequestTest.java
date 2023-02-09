package ai.zuva.docai;

import static ai.zuva.docai.DocAIClient.*;
import static ai.zuva.docai.mlc.MLCRequest.getStatuses;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.*;

import ai.zuva.docai.files.File;
import ai.zuva.docai.mlc.MLCMultipleResults;
import ai.zuva.docai.mlc.MLCRequest;
import ai.zuva.docai.mlc.MLCResult;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.*;
import org.junit.jupiter.api.Test;

@WireMockTest
public class MLCRequestTest {

  @Test
  void theTest(WireMockRuntimeInfo wmRuntimeInfo) throws RuntimeException {
    try {
      int port = wmRuntimeInfo.getHttpPort();
      String fileId = "c5e41av1qk1er7odm79g";
      String requestId = "c5e43kf1qk1bstse6nrg";
      String[] classifications = new String[] {"Contract", "IP Agt", "License Agt"};

      // Test constructing and sending a request for a single file
      String postResponseBody =
          TestHelpers.resourceAsString(this, "doc-classification-request-created.json");
      stubFor(
          post("/api/v2/mlc")
              .withRequestBody(equalToJson("{\"file_ids\": [\"c5e41av1qk1er7odm79g\"]}"))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

      DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
      MLCRequest request = MLCRequest.createRequest(client, new File(client, fileId));

      assertEquals(requestId, request.requestId);

      // Testing getMLCResult where processing is complete
      String getResponseBody = TestHelpers.resourceAsString(this, "doc-mlc-request-complete.json");
      stubFor(
          get("/api/v2/mlc/" + requestId)
              .willReturn(aResponse().withStatus(200).withBody(getResponseBody)));

      MLCResult result = request.getStatus();

      assertTrue(result.isComplete());
      assertArrayEquals(classifications, result.classifications);

      // Testing get Multiple MLC Results
      File[] files =
          new File[] {
            new File(client, "ce7ks02b08o78qsc6qog"),
            new File(client, "ce7ks3qb08o78qsc6qsg"),
            new File(client, "ce7ks62b08o78qsc6qv0"),
            new File(client, "ce7m85s2nt5r5uan68hg")
          };

      postResponseBody = TestHelpers.resourceAsString(this, "multiple-status-request.json");
      stubFor(
          post("/api/v2/mlc")
              .withRequestBody(
                  equalToJson(
                      "{\"file_ids\": [\"ce7ks02b08o78qsc6qog\", \"ce7ks3qb08o78qsc6qsg\", \"ce7ks62b08o78qsc6qv0\", \"ce7m85s2nt5r5uan68hg\"]}"))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));
      MLCRequest[] requests = MLCRequest.createRequests(client, files);

      List<String> mlcIds = new ArrayList<>();
      mlcIds.add("ce7m85s2nt5r5uan68g0");
      mlcIds.add("ce7m85s2nt5r5uan68gg");
      mlcIds.add("ce7m85s2nt5r5uan68h0");
      mlcIds.add("ce7m85s2nt5r5uan68hg");

      String getMultipleResponseBody =
          TestHelpers.resourceAsString(this, "multiple-mlc-response.json");
      stubFor(
          get("/api/v2/mlcs?" + listToQueryParams("request_id", mlcIds))
              .willReturn(aResponse().withStatus(200).withBody(getMultipleResponseBody)));

      MLCMultipleResults results = getStatuses(client, mlcIds);

      assertEquals(results.numFound, 3);
      assertEquals(results.numErrors, 1);

      assertEquals(
          results.requestErrors.get("ce7m85s2nt5r5uan68hg").reqError.code, "request_not_found");

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68g0").status.isComplete());
      assertArrayEquals(
          results.statuses.get("ce7m85s2nt5r5uan68g0").classifications,
          (new String[] {"Contract", "IP Agt", "License Agt"}));

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68gg").status.isProcessing());

      assertTrue(results.statuses.get("ce7m85s2nt5r5uan68h0").status.isComplete());
      assertArrayEquals(
          results.statuses.get("ce7m85s2nt5r5uan68h0").classifications,
          (new String[] {"Contract", "IP Agt", "License Agt"}));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
