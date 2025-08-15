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
        <version>0.0.1</version>
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
        <version>0.0.1</version>
    </dependency>
</dependencies>
```
> **Note:** JitPack uses a different `groupId` format (`com.github.{USERNAME}`). The version number corresponds to a GitHub release tag.

## Usage

Here is a basic example of how to use the AuthZEN Java Client to perform an access evaluation check.

### 1. Configure the Client

First, configure the client with your Policy Decision Point (PDP) endpoint and authentication credentials.

```java
import io.github.kkakui.azc.AuthzClient;
import io.github.kkakui.azc.config.DefaultAuthzClientConfig;

// Configure the client with your PDP endpoint and API key
DefaultAuthzClientConfig config = new DefaultAuthzClientConfig.Builder()
    .endpoint("https://pdp.mycompany.com/access/v1/evaluation")
    .apiKey("your-secret-api-key")
    .build();

// Create the client instance
AuthzClient client = new AuthzClient(config);
```

### 2. Build the Authorization Request

Next, build the components of your authorization request: the subject, action, and resource.

```java
import io.github.kkakui.azc.entity.Action;
import io.github.kkakui.azc.entity.Decision;
import io.github.kkakui.azc.entity.Resource;
import io.github.kkakui.azc.entity.Subject;

// Define the subject asking for access
Subject subject = new Subject.Builder("user", "alice@acmecorp.com").build();

// Define the resource they want to access
Resource resource = new Resource.Builder("document", "report-123").build();

// Define the action they want to perform
Action action = new Action.Builder("view").build();
```

### 3. Evaluate the Decision

Finally, call the `evaluate` method and check the decision.

```java
try {
    Decision decision = client.evaluate(subject, resource, action);

    if (decision.isAllowed()) {
        System.out.println("Access granted!");
        // Proceed with the operation
    } else {
        System.out.println("Access denied.");
        // Optionally, inspect the context for reasons
        decision.getContext().ifPresent(context -> {
            System.out.println("Reason: " + context);
        });
    }
} catch (Exception e) {
    System.err.println("An error occurred during authorization: " + e.getMessage());
    // Handle exceptions (e.g., network issues, server errors)
}
```

## Limitations

This client is currently in an early stage of development and has the following limitations:

*   **Draft API Version:** It is based on [a draft version of the AuthZEN Authorization API 1.0](https://github.com/kkakui/azc/blob/main/docs/authorization-api-1_0_draft_04.md). The API specification is subject to change, which may require updates to this client.
*   **Partial API Implementation:** Currently, only the Access Evaluation API is implemented. The bulk Access Evaluations API and the various Search APIs are not yet supported.
*   **Transport Protocol:** The client only supports HTTP/HTTPS for transport. Other transport bindings like gRPC are not available.

## License

This project is licensed under the terms of the [MIT License](LICENSE).