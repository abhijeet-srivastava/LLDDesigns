---
name: gridpathfinding-conventions
description: Build/test conventions specific to the GridPathfinding module. Use when running mvn commands, editing classes, or writing tests under GridPathfinding/src.
---

# GridPathfinding Module Conventions

This module (`org.example`, Java 17) holds standalone LLD/data-structure and
algorithm exercises (grid pathfinding, expression evaluators, schedulers,
custom collections). Prefer these module-local patterns over generic
repo-wide defaults when working here.

## Build & test

- Scope Maven commands to this module: `mvn -pl GridPathfinding -am compile`,
  `mvn -pl GridPathfinding -am test`.
- Tests use JUnit 5 + AssertJ (AssertJ pinned to `3.26.3` in this module's
  `pom.xml`, overriding the root-inherited version).

## Class conventions

- No JDK collection interfaces (`java.util.Map`, etc.) are implemented by
  custom data structures — plain classes with hand-rolled bucket/node
  storage instead (see `CustomHashMap`).
- No generics in these exercises — hardcode to `String`/concrete types
  rather than introducing `<K, V>` type parameters.
- Top-level methods are package-private (no `public`) except constructors,
  which are always `public`.
- Inner helper types (`Node`, etc.) are non-static `private class`, even
  though they don't capture outer state — matches `BookShelfManager.Node`
  and `CustomHashMap.Node`.
- "Not found" / missing-key lookups return `""` (empty string), never
  `null`.
- No synchronization/concurrency utilities — these exercises are
  single-threaded by convention.

## Test conventions

- File name: `<ClassName>Test.java`, same `org.example` package.
- `@DisplayName` on the test class and most `@Test` methods.
- `@Nested` inner classes group tests by the method under test (e.g. `Put`,
  `Get`, `Remove`), each with its own `@BeforeEach` when it needs extra
  seed data beyond the outer `@BeforeEach`.
- Cover both positive and negative cases per method (e.g. found vs.
  not-found lookups, valid vs. out-of-range indices).
- Prefer plain `assertThat(...)` chains; add `.as("...")` descriptions only
  where the assertion's intent isn't obvious from the code around it.