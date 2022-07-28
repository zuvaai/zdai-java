package ai.zuva;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.zuva.files.File;
import ai.zuva.ocr.OcrRequest;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

@WireMockTest
class OcrRequestTest {

  @Test
  void ocrTest(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
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

    OcrRequest request = OcrRequest.submitRequest(client, new File(client, fileId));
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
  }
}
