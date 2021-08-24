# Client Sajari service

This repository contains a Dockerised process that with minimal configuration will allow you to integrate with the Sajari search service.

# Building the application

This application uses Gradle as a build tool. Simply run `gradle build` to build the project, or if using Docker
you can run the Docker file.

You will need to create a _gradle.properties_ with a valid Github personal access token with package access. More information is available here [using a published package](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package) and here [creating a personal access token](https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token) 

####**_gradle.properties_**
```properties
gpr.user=githubusername
gpr.key=ghp_personal_access_token
```

Update the [application.yml](../main/src/main/resources/application.yml) to correctly reference your Sajari collection and with the appropriate collection-id, key-id and key-secret. You will also need to provide a reference to the Google Product Feed.

```yaml
sajari:
  key-id: key-id-from-sajari-app
  key-secret: key-secret-from-sajari-app
  collection-id: google-feed-collection
  api-url: https://api-gateway.sajari.com

customer:
  url: https://www.mywebsite.com/feed/google.xml

```
These can also be passed as environment variables:

|Key|Value|
|----|----|
|key.id|See [Console credentials page](https://app.sajari.com/project/credentials)|
|key.secret|See [Console credentials page](https://app.sajari.com/project/credentials)|
|collection.id|The Sajari collection id you are working with| 
|google.product.feed.url|Google product feed endpoint|

# Deploying the application

######//TODO 
Docker instructions for Google (Cloud Run) .. perhaps deploying via Github actions and AWS.