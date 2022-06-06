package ai.zuva;

import ai.zuva.http.ZdaiHttpClient;
import ai.zuva.ocr.OcrRequest;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest
class OcrRequestTest {

    @Test
    void ocrTest(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        int port = wmRuntimeInfo.getHttpPort();

        String fileId = "c5e41av1qk1er7odm79g";
        String requestId = "c5e41cgvsl2pp2tpc9i0";
        String postBody = TestHelpers.resourceAsString(this, "ocr-request.json");
        String postResponseBody = TestHelpers.resourceAsString(this, "ocr-request-created.json");
        stubFor(post("/ocr")
                .withRequestBody(equalToJson(postBody))
                .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");

        OcrRequest request = OcrRequest.createOcrRequest(client, fileId);
        assertEquals(requestId, request.requestId);

        String getStatusResponseBody = TestHelpers.resourceAsString(this, "ocr-status-complete.json");
        stubFor(get("/ocr/" + requestId)
                .willReturn(aResponse().withStatus(200).withBody(getStatusResponseBody)));

        assertEquals("complete", request.getStatus());


        String getTextResponseBody = TestHelpers.resourceAsString(this, "ocr-text.json");
        stubFor(get("/ocr/" + requestId + "/text")
                .willReturn(aResponse().withStatus(200).withBody(getTextResponseBody)));

        assertEquals("Exhibit ... ", request.getText());


        byte[] getImagesResponseBody = TestHelpers.resourceAsByteArray(this, "ocr-images.zip");

        stubFor(get("/ocr/" + requestId + "/images")
                .willReturn(aResponse().withStatus(200).withBody(getImagesResponseBody)));

        assertArrayEquals(getImagesResponseBody, request.getImages());


        byte[] getLayoutsResponseBody = new byte[]{1, 2, 3, 4};

        stubFor(get("/ocr/" + requestId + "/layouts")
                .willReturn(ok().withBody(getLayoutsResponseBody)));

        assertArrayEquals(getLayoutsResponseBody, request.getLayouts());

    }
}