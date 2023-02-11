package ai.zuva.docai.normalization;

import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Normalization {
  public String text;

  // This class is used internally to construct the JSON body for the POST /normalization request
  static class NormalizationBody {
    @JsonProperty("text")
    public String text;

    public NormalizationBody(String text) {
      this.text = text;
    }
  }

  /**
   * Sends a get request for currency normalization
   *
   * @param client The client to use to make the request
   * @param text The text to normalize for currency
   * @return A CurrencyNormalizationResults, which contains the results of the currency
   *     normalization
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static CurrencyNormalizationResults getCurrency(DocAIClient client, String text)
      throws DocAIClientException, DocAIApiException {
    CurrencyNormalizationResults resp =
        client.authorizedJsonRequest(
            "POST",
            "api/v2/normalization/currency",
            new NormalizationBody(text),
            200,
            CurrencyNormalizationResults.class);
    return resp;
  }

  /**
   * Sends a get request for date normalization
   *
   * @param client The client to use to make the request
   * @param text The text to normalize for date
   * @return A DateNormalizationResults, which contains the results of the date normalization
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static DateNormalizationResults getDate(DocAIClient client, String text)
      throws DocAIClientException, DocAIApiException {
    DateNormalizationResults resp =
        client.authorizedJsonRequest(
            "POST",
            "api/v2/normalization/date",
            new NormalizationBody(text),
            200,
            DateNormalizationResults.class);
    return resp;
  }

  /**
   * Sends a get request for duration normalization
   *
   * @param client The client to use to make the request
   * @param text The text to normalize for duration
   * @return A DurationNormalizationResults, which contains the results of the duration
   *     normalization
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static DurationNormalizationResults getDuration(DocAIClient client, String text)
      throws DocAIClientException, DocAIApiException {
    DurationNormalizationResults resp =
        client.authorizedJsonRequest(
            "POST",
            "api/v2/normalization/duration",
            new NormalizationBody(text),
            200,
            DurationNormalizationResults.class);
    return resp;
  }
}
