package ai.zuva.docai;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.zuva.docai.files.File;
import ai.zuva.docai.mlc.MLCRequest;
import ai.zuva.docai.mlc.MLCResult;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.Arrays;
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
      assertTrue(Arrays.equals(classifications, result.classifications));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
