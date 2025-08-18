# azc: A Minimal AuthZEN Java Client

[![Maven Package](https://github.com/kkakui/azc/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/kkakui/azc/actions/workflows/maven-publish.yml)
[![JitPack](https://jitpack.io/v/com.github.kkakui/azc.svg)](https://jitpack.io/#com.github.kkakui/azc)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A lightweight Java client for the [AuthZEN Authorization API 1.0](https://github.com/kkakui/azc/blob/main/docs/authorization-api-1_0_draft_04.md), designed with minimal dependencies for easy integration.

## Installation

You can add this library to your project using Maven from either GitHub Packages or JitPack.

### Option 1: Using GitHub Packages

To consume this package from GitHub Packages, you need to authenticate.

**1. Configure Authentication**

Add a server entry to your `~/.m2/settings.xml` file. You'll need to create a Personal Access Token (PAT) with the `read:packages` scope.

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```

**2. Add to your `pom.xml`**

Add the repository and dependency to your project's `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub kkakui Apache Maven Packages</name>
        <url>https://maven.pkg.github.com/kkakui/azc</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.github.kkakui</groupId>
        <artifactId>azc</artifactId>
        <version>0.0.2</version>
    </dependency>
</dependencies>
```

### Option 2: Using JitPack

JitPack is a simpler alternative that does not require authentication for public repositories.

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.kkakui</groupId>
        <artifactId>azc</artifactId>
        <version>0.0.2</version>
    </dependency>
</dependencies>
```
> **Note:** JitPack uses a different `groupId` format (`com.github.{USERNAME}`). The version number corresponds to a GitHub release tag.

## Usage

Here is a basic example of how to use the AuthZEN Java Client to perform an access evaluation check.

### 1. Configure the Client

First, configure the client with your Policy Decision Point (PDP) endpoint and authentication credentials.

```java
import io.github.kkakui.azc.api.AuthzClient;
import io.github.kkakui.azc.config.DefaultAuthzClientConfig;
import io.github.kkakui.azc.transport.http.SimpleHttpClient;

// Configure the client with your PDP endpoint and API key
DefaultAuthzClientConfig config = DefaultAuthzClientConfig.builder()
    .endpoint("https://pdp.mycompany.com/access/v1/evaluation")
    .apiKey("your-secret-api-key")
    // The API key header can be customized via .apiKeyHeader("X-API-Key").
    // If not set, it defaults to "Authorization".
    .build();

// Create the client instance
AuthzClient client = new AuthzClient(config, new SimpleHttpClient());
```

### 2. Build the Authorization Request

Next, build the components of your authorization request: the subject, action, and resource.

```java
import io.github.kkakui.azc.api.AuthorizationRequest;
import io.github.kkakui.azc.model.Action;
import io.github.kkakui.azc.model.Resource;
import io.github.kkakui.azc.model.Subject;

// Define the subject asking for access
Subject subject = new Subject.Builder()
    .type("user")
    .id("alice@acmecorp.com")
    .build();

// Define the resource they want to access
Resource resource = new Resource.Builder()
    .type("document")
    .id("report-123")
    .build();

// Define the action they want to perform
Action action = new Action.Builder()
    .name("view")
    .build();

// Assemble the authorization request
AuthorizationRequest request = new AuthorizationRequest.Builder()
    .subject(subject)
    .resource(resource)
    .action(action)
    .build();
```

### 3. Evaluate the Decision

Finally, call the `authorize` method and check the decision.

```java
import io.github.kkakui.azc.api.AuthorizationResponse;
import io.github.kkakui.azc.exception.AuthorizationException;
import io.github.kkakui.azc.exception.TransportException;
import java.util.Map;

try {
    AuthorizationResponse response = client.authorize(request);

    if (response.isAllowed()) {
        System.out.println("Access granted!");
        // Proceed with the operation
    } else {
        System.out.println("Access denied.");
        // Optionally, inspect the context for reasons
        Map<String, Object> context = response.getContext();
        if (context != null && !context.isEmpty()) {
            System.out.println("Reason: " + context));
        }
    }
} catch (AuthorizationException e) {
    System.err.println("Authorization failed: " + e.getMessage());

    // Check the cause to distinguish between different failure types
    if (e.getCause() instanceof TransportException) {
        System.err.println("This was a network/transport error.");
    } else {
        // Handle other exceptions
    }
}
```

## Limitations

This client is currently in an early stage of development and has the following limitations:

*   **Draft API Version:** It is based on [a draft version of the AuthZEN Authorization API 1.0](https://github.com/kkakui/azc/blob/main/docs/authorization-api-1_0_draft_04.md). The API specification is subject to change, which may require updates to this client.
*   **Partial API Implementation:** Currently, only the Access Evaluation API is implemented. The bulk Access Evaluations API and the various Search APIs are not yet supported.
*   **Transport Protocol:** The client only supports HTTP/HTTPS for transport. Other transport bindings like gRPC are not available.

## License

This project is licensed under the terms of the [MIT License](LICENSE).