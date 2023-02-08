package ai.zuva.docai;

import static ai.zuva.docai.DocAIClient.listToMapQueryParams;
import static ai.zuva.docai.DocAIClient.mapToQueryParams;
import static ai.zuva.docai.ocr.OcrRequest.getStatuses;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.zuva.docai.files.File;
import ai.zuva.docai.ocr.OcrMultipleStatuses;
import ai.zuva.docai.ocr.OcrRequest;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@WireMockTest
class OcrRequestTest {

  @Test
  void ocrTest(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
    try {
      int port = wmRuntimeInfo.getHttpPort();

      String fileId = "c5e41av1qk1er7odm79g";
      String requestId = "c5e41cgvsl2pp2tpc9i0";
      String postBody = TestHelpers.resourceAsString(this, "ocr-request.json");
      String postResponseBody = TestHelpers.resourceAsString(this, "ocr-request-created.json");
      stubFor(
          post("/api/v2/ocr")
              .withRequestBody(equalToJson(postBody))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

      DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");

      OcrRequest request = OcrRequest.createRequest(client, new File(client, fileId));
      assertEquals(requestId, request.requestId);

      String getStatusResponseBody = TestHelpers.resourceAsString(this, "ocr-status-complete.json");
      stubFor(
          get("/api/v2/ocr/" + requestId)
              .willReturn(aResponse().withStatus(200).withBody(getStatusResponseBody)));

      assertTrue(request.getStatus().isComplete());

      String getTextResponseBody = TestHelpers.resourceAsString(this, "ocr-text.json");
      stubFor(
          get("/api/v2/ocr/" + requestId + "/text")
              .willReturn(aResponse().withStatus(200).withBody(getTextResponseBody)));

      assertEquals("Exhibit ... ", request.getText());

      byte[] getImagesResponseBody = TestHelpers.resourceAsByteArray(this, "ocr-images.zip");

      stubFor(
          get("/api/v2/ocr/" + requestId + "/images")
              .willReturn(aResponse().withStatus(200).withBody(getImagesResponseBody)));

      assertArrayEquals(getImagesResponseBody, request.getImages());

      byte[] getLayoutsResponseBody = new byte[] {1, 2, 3, 4};

      stubFor(
          get("/api/v2/ocr/" + requestId + "/layouts")
              .willReturn(ok().withBody(getLayoutsResponseBody)));

      assertArrayEquals(getLayoutsResponseBody, request.getLayouts());

      // Testing get Multiple OCR Results
      File[] files =
          new File[] {
            new File(client, "ce7ks02b08o78qsc6qog"),
            new File(client, "ce7ks3qb08o78qsc6qsg"),
            new File(client, "ce7ks62b08o78qsc6qv0"),
            new File(client, "ce7m85s2nt5r5uan68hg")
          };

      postResponseBody = TestHelpers.resourceAsString(this, "multiple-status-request.json");
      stubFor(
          post("/api/v2/ocr")
              .withRequestBody(
                  equalToJson(
                      "{\"file_ids\": [\"ce7ks02b08o78qsc6qog\", \"ce7ks3qb08o78qsc6qsg\", \"ce7ks62b08o78qsc6qv0\", \"ce7m85s2nt5r5uan68hg\"]}"))
              .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));
      OcrRequest[] requests = OcrRequest.createRequests(client, files);

      List<String> ocrIds = new ArrayList<>();
      ocrIds.add("ce7m85s2nt5r5uan68g0");
      ocrIds.add("ce7m85s2nt5r5uan68gg");
      ocrIds.add("ce7m85s2nt5r5uan68h0");
      ocrIds.add("ce7m85s2nt5r5uan68hg");

      Map<String, String> ocrsQueryParams = listToMapQueryParams("request_id", ocrIds);

      String getMultipleResponseBody =
          TestHelpers.resourceAsString(this, "multiple-status-response.json");
      stubFor(
          get("/api/v2/ocrs&" + mapToQueryParams(ocrsQueryParams))
              .willReturn(aResponse().withStatus(200).withBody(getMultipleResponseBody)));

      OcrMultipleStatuses statuses = getStatuses(client, ocrIds);

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
