```markdown
# agent.md

## Mission

You are assisting in completing a backend coding challenge using **Java + Spring Boot + PostgreSQL**.

Your role is to:

- Help design architecture
- Generate production-quality code
- Ensure the solution is complete, runnable, and tested
- Work **iteratively in small steps** instead of generating a full project blindly

Always prioritise:

- correctness
- clarity
- minimal rewrites
- incremental progress

---

# General Rules

1. **Do not generate the full solution at once.**  
   Work only on the current phase.

2. **Ask clarification questions when requirements are ambiguous.**

3. **Follow clean architecture principles**
    - Controller
    - Service
    - Repository
    - DTO
    - Domain entities

4. **Prefer explicit design before implementation**
    - Data model first
    - API contracts second
    - Business logic last

5. **Generated code must compile.**

6. **Use best practices**
    - Bean Validation
    - Constructor injection
    - Lombok where appropriate
    - Proper HTTP status codes
    - Integration tests with Testcontainers

7. **Never invent requirements not present in the problem statement.**

---

# Interaction Protocol

When working with the user:

- Wait for instructions for the current phase
- Output **only what was requested**
- Do not jump ahead to later phases
- Provide **brief explanations when necessary**
- If multiple design options exist, explain trade-offs briefly

---

# Execution Phases

## Phase 0 — Decode the Problem

Goal: Understand requirements before writing code.

Prompt template:

```

Here is the coding challenge:

The coding task is to create a simple payment application.

- Your application should provide an API to create a payment, accepting first name, last name, zip code, and card
  number. The card number should be encrypted when stored on file.
- This application should be able to allow registering dynamic webhooks via an API which will receive the corresponding
  endpoint HTTP to make a POST request. This webhook should be called after each new payment, passing details as Json
  content in the body. This process should be resilient to failure.
- For API documentation, create an OpenAPI Specification and store it with examples at the root of your project. Please
  ensure proper return codes and meaningful information.

Identify:

1. Functional requirements
2. Non-functional requirements
3. Edge cases
4. Ambiguities that require clarification

```

Expected output:

- Structured requirements list
- Clarifying questions


---

## Phase 1 — Project Scaffold

Goal: Create a solid project structure before writing code.

Prompt template:

```

Generate a Spring Boot project scaffold including:

* pom.xml
* dependency list
* package structure

Requirements:

Java 17+
Spring Boot
Spring Web
Spring Data JPA
PostgreSQL
Flyway
Lombok
Testcontainers
JUnit

Provide the complete package layout.

```

Expected output:

- `pom.xml`
- directory tree
- dependency explanation


---

## Phase 2 — Domain & Database Design

Goal: Lock in the data model before writing services.

Prompt template:

```

Design the domain model and database schema.

Generate:

1. JPA entities
2. Flyway migration SQL
3. relationships
4. indexes and constraints

Ensure the model supports the requirements.

```

Expected output:

- Entity classes
- SQL migration scripts


---

## Phase 3 — Payment API

Goal: Implement the payment creation functionality.

Work **one vertical slice at a time**.

Prompt template:

```

Generate the full vertical slice for the Create Payment endpoint.

Include:

* Controller
* Service
* Repository
* Request DTO
* Response DTO
* Mapper

Follow clean architecture.
Use validation annotations on request DTOs.

```

Expected output:

- Fully working endpoint implementation


---

## Phase 4 — Webhook System

This phase is split into two parts.


### Phase 4a — Webhook Registration API

Goal: Allow clients to register webhook URLs.

Prompt template:

```

Generate CRUD endpoints for webhook registration.

Include:

* Controller
* Service
* Repository
* Request/response DTOs
* JPA entity

Ensure URLs are unique.

```

Expected output:

- webhook entity
- registration endpoints


### Phase 4b — Webhook Delivery Mechanism

Goal: Deliver events asynchronously.

Prompt template:

```

Design and implement the webhook delivery system.

Requirements:

* Trigger when a payment is created
* Send POST requests to registered webhook URLs
* Include event payload
* Use asynchronous processing
* Implement retry logic

```

Expected output:

- event publisher
- webhook dispatcher
- retry strategy
- event payload structure


---

## Phase 5 — Validation & Error Handling

Prompt template:

```

Add global error handling and validation.

Implement:

* @ControllerAdvice
* custom exceptions
* consistent API error response format
* proper HTTP status codes

```

Expected output:

- global exception handler
- error response DTO


---

## Phase 6 — Tests

Prompt template:

```

Generate integration tests.

Requirements:

* SpringBootTest
* Testcontainers PostgreSQL
* Happy path tests
* Failure case per endpoint

```

Expected output:

- integration test classes
- container setup


---

## Phase 7 — Runability

Prompt template:

```

Make the project runnable locally.

Generate:

* docker-compose.yml
* README.md
* example curl requests

```

Expected output:

- docker setup
- project documentation
- API usage examples


---

# Output Quality Requirements

All generated code must:

- Compile
- Follow standard Spring Boot conventions
- Use constructor injection
- Avoid unnecessary abstractions
- Be production-ready
- Be readable and well structured
```
