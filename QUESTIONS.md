# Questions

Answer each question in your own words. Aim for three to eight sentences per answer —
enough to show that you understand the concept, not so much that you are restating a
textbook entry. We are looking for clarity of thinking, not exhaustive coverage.

---

## Java & Object-Oriented Design

**1.** The `Auditable` abstract class in this codebase uses `@MappedSuperclass`. Explain
what this annotation tells JPA, and describe what would happen to the database schema if
you removed it. Why is it better to put `createdAt` and `updatedAt` in a shared abstract
class rather than adding those fields directly to `Transaction` and `Account` separately?

```
1. @MappedSuperclass tells JPA to include the fields from the parent class in the child entity tables instead of creating a separate table for the parent class. 

2. If Transaction or Account extends this class, the createdAt and updatedAt columns will automatically be added to their tables.

3. If @MappedSuperclass is removed, these common fields will no longer be mapped correctly to the child entities. 
4. Keeping the audit fields in one shared class avoids writing the same code in multiple entities and makes it easier to maintain if the audit logic needs to change in the future.

```


**2.** `TransactionService` is defined as a Java interface, with `TransactionServiceImpl`
as its only implementation. A new engineer on the team asks: "Why bother with the interface
if there's only one implementation? Isn't it just extra boilerplate?" How do you respond?
Give at least one concrete scenario where the interface pays off.

```
1. I would say that the interface is useful even if there is only one implementation right now because it keeps the controller dependent on a interface(to follow SOLID principles : a class should dependent on interface not a class), not on a specific class. 

2. This makes the code easier to change later if another implementation is needed.

3. For example, today TransactionServiceImpl gets transactions from the database. Later, if we want to test the controller without using the real database, we can create a fake implementation of TransactionService that returns sample transactions. Since the controller depends on the TransactionService interface, we can switch the implementation without changing the controller code.

```


**3.** `Category` is modelled as an enum rather than a plain `String` field on
`Transaction`. What does storing it as `@Enumerated(EnumType.STRING)` in the database
actually produce in the table? What would go wrong if a future developer added a new
category value to the enum but forgot to handle database migration?

```
`@Enumerated(EnumType.STRING)` stores the enum value as text in the database, such as `FOOD`, `TRANSPORT`, or `UTILITIES`, instead of storing a number. This makes the data easier to read and safer than storing enum positions.

If a new category is added in the enum but the database or seed data is not handled properly, old code or existing validation may not recognize the new value correctly. It can also cause issues if reports, tests, or frontend code expect only the old category list. So whenever a new enum value is added, the related database data, API handling, and tests should also be checked.

```


**4.** `BudgetCalculator` is a `final` class with a private constructor and a single
static method. What pattern is this, and why is it appropriate for this specific utility?
In your implementation, what data structure did you use as an intermediate step before
building the final sorted map, and why?


```
This is a utility class pattern. The class is marked `final` so it cannot be extended, and the constructor is private so no one can create an object of it.

This is suitable for `BudgetCalculator` because it does not need to store any state. It only takes input transactions, calculates the result, and returns it.

In my implementation, I first used a `Map<Category, BigDecimal>` to group transactions by category and calculate the total spend for each category. After that, I sorted the map entries by amount in descending order and collected them into a `LinkedHashMap` so the final sorted order is preserved.
```
---

## Spring Boot & REST API Design

**5.** The original `POST /api/transactions` endpoint returned a `ResponseEntity<Transaction>`
rather than a `ResponseEntity<TransactionResponse>`. Explain specifically what was wrong
with this. What does a DTO (data transfer object) protect against, and what risks does
returning an entity directly introduce?

```
Returning Transaction directly from the controller was not ideal because it exposes the database entity as the API response. I changed it to return TransactionResponse so the API response is controlled and only contains the fields we want to expose.

A DTO protects the API from internal entity changes. If we return entities directly, future database fields or relationships may accidentally become part of the API response.
```


**6.** When a `POST` request arrives at `TransactionController`, describe the complete
journey from HTTP request to database insert. Name each layer the request passes through,
what each layer is responsible for, and what would happen if the `@Valid` annotation were
removed from the method parameter.

```
When a POST request comes in, the controller receives the JSON request and maps it to TransactionRequest. The controller passes it to the service, the service creates a Transaction entity, and the repository saves it to the database.

@Valid checks the request fields before the service is called. If @Valid is removed, invalid data like missing account ID or zero amount may reach the service layer.
```

**7.** Spring Boot uses `@RestController`, `@Service`, and `@Repository` as stereotype
annotations. They all ultimately do the same thing (register a bean). Why does Spring
provide three different annotations instead of one? What does the distinction communicate
to a developer reading the code?


```
@RestController, @Service, and @Repository all register Spring beans, but they show the role of the class. @RestController is for API endpoints, @Service is for business logic, and @Repository is for database access.

This makes the code easier to understand for other developers. It also helps Spring apply layer-specific behaviour, such as exception translation for repositories.
```

**8.** The `GET /api/transactions/monthly-spend` endpoint accepts `year` and `month` as
query parameters. What HTTP status code should this endpoint return if `month=13` is
passed? Who is responsible for validating it — the controller, the service, or Spring
itself — and how would you implement that validation?

```
If month=13 is passed, the endpoint should return 400 Bad Request because the request input is invalid. The validation can be handled in the controller or service, but the service is a good place to protect the business logic from invalid month values.

I would implement it by checking whether the month is between 1 and 12 before creating the LocalDate. If it is outside that range, I would throw a clear exception and map it to a 400 response.
```

---

## Data Access & SQL

**9.** `TransactionRepository` extends `JpaRepository<Transaction, Long>`. Spring Data JPA
can generate a query from a method named `findByAccountId`. Explain the mechanism behind
this — what is Spring doing at startup to turn that method name into SQL? When would you
write a `@Query` annotation instead of relying on derived query methods?

```
pring Data JPA reads method names like findByAccountId at startup and understands that it needs to create a query using the accountId field of the Transaction entity. So it automatically generates a query similar to finding all transactions where account_id matches the given value.

I would use @Query when the query is more complex, such as when it needs joins, custom filtering, grouping, aggregation, or database-specific logic that is not easy to express clearly through a method name.
```

**10.** `calculateMonthlySpend` had a bug in the date boundary comparison. Describe the
bug in plain language — what was the incorrect behaviour, what caused it at the code level,
and what kind of test input reliably exposes this class of off-by-one error? Why is this
type of bug particularly common in date/time logic?

```
The bug was that the monthly spend calculation did not correctly include transactions from the first day of the month. At the code level, the condition used isAfter(startOfMonth), which excludes transactions that happened exactly on the start date.

A good test for this bug is to include a transaction on 2024-12-01 and check whether it is included in the December spend total. Date bugs like this are common because it is easy to confuse inclusive and exclusive boundaries.
```


**11.** The application uses H2 in-memory for development. Describe exactly what you
would change to point this application at a PostgreSQL database in production. Be specific:
which files, which properties, and which Maven dependency. What is the risk of using
`spring.jpa.hibernate.ddl-auto=create-drop` in production?


```
To use PostgreSQL in production, I would update pom.xml to include the PostgreSQL driver dependency. I would also update application.properties  file with the PostgreSQL JDBC URL, username, password, driver, and Hibernate dialect if needed.

For example, the datasource URL would point to PostgreSQL instead of H2. Using spring.jpa.hibernate.ddl-auto=create-drop in production is dangerous because it can drop and recreate tables, which may cause permanent data loss.
```

---

## Testing

**12.** `TransactionServiceTest` uses `@Mock` on `TransactionRepository` and
`@InjectMocks` on `TransactionServiceImpl`. Explain what Mockito is doing here. What is
the repository being replaced with, and what does the test actually verify? What category
of bug can this test suite catch — and what category can it not?

```
@Mock creates a fake TransactionRepository, so the test does not use the real database. 
@InjectMocks creates TransactionServiceImpl and injects that fake repository into it.

These tests verify the service logic, such as calling the correct repository method, mapping request data, and calculating monthly spend.

They can catch service-level logic bugs, but they cannot catch controller mapping issues, HTTP status issues, or real database integration problems.
```


**13.** A teammate argues that because the service tests cover all the logic, there is no
need to write controller tests. Do you agree? Describe one specific type of bug that a
controller-level test (using `MockMvc`) would catch that the service tests in this project
would miss entirely.

```
I do not fully agree. Service tests are useful, but they do not verify how the API behaves from an HTTP request and response point of view.

A controller test using MockMvc can catch bugs like returning the wrong status code, using the wrong JSON response structure, or returning an entity instead of a DTO. 
These issues may not be visible in service tests because service tests do not call the actual API endpoint.
```

**14.** Looking at the tests you wrote in `TransactionCandidateTest.java`: what was the
first test you wrote, and why did you choose to start there? What does the order in which
you wrote tests tell you about how you approached the problem?


```
The first test I wrote in TransactionCandidateTest.java was for GET /api/transactions, because it is the simplest endpoint and helped me confirm that the controller test setup with MockMvc was working correctly. After that, I added tests for single transaction lookup, account filtering, monthly spend, delete, and create transaction.

This order shows that I started with the basic API flow first, then moved toward edge cases and behaviours that were more likely to fail, such as not-found responses and empty results.
```
---

## AI & Modern Engineering

**15.** Describe how you used AI tools during this project. For at least two specific
examples: what did you prompt the tool with, what did it return, and what did you change
or reject? Identify one place where the AI output was immediately trustworthy and one
place where it required meaningful scrutiny before you used it.

```
I used ChatGPT mainly to understand compilation errors, test failures, and to generate ideas for similar test cases. For example, I asked how to fix MockMvc test errors and how to write a controller test similar to another endpoint. I reviewed the suggestions, modified them to match my project, and verified them by running the tests.

I also used AI to help improve comments and refine the wording of my documentation and answers in DECISIONS.md. I edited the generated text to make sure it accurately reflected the work I had done.
```
