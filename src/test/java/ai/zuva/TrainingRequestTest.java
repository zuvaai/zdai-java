package ai.zuva;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.fields.TrainingExample;
import ai.zuva.fields.TrainingRequest;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
public class TrainingRequestTest {
    @Test
    void requestTrainingSuccessTest(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

        int port = wmRuntimeInfo.getHttpPort();
        String fieldId = "2efa79d4-854d-46de-8087-f70778157dbf";
        String requestId = "c71qfbbo2ua8dqja2d70";


        String postRequestBody = TestHelpers.resourceAsString(this, "training-request.json");
        String postResponseBody = TestHelpers.resourceAsString(this, "training-request-created.json");

        TrainingExample te = new TrainingExample("{{file_id}}");
        te.addLocation(26952, 27310);

        TrainingExample[] td = new TrainingExample[]{te};

        stubFor(post(String.format("/fields/%s/train", fieldId))
                .withRequestBody(equalToJson(postRequestBody))
                .willReturn(aResponse().withStatus(202).withBody(postResponseBody)));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
        TrainingRequest request = TrainingRequest.createTrainingRequest(client, fieldId, td);

        assertEquals(requestId, request.requestId);

        //Test checking the status of the request
        String statusResponseBody = TestHelpers.resourceAsString(this, "training-request-complete.json");
        stubFor(get(String.format("/fields/%s/train/%s", fieldId, requestId))
                .willReturn(ok().withBody(statusResponseBody)));

        assertEquals("complete", request.getStatus());
    }

    @Test
    void requestTrainingErrorTest(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        int port = wmRuntimeInfo.getHttpPort();
        String fieldId = "2efa79d4-854d-46de-8087-f70778157dbf";
        String requestId = "c71qfbbo2ua8dqja2d70";

        String postRequestBody = TestHelpers.resourceAsString(this, "training-request.json");
        String postResponseBody = TestHelpers.resourceAsString(this, "training-request-error.json");

        ObjectMapper mapper = new ObjectMapper();
        TrainingExample[] td = mapper.readerForArrayOf(TrainingExample.class).readValue(postRequestBody);

        stubFor(post(String.format("/fields/%s/train", fieldId))
                .withRequestBody(equalToJson(postRequestBody))
                .willReturn(aResponse().withStatus(409).withBody(postResponseBody)));

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");

        assertThrows(Exception.class, () -> {
            TrainingRequest.createTrainingRequest(client, fieldId, td);
        });
    }

    @Test
    void testRequestNotFound(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

        int port = wmRuntimeInfo.getHttpPort();
        String fieldId = "2efa79d4-854d-46de-8087-f70778157dbf";
        String requestId = "c71qfbbo2ua8dqja2d70";

        ZdaiHttpClient client = new ZdaiHttpClient("http://localhost:" + port, "my-token");
        TrainingRequest request = new TrainingRequest(client, fieldId, requestId);

        assertEquals(fieldId, request.fieldId);
        assertEquals(requestId, request.requestId);

        //Test checking the status of the request
        String statusResponseBody = TestHelpers.resourceAsString(this, "request-not-found.json");
        stubFor(get(String.format("/fields/%s/train/%s", fieldId, requestId))
                .willReturn(notFound().withBody(statusResponseBody)));

        assertThrows(ZdaiApiException.class, request::getStatus);
    }
}