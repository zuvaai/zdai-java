package ai.zuva.example;

import ai.zuva.*;
import ai.zuva.classification.ClassificationRequest;
import ai.zuva.extraction.ExtractionRequest;
import ai.zuva.fields.Field;
import ai.zuva.files.ZdaiFile;
import ai.zuva.language.LanguageRequest;
import ai.zuva.ocr.OcrRequest;
import ai.zuva.extraction.ExtractionData;
import ai.zuva.extraction.ExtractionResults;
import ai.zuva.fields.FieldMetadata;

import java.io.FileOutputStream;
import java.nio.file.Paths;


public class Example {
    public static void main(String[] args) throws Exception {
        String token = System.getenv("ZUVA_TOKEN");
        if (token == null) {
            throw(new Exception("Environment variable ZUVA_TOKEN not set"));
        }

        String url = System.getenv("ZUVA_URL");
        if (url == null) {
            throw(new Exception("Environment variable ZUVA_URL not set"));
        }

        String documentPath = System.getenv("ZUVA_DEMO_DOC");
        if (documentPath == null) {
            // default to looking for the demo file in the current working directory
            documentPath = "CANADAGOOS-F1Securiti-2152017.PDF";
        }

        ZdaiClient client = new ZdaiClient(url, token);

        ZdaiFile zdaiFile = client.submitFile(Paths.get(documentPath));
        System.out.println(String.format("Uploaded file with id %s expires at %s", zdaiFile.fileId, zdaiFile.expiration));

        System.out.println("\nClassifying Document type:");
        ClassificationRequest classificationRequest = client.newDocClassifierRequest(zdaiFile.fileId);
        System.out.println("Request ID: " + classificationRequest.requestId);

        String status = StatusChecker.waitForStatus(() -> classificationRequest.getClassificationResult().status, 1, 60);
        if (status.equals("complete")) {
            System.out.println("Document type is: " + classificationRequest.getClassificationResult().classification);
        }
        else {
            System.out.println("Classification failed.");
        }

        System.out.println("\nDetermining Document Language:");
        LanguageRequest languageRequest = client.newLanguageRequest(zdaiFile.fileId);
        System.out.println("Request ID: " + languageRequest.requestId);

        status = StatusChecker.waitForStatus(() -> languageRequest.getResult().status, 1, 60);
        if (status.equals("complete")) {
            System.out.println("Document language is: " + languageRequest.getResult().language);
        }
        else {
            System.out.println("Classification failed.");
        }

        System.out.println("\nPerforming Field extraction");
        String[] fieldIds = new String[]{
                "668ee3b5-e15a-439f-9475-05a21755a5c1",
                "f743f363-1d8b-435b-8812-204a6d883834",
                "4d34c0ac-a3d4-4172-92d0-5fad8b3860a7"
        };

        ExtractionRequest extractionRequest = client.newExtractionRequest(zdaiFile.fileId, fieldIds);
        System.out.println("Request ID: " + extractionRequest.requestId);

        status = StatusChecker.waitForStatus(() -> extractionRequest.getStatus(), 1, 60);
        if (status.equals("complete")) {
            ExtractionResults[] extractions = extractionRequest.getResults();


            for (ExtractionResults ex : extractions) {
                FieldMetadata fm = client.field(ex.fieldId).getMetadata();
                System.out.println(String.format("%s:", fm.name));

                for (ExtractionData ed : ex.extractions) {
                    System.out.println("> " + ed.text);
                }
            }
        }

        System.out.println("\nObtaining OCR results:");
        OcrRequest ocrRequest = client.newOcrRequest(zdaiFile.fileId);
        System.out.println("Request ID: " + ocrRequest.requestId);

        status = StatusChecker.waitForStatus(() -> ocrRequest.getStatus(), 1, 60);
        if (status.equals("complete")) {
            System.out.println(String.format("Character count: %d", ocrRequest.getText().length()));
            System.out.println("Downloading and saving images as temp.zip");

            try (FileOutputStream outputStream = new FileOutputStream("temp.zip")) {
                outputStream.write(ocrRequest.getImages());
            }
        }
        System.out.println("\nDeleting file from server.");
        zdaiFile.delete();
    }
}