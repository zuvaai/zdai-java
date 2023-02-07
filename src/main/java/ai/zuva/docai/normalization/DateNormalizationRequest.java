package ai.zuva.docai.normalization;

import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;

public class DateNormalizationRequest extends NormalizationRequest {
  /**
   * Sends a request for date normalization
   *
   * @param client The client to use to make the request
   * @param text The text to normalize for date
   * @return A DateNormalizationResults, which contains the results of the date normalization
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static DateNormalizationResults createRequest(DocAIClient client, String text)
      throws DocAIClientException, DocAIApiException {
    DateNormalizationResults resp =
        client.authorizedJsonRequest(
            "POST",
            "api/v2/normalization/date",
            new NormalizationRequestBody(text),
            200,
            DateNormalizationResults.class);
    return resp;
  }
}
