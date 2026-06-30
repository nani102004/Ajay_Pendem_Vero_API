# Decisions

**Name:** Pendem Ajay

**Date started:** 01-Jul-2026

**Date submitted:**01-Jul-2026

In two or three sentences, describe your overall approach before getting into specifics.
What did you read first? What did you prioritise, and why?
```
I started by reading the README to understand the project's purpose, requirements, and expected behaviour. After that, I reviewed the project structure to familiarize myself with the controller, service, repository, and model layers.

I prioritized identifying and fixing functional bugs first to ensure the application behaved correctly, followed by removing incomplete or unnecessary code and improving the overall implementation where needed.
```
---

## 1. Code & Design Decisions

**The codebase includes an `Auditable` abstract class that is not currently used by any
entity. What did you do with it, if anything? Walk through your reasoning — what is the
purpose of the Auditable pattern, what are the tradeoffs of using it versus not, and why
did you make the choice you did?**

```
I kept the `Auditable` class as it was because it provides a common place to manage `createdAt` and `updatedAt` fields for entities that need audit information. I did not use it in the existing entities since it would require changing the database schema, which was outside the scope of this assessment, but it can be easily used in the future if audit tracking is needed.

```


**`TransactionResponse` is used as the outbound DTO for the API. What changes did you
make to it, if any? Why does the shape of a response DTO matter — and what is the risk
of returning an entity directly from a controller?**

```
I updated `TransactionResponse` to include the fields needed in API responses, such as `accountId` and `category`, and used it in the `createTransaction` endpoint instead of returning the `Transaction` entity directly. This keeps the API response controlled and prevents exposing internal entity details or future database-related fields unintentionally.
```

**The `BudgetCalculator` requires grouping and sorting data. What data structure or
approach did you choose to implement it? Walk through the alternatives you considered
and why you landed where you did.**

```
I used a `Map` to group transactions by category and calculate the total amount spent for each category. After that, I sorted the map entries by total spend in descending order and collected the result into a `LinkedHashMap` because it preserves the sorted order in the final response.
```


**Were there any decisions you made that are not covered by the questions above? Describe
the most significant one and your reasoning.**

```
One additional decision I made was to add simple validation checks for null inputs in service methods such as creating and deleting transactions.

This makes the code safer and gives a clear error instead of allowing unexpected null values to fail later in the application flow.
```

---

## 2. Bug Fixes & Issues Found

**Describe each problem you found in the codebase. For each one: where was it, how did
you identify it, what did it cause, and how did you fix it?**

```
1. I found that the createTransaction endpoint was returning the Transaction entity directly instead of returning a TransactionResponse DTO. I fixed this in TransactionController by converting the saved transaction into TransactionResponse before sending the response.

2. The monthly spend calculation had a date boundary issue. It did not correctly include transactions from the first day of the month, so I fixed the filter condition to include transactions from the start date through the end date of the month.

3. The getTransactionsByDateRange method was incomplete and returned an empty list. I fixed it by adding repository support for filtering transactions between two dates and calling that method from the service.

4. The BudgetCalculator method was not implemented. I completed it by grouping transactions by category, summing the amounts, sorting the totals in descending order, and returning only the top requested categories.

5. There was also an unsupported repository method usage for finding category transactions by month. Since it was not required for the current API behaviour, I removed or avoided that incomplete method to keep the code compiling cleanly.
```

**Were there any problems you noticed but chose not to fix? If so, explain why.**

```
I noticed that the Auditable class was not used by any entity, but I chose not to connect it to the existing models because that would change the database schema by adding audit columns.
```

---

## 3. Testing Decisions

**What tests did you write in `TransactionCandidateTest.java`? For each test, explain
what behaviour it validates and why you chose to cover that behaviour.**

```
I wrote controller-level tests in TransactionCandidateTest.java using MockMvc. I tested the main transaction endpoints to verify that they return the correct HTTP status codes and response bodies.

1. I tested GET /api/transactions to confirm that all transactions are returned as TransactionResponse objects. I also tested GET /api/transactions/{id} for both success and not-found cases, so the API returns transaction details when the ID exists and 404 when it does not.

2. I tested GET /api/transactions/account/{accountId} to confirm that transactions are returned for a valid account and that an empty list is returned when no transactions exist for an account. I also tested GET /api/transactions/monthly-spend to verify that spend totals are returned by category and that an empty map is returned when there are no transactions for the selected month.

3. I tested DELETE /api/transactions/{id} to ensure it returns 204 No Content after deletion. I also tested POST /api/transactions to confirm that a valid request creates a transaction and returns a TransactionResponse DTO instead of exposing the entity directly.

```


**What did you deliberately not test, and why? If you had more time, what would be the
next most important test to add?**

```
I deliberately did not test every possible validation error for POST /api/transactions, such as missing category or an overly long description, because I focused first on the main endpoint behaviour and response structure.

If I had more time, the next most important test I would add is a full integration test for monthly spend using real seeded transactions, because that would verify the controller, service, repository, and database flow together.
```


**What is the difference between what `TransactionServiceTest` covers and what your
`TransactionCandidateTest` covers? Are they testing the same things?**

```
TransactionServiceTest mainly tests the service layer logic directly by mocking the repository. It checks whether the service maps data correctly, calls repository methods, calculates monthly spend, and handles business logic.

TransactionCandidateTest tests the controller/API layer using MockMvc. It checks whether the endpoints return the correct HTTP status codes and JSON response structure.

They are related, but they are not testing the exact same things. The service tests focus on internal logic, while the controller tests focus on API behaviour and request-response handling.
```

---

## 4. AI Tool Usage

AI tool usage is expected and encouraged. Using AI is not cheating — it is a core skill
of modern engineering. What we are evaluating is whether you used it thoughtfully:
whether you understood and verified what it produced, and whether you can recognise
when its output should not be trusted.

**Which AI tools did you use? (e.g. ChatGPT, Claude, GitHub Copilot, Cursor, other)**

```
I primarily used ChatGPT during this project.
```


**Give two or three specific examples of how you used AI on this project. For each:
what did you prompt it with, what did it return, and what did you accept, change, or
reject?**

```
1. I used AI to understand some compilation errors and test failures while writing the controller and service test cases. It helped explain the cause of the errors, but I verified the fixes by running the tests and making the necessary changes myself.

2. I also used AI to generate a few similar test case structures. For example, after writing one endpoint test, I used AI to suggest a similar structure for another endpoint such as getTransactionById and getTransactionsByAccount, and then modified the tests to match my implementation.

3. Additionally, I used AI to help write and refine method comments and improve the wording of the explanations in the documentation (DECISIONS.md and question responses). I reviewed and edited the generated text before using it.

```


**Describe a moment where AI gave you something wrong, incomplete, or subtly misleading.
How did you catch it, and what did you do?**

```
In a few cases, AI suggested test cases that did not match my API responses correctly. I found these issues by running the tests, checking my controller code, and updating the assertions to match the actual API output.
```


**What is your general philosophy on using AI when writing backend code? Where does it
help, and where do you not trust it?**

```
I use AI to help me understand errors, get ideas, and save time on repetitive tasks like writing similar test cases and improving documentation.

However, I always review the suggestions, understand the code myself, and verify everything by compiling the project and running the tests before using it.

```

---

## 5. What You'd Do Next

**If you had two more days on this project, what would you build or fix first?
List in priority order, with one sentence of justification for each.**


```
1. Improve validation and global exception handling and create custom exceptions with proper user friendly messages to provide more consistent and user-friendly error responses for invalid requests.

2. Increase test coverage by adding more edge case tests, such as invalid inputs, empty data, and boundary conditions, to improve the overall reliability of the application.

```

**What is the biggest remaining risk or weakness in the code you have submitted?**

```
The biggest remaining risk is that most of my controller tests use mocked service responses, so they do not fully verify the real database flow.
```