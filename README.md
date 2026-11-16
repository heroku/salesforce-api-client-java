# salesforce-api-client-java

A small Java client for the Salesforce REST API that supports querying, creating, updating and deleting records, and committing multiple operations in a single transaction via a unit of work.

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

## License

BSD-3-Clause, see [LICENSE.txt](LICENSE.txt).
