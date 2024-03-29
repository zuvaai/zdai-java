package ai.zuva.docai.example;

import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.classification.ClassificationRequest;
import ai.zuva.docai.classification.ClassificationResult;
import ai.zuva.docai.extraction.ExtractionData;
import ai.zuva.docai.extraction.ExtractionRequest;
import ai.zuva.docai.extraction.ExtractionResults;
import ai.zuva.docai.extraction.ExtractionStatus;
import ai.zuva.docai.fields.Field;
import ai.zuva.docai.fields.FieldListElement;
import ai.zuva.docai.files.File;
import ai.zuva.docai.language.LanguageRequest;
import ai.zuva.docai.language.LanguageResult;
import ai.zuva.docai.mlc.MLCRequest;
import ai.zuva.docai.mlc.MLCResult;
import ai.zuva.docai.ocr.OcrRequest;
import ai.zuva.docai.ocr.OcrStatus;
import java.io.FileOutputStream;
import java.util.HashMap;

public class Example {
  public static void main(String[] args) throws Exception {
    String token = System.getenv("DOCAI_TOKEN");
    if (token == null) {
      throw (new Exception("Environment variable DOCAI_TOKEN not set"));
    }

    String url = System.getenv("DOCAI_URL");
    if (url == null) {
      throw (new Exception("Environment variable DOCAI_URL not set"));
    }

    String documentPath = System.getenv("ZUVA_DEMO_DOC");
    if (documentPath == null) {
      // default to looking for the demo file in the current working directory
      documentPath = "CANADAGOOS-F1Securiti-2152017.PDF";
    }

    DocAIClient client = new DocAIClient(url, token);

    File file = File.submitFile(client, new java.io.File(documentPath));
    System.out.println(
        String.format("Uploaded file with id %s expires at %s", file.fileId, file.expiration));

    System.out.printf("%nObtaining OCR results:%n");
    OcrRequest ocrRequest = OcrRequest.createRequest(client, file);
    System.out.println("Request ID: " + ocrRequest.requestId);

    OcrStatus ocrStatus = ocrRequest.pollStatus(1, 60, true);
    if (ocrStatus.isComplete()) {
      System.out.println(String.format("Character count: %d", ocrRequest.getText().length()));
      System.out.println("Downloading and saving images as temp.zip");

      try (FileOutputStream outputStream = new FileOutputStream("temp.zip")) {
        outputStream.write(ocrRequest.getImages());
      }
    }

    System.out.printf("%nClassifying Document type:%n");
    ClassificationRequest classificationRequest = ClassificationRequest.createRequest(client, file);
    System.out.println("Request ID: " + classificationRequest.requestId);

    ClassificationResult classificationResult = classificationRequest.pollStatus(1, 60, true);

    if (classificationResult.isComplete()) {
      System.out.println("Document type is: " + classificationResult.classification);
    } else {
      System.out.println("Classification failed.");
    }

    System.out.printf("%nDetermining MLC Classification:%n");
    MLCRequest mlcRequest = MLCRequest.createRequest(client, file);
    System.out.println("Request ID: " + mlcRequest.requestId);

    MLCResult mlcResult = mlcRequest.pollStatus(1, 60, true);

    if (mlcResult.isComplete()) {
      System.out.println("MLC classifications are: " + mlcResult.classifications);
    } else {
      System.out.println("MLC classification failed.");
    }

    System.out.printf("%nDetermining Document Language:%n");
    LanguageRequest languageRequest = LanguageRequest.createRequest(client, file);
    System.out.println("Request ID: " + languageRequest.requestId);

    LanguageResult languageResult = languageRequest.pollStatus(1, 60, true);
    if (languageResult.isComplete()) {
      System.out.println("Document language is: " + languageResult.language);
    } else {
      System.out.println("Classification failed.");
    }

    System.out.println("Getting the list of available fields");
    HashMap<String, FieldListElement> fieldMetadata = new HashMap<>();
    FieldListElement[] fields = Field.listFields(client);
    for (FieldListElement field : fields) {
      fieldMetadata.put(field.fieldId, field);
    }

    System.out.printf("%nPerforming Field extraction%n");
    String[] fieldIds =
        new String[] {
          "668ee3b5-e15a-439f-9475-05a21755a5c1",
          "f743f363-1d8b-435b-8812-204a6d883834",
          "4d34c0ac-a3d4-4172-92d0-5fad8b3860a7"
        };

    ExtractionRequest extractionRequest = ExtractionRequest.createRequest(client, file, fieldIds);
    System.out.println("Request ID: " + extractionRequest.requestId);

    ExtractionStatus extractionStatus = extractionRequest.pollStatus(1, 60, true);

    if (extractionStatus.isComplete()) {
      System.out.println("Getting results");
      ExtractionResults[] extractions = extractionRequest.getResults();
      System.out.println("Got results");

      for (ExtractionResults ex : extractions) {
        System.out.println(String.format("%s:", fieldMetadata.get(ex.fieldId).name));

        for (ExtractionData ed : ex.extractions) {
          System.out.println("> " + ed.text);
        }
      }
    }

    System.out.printf("%nDeleting file from server.%n");
    file.delete();
  }
}
