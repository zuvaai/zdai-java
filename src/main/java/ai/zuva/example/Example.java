package ai.zuva.examples;

import ai.zuva.*;
import ai.zuva.classification.ClassificationRequest;
import ai.zuva.extraction.ExtractionRequest;
import ai.zuva.fields.FieldService;
import ai.zuva.files.FileService;
import ai.zuva.files.SubmitFileResponse;
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

        // default to looking for the demo file in the current working directory
        if (documentPath == null) {
            documentPath = "CANADAGOOS-F1Securiti-2152017.PDF";
        }

        ZdaiClient client = new ZdaiClient(url, token);
        FileService fileService = client.newFileService();
        FieldService fieldService = client.newFieldService();

        SubmitFileResponse fileDetails = fileService.submitFile(Paths.get(documentPath));

        System.out.println(String.format("Uploaded file with id %s expires at %s", fileDetails.fileId, fileDetails.expiration));

        String[] fileIds = new String[]{fileDetails.fileId};

        System.out.println("\nClassifying Document type:");

        ClassificationRequest classificationRequest = client.newDocClassifierRequest(fileIds[0]);
        System.out.println("Request ID: " + classificationRequest.requestId);

        String status = StatusChecker.waitForStatus(() -> classificationRequest.getClassificationResult().status, 1, 60);
        if (status.equals("complete")) {
            System.out.println("Document type is: " + classificationRequest.getClassificationResult().classification);
        }
        else {
            System.out.println("Classification failed.");
        }

        System.out.println("\nDetermining Document Language:");

        LanguageRequest languageRequest = client.newLanguageRequest(fileIds[0]);
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

        ExtractionRequest extractionRequest = client.newExtractionRequest(fileIds[0], fieldIds);
        System.out.println("Request ID: " + extractionRequest.requestId);

        status = StatusChecker.waitForStatus(() -> extractionRequest.getStatus(), 1, 60);
        if (status.equals("complete")) {
            ExtractionResults[] extractions = extractionRequest.getResults();


            for (ExtractionResults ex : extractions) {
                FieldMetadata fm = fieldService.getFieldMetadata(ex.fieldId);
                System.out.println(String.format("%s:", fm.name));

                for (ExtractionData ed : ex.extractions) {
                    System.out.println("> " + ed.text);
                }
            }
        }

        System.out.println("\nObtaining OCR results:");

        OcrRequest ocrRequest = client.newOcrRequest(fileIds[0]);
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
        fileService.deleteFile(fileIds[0]);
    }
}