# Welcome to Parkeer Assistent

This project consist of:
- a backend (Kotlin)
- a frontend (Kotlin/React)
- an Android app (Kotlin)
- an iOS app (Swift)

## Backend

### How to deploy

```


heroku container:push web -a parkeer-assistent-staging
heroku container:release web -a parkeer-assistent-staging
```

### Setup Elasticsearch

Provision an Elasticsearch cluster and create an index

```
curl -X PUT <elasticsearch-url>/parkeer-assistent-staging
```

Clean up old data

```POST /parkeer-assistent/_delete_by_query```
```json
{
  "query": {
    "range": {
      "date": {
        "lte": "2022-07-01T00:00:00"
      }
    }
  }
}
```
