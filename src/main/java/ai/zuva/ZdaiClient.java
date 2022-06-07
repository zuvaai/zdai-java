package ai.zuva;

import ai.zuva.classification.ClassificationRequest;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.extraction.ExtractionRequest;
import ai.zuva.fields.Field;
import ai.zuva.fields.FieldListElement;
import ai.zuva.fields.TrainingExample;
import ai.zuva.fields.TrainingRequest;
import ai.zuva.files.ZdaiFile;
import ai.zuva.http.ZdaiHttpClient;
import ai.zuva.language.LanguageRequest;
import ai.zuva.ocr.OcrRequest;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class ZdaiClient {
    public ZdaiHttpClient client;

    public ZdaiClient(String baseURL, String token) {
        client = new ZdaiHttpClient(baseURL, token);
    }

    public ZdaiFile submitFile(Path p, String... contentType) throws ZdaiClientException, ZdaiApiException, FileNotFoundException, SecurityException {
        return ZdaiFile.submitFile(client, p, contentType);
    }

    public ZdaiFile submitFile(String s, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return ZdaiFile.submitFile(client, s, contentType);
    }

    public ZdaiFile submitFile(byte[] ba, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return ZdaiFile.submitFile(client, ba, contentType);
    }

    public ClassificationRequest newDocClassifierRequest(ZdaiFile file) throws ZdaiClientException, ZdaiApiException {
        return ClassificationRequest.createClassificationRequest(client, file);
    }

    public LanguageRequest newLanguageRequest(ZdaiFile file) throws ZdaiClientException, ZdaiApiException {
        return LanguageRequest.createLanguageRequest(client, file);
    }

    public OcrRequest newOcrRequest(ZdaiFile file) throws ZdaiClientException, ZdaiApiException  {
        return OcrRequest.createOcrRequest(client, file);
    }

    public ExtractionRequest newExtractionRequest(ZdaiFile file, String[] fieldIds) throws ZdaiClientException, ZdaiApiException  {
        return ExtractionRequest.createExtractionRequest(client, file, fieldIds);
    }

    public TrainingRequest newTrainingRequest(String fieldId, TrainingExample[] trainingExamples) throws ZdaiClientException, ZdaiApiException {
        return TrainingRequest.createTrainingRequest(client, fieldId, trainingExamples);
    }

    public Field field(String fieldId) {
        return new Field(client, fieldId);
    }

    public FieldListElement[] listFields(ZdaiHttpClient client) throws ZdaiClientException, ZdaiApiException {
        return Field.listFields(client);
    }

    public Field createField(String name, String description) throws ZdaiClientException, ZdaiApiException {
        return Field.createField(client, name, description);
    }
}
