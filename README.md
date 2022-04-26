# Welcome to Parkeer Assistent

This project consist of:
- a backend (Kotlin)
- a frontend (Kotlin/React)
- an Android app (Kotlin)
- an iOS app (Swift)

## Backend

### How to deploy

```
heroku login
heroku container:login
heroku container:push web -a parkeer-assistent-staging
heroku container:release web -a parkeer-assistent-staging
```

### Setup Elasticsearch

Provision an Elasticsearch cluster and create an index

```
curl -X PUT <elasticsearch-url>/parkeer-assistent-staging
```