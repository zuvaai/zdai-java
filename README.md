# Zuva DocAI Java SDK

This SDK provides a Java wrapper around the [Zuva DocAI API](https://zuva.ai/documentation/api-reference/).

### Obtaining a Token

1. Make an account at [zuva.ai](https://zuva.ai/)
2. In the Dashboard, navigate to DocAI and select a region
3. Click Create Token and save your token somewhere safe (you will not be able to view it again)

### Quick start

Start by instantiating a `DocAIApiClient` with the url of the Zuva region you are using and your token:

```java
DocAIApiClient client = new DocAIApiClient(url, token);
```

Submit your file to Zuva:

```java
File file = File.submitFile(client, Paths.get(fileName));
```

Obtain field IDs of the fields you are interested in from the [field library](https://docai.zuva.ai/field-library),
or from the [GET fields endpoint](https://zuva.ai/documentation/api-reference/fields/get-fields/), and submit
a field extraction request:

```java
String[] fieldIds = new String[]{
		"668ee3b5-e15a-439f-9475-05a21755a5c1",
		"f743f363-1d8b-435b-8812-204a6d883834",
		"4d34c0ac-a3d4-4172-92d0-5fad8b3860a7"
		};
ExtractionRequest extractionRequest = ExtractionRequest.createRequest(client, file, fieldIds);
```

DocAI field extraction works asynchronously: you will need to poll the status of the request until it completes.

```java
long tStart = Instant.now().toEpochMilli();
System.out.print("Wait for processing");
while (Instant.now().toEpochMilli() - tStart < 300000) {
	String status = extractionRequest.getStatus();
	System.out.print(".");
	if (status.equals("complete") || status.equals("failed")) {
		System.out.println(status);
		break;
	}
	Thread.sleep(1000);
}
```

If the status is `complete`, you can now retrieve the results, which are grouped by field. Note
that there may be multiple results for any particular field, so a nested `for` loop is required to walk
the full set of results.

```java
if (status.equals("complete")) {
	ExtractionResults[] extractions = extractionRequest.getResults();

	for (ExtractionResults ex : extractions) {
		FieldMetadata fm = (new Field(client, ex.fieldId)).getFieldMetadata();
		System.out.println(String.format("%s:", fm.name));

		for (ExtractionData ed : ex.extractions) {
			System.out.println("> " + ed.text);
		}
	}
}
```

## Example

A command line demo is provided in the [examples folder](src/main/java/ai/zuva/example). The demo exercises
the document analysis workflow, including uploading a document, determining its document type and language, extracting text fields
and fetching the results of OCR. The example expect certain environment variables to be set:

- `DOCAI_URL`: the URL of the Zuva environment to connect to
- `DOCAI_TOKEN`: your token for the Zuva environment
- `ZUVA_DEMO_DOC`: the path to the demo document to analyze (defaults to "CANADAGOOS-F1Securiti-2152017.PDF" in the current working directory)

## Exceptions

The general `DocAIException` class has two subclasses:
- `DocAIApiException` captures unexpected status codes (4xx, 5xx) returned from the API request and includes
the status code and response from the server
- `DocAIClientException` captures all other errors and always includes the original `cause` (e.g. `IOException`,`JsonProcessingException` etc.)

## HTTP Library

The client currently uses [OkHttp](https://square.github.io/okhttp/), which is compatible with Java 8+. This dependency is
isolated to `ai.zuva.api`, which can be rewritten to use your http library of choice.
