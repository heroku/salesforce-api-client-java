# salesforce-api-client-java

A small Java client for the Salesforce REST API that supports querying, creating, updating and deleting records, and committing multiple operations in a single transaction via a unit of work. It also runs Bulk API 2.0 ingest and query jobs.

## Installation

```xml
<dependency>
    <groupId>com.heroku</groupId>
    <artifactId>salesforce-api-client</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Requires Java 17 or newer.

## Usage

```java
DataApi dataApi =
    new DataApi(URI.create("https://example.my.salesforce.com"), "62.0", accessToken);

RecordQueryResult result = dataApi.query("SELECT Id, Name FROM Account");
for (Record record : result.getRecords()) {
  System.out.println(record.getStringField("Name").orElse("<no name>"));
}

RecordModificationResult created =
    dataApi.create(dataApi.newRecordBuilder("Account").withField("Name", "Acme").build());

UnitOfWorkBuilder unitOfWorkBuilder = dataApi.newUnitOfWorkBuilder();
ReferenceId accountId =
    unitOfWorkBuilder.registerCreate(
        dataApi.newRecordBuilder("Account").withField("Name", "Acme Subsidiary").build());

unitOfWorkBuilder.registerCreate(
    dataApi
        .newRecordBuilder("Contact")
        .withField("LastName", "Smith")
        .withField("AccountId", accountId)
        .build());

Map<ReferenceId, RecordModificationResult> results =
    dataApi.commitUnitOfWork(unitOfWorkBuilder.build());
```

Use `queryMore` to fetch the next batch of records while `RecordQueryResult#isDone()` returns `false`.

### Bulk API

`BulkApi` runs Bulk API 2.0 jobs for loading and extracting large amounts of data as CSV.

```java
BulkApi bulkApi =
    new BulkApi(URI.create("https://example.my.salesforce.com"), "62.0", accessToken);

IngestJobReference ingestJob = bulkApi.createIngestJob("Account", IngestOperation.INSERT);
try (InputStream csv = Files.newInputStream(Path.of("accounts.csv"))) {
  bulkApi.uploadJobData(ingestJob, csv);
}
bulkApi.closeJob(ingestJob);

IngestJobInfo ingestJobInfo = bulkApi.getJobInfo(ingestJob);
// Poll getJobInfo until the state is JOB_COMPLETE, FAILED or ABORTED.

try (InputStream failedRecords = bulkApi.getFailedRecordResults(ingestJob)) {
  // CSV with the error and the original data of each failed record.
}
```

Upserts need an external ID field, set with `newIngestJobOptionsBuilder().withExternalIdFieldName(...)`.

```java
QueryJobReference queryJob =
    bulkApi.createQueryJob("SELECT Id, Name FROM Account", QueryOperation.QUERY);
// Poll getJobInfo until the state is JOB_COMPLETE.

QueryJobResult page = bulkApi.getQueryJobResults(queryJob);
while (true) {
  try (QueryJobResult current = page) {
    // current.getData() is the CSV of this page.
    if (current.isDone()) {
      break;
    }
    page = bulkApi.getMoreQueryJobResults(current);
  }
}
```

Each `QueryJobResult` holds an open response and must be closed.

## License

BSD-3-Clause, see [LICENSE.txt](LICENSE.txt).
