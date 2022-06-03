package ai.zuva;

import ai.zuva.classification.ClassificationRequest;
import ai.zuva.extraction.ExtractionRequest;
import ai.zuva.fields.FieldService;
import ai.zuva.fields.TrainingExample;
import ai.zuva.fields.TrainingRequest;
import ai.zuva.files.FileService;
import ai.zuva.http.ZdaiHttpClient;
import ai.zuva.language.LanguageRequest;
import ai.zuva.ocr.OcrRequest;

public class ZdaiClient {
    private ZdaiHttpClient client;

    public ZdaiClient(String baseURL, String token) {
        client = new ZdaiHttpClient(baseURL, token);
    }

    public ClassificationRequest newDocClassifierRequest(String fileId) throws Exception {
        return new ClassificationRequest(client, fileId);
    }

    public LanguageRequest newLanguageRequest(String fileId) throws Exception {
        return new LanguageRequest(client, fileId);
    }

    public OcrRequest newOcrRequest(String fileId) throws Exception {
        return new OcrRequest(client, fileId);
    }

    public ExtractionRequest newExtractionRequest(String fileId, String[] fieldIds) throws Exception {
        return new ExtractionRequest(client, fileId, fieldIds);
    }

    public TrainingRequest newTrainingRequest(String fieldId, TrainingExample[] trainingExamples) throws Exception {
        return new TrainingRequest(client, fieldId, trainingExamples);
    }
    public FileService newFileService() {
        return new FileService(client);
    }

    public FieldService newFieldService() {
        return new FieldService(client);
    }
}
