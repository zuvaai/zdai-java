package ai.zuva.docai.normalization;

import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;

public class CurrencyNormalizationRequest extends NormalizationRequest {
  /**
   * Sends a request for currency normalization
   *
   * @param client The client to use to make the request
   * @param text The text to normalize for currency
   * @return A CurrencyNormalizationResults, which contains the results of the currency
   *     normalization
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static CurrencyNormalizationResults createRequest(DocAIClient client, String text)
      throws DocAIClientException, DocAIApiException {
    CurrencyNormalizationResults resp =
        client.authorizedJsonRequest(
            "POST",
            "api/v2/normalization/currency",
            new NormalizationRequestBody(text),
            200,
            CurrencyNormalizationResults.class);
    return resp;
  }
}
