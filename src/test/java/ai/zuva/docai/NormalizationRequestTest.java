package ai.zuva.docai;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.zuva.docai.normalization.*;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

@WireMockTest
public class NormalizationRequestTest {

  @Test
  void theTest(WireMockRuntimeInfo wmRuntimeInfo) throws RuntimeException {
    try {
      int port = wmRuntimeInfo.getHttpPort();
      String currencyText = "The rent is two thousand three hundred forty seven dollars";
      String currencyValue = "2347.00";
      int currencyPrecision = 2;
      String currencySymbol = "dollars";

      // Test constructing and sending a request for a currency normalization text
      String postCurrencyResponseBody =
          TestHelpers.resourceAsString(this, "normalization-currency.json");
      stubFor(
          post("/api/v2/normalization/currency")
              .withRequestBody(
                  equalToJson(
                      "{\"text\":\"The rent is two thousand three hundred forty seven dollars\"}"))
              .willReturn(aResponse().withStatus(200).withBody(postCurrencyResponseBody)));

      DocAIClient client = new DocAIClient("http://localhost:" + port, "my-token");
      CurrencyNormalizationResults currencyResult = Normalization.getCurrency(client, currencyText);

      assertEquals(currencyText, currencyResult.text);
      assertEquals(currencyValue, currencyResult.currency[0].value);
      assertEquals(currencyPrecision, currencyResult.currency[0].precision);
      assertEquals(currencySymbol, currencyResult.currency[0].symbol);

      // Test constructing and sending a request for a duration normalization text
      String durationText = "The contract expires 4 months after termination";
      String durationUnit = "months";
      int durationValue = 4;

      String postDurationResponseBody =
          TestHelpers.resourceAsString(this, "normalization-duration.json");
      stubFor(
          post("/api/v2/normalization/duration")
              .withRequestBody(
                  equalToJson("{\"text\":\"The contract expires 4 months after termination\"}"))
              .willReturn(aResponse().withStatus(200).withBody(postDurationResponseBody)));

      DurationNormalizationResults durationResult = Normalization.getDuration(client, durationText);

      assertEquals(durationText, durationResult.text);
      assertEquals(durationValue, durationResult.duration[0].value);
      assertEquals(durationUnit, durationResult.duration[0].unit);

      // Test constructing and sending a request for a date normalization text
      String dateText = "The lease terminates on Monday, November 28, 2022";
      int dateDay = 28;
      int dateMonth = 11;
      int dateYear = 2022;

      String postDateResponseBody = TestHelpers.resourceAsString(this, "normalization-date.json");
      stubFor(
          post("/api/v2/normalization/date")
              .withRequestBody(
                  equalToJson("{\"text\":\"The lease terminates on Monday, November 28, 2022\"}"))
              .willReturn(aResponse().withStatus(200).withBody(postDateResponseBody)));

      DateNormalizationResults dateResult = Normalization.getDate(client, dateText);

      assertEquals(dateText, dateResult.text);
      assertEquals(dateDay, dateResult.date[0].day);
      assertEquals(dateMonth, dateResult.date[0].month);
      assertEquals(dateYear, dateResult.date[0].year);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
