---
layout: page
title: Code Review
permalink: /code-review/
---

This code review walks through the three artifacts in my ePortfolio as they were originally submitted. For each one I explain what the code does, analyze it against the CS 499 code review checklist, and describe the enhancements I plan and the course outcomes they support.

## Video

**[Watch the code review video](VIDEO_LINK_HERE)** (about 20 minutes)

The video covers each artifact in order: Software Design and Engineering, Algorithms and Data Structures, then Databases. A written summary of the review follows below.

---

## Category One: Software Design and Engineering

**Artifact:** Weight Tracker Android app, CS-360 ([original code](https://github.com/samherbst0/samherbst0.github.io/tree/main/reference/cs-360-weight-tracker))

**What it does.** Users create an account, log in, record daily weights in a scrolling list, set a goal weight, and get an SMS notification when they reach the goal. It is written in Java with an SQLite database and has five classes: `LoginActivity`, `MainActivity`, `DatabaseHelper`, `WeightAdapter`, and `WeightEntry`.

**Key findings**

- **Overly complex module:** `MainActivity` is 421 lines and handles the UI, input parsing, database calls, goal logic, and SMS permissions in one class, so none of it can be unit tested without a device.
- **Plain-text passwords:** passwords are stored and compared as plain text, so anyone with the database file can read every password.
- **Missing input validation:** weights of 0, negative numbers, unrealistic values such as 5,000 lb, and future dates are all accepted.
- **Data loss on upgrade:** `onUpgrade` drops every table, so any schema change would erase all users and weights.
- **Work on the UI thread:** database calls run on the main thread and can freeze the screen as data grows.
- **Hard-coded values and no tests:** the SMS alert goes to a hard-coded test number, and there are no automated tests.

**Planned enhancements**

- Refactor to MVVM, with repositories as the only classes that touch the database and ViewModels that hold the screen logic.
- Hash passwords with salted PBKDF2, compare them in constant time, and require a minimum password length.
- Add validators that reject bad weights, dates, and credentials with a clear message for each.
- Move the database to version 2 with a migration that keeps existing data, and run database work on a background executor.
- Add JUnit tests for the validators, the password hasher, and the ViewModels, and make the SMS number a user setting.

**Course outcomes:** 4 (industry-standard architecture and testing tools), 5 (removing the plain-text password vulnerability), 3 (more classes and indirection in exchange for testability), and 1 (a layered, documented codebase that other developers can review).

---

## Category Two: Algorithms and Data Structures

**Artifact:** ABCU Course Planner, CS-300 ([original code](https://github.com/samherbst0/samherbst0.github.io/tree/main/reference/cs-300-course-planner))

**What it does.** A C++ console program that loads a course catalog file, prints an alphanumeric course list, and looks up a single course with its prerequisites. Courses are stored as `Course` structs in a global `vector`.

**Key findings**

- **Linear lookups:** every lookup uses `find_if`, which is O(n).
- **Repeated sorting:** `printCourseList` re-sorts the whole vector on every call, which is O(n log n) per print.
- **Duplicate data:** loading the file twice adds every course again.
- **Unvalidated input:** prerequisites are never checked against real courses, and malformed lines are not reported, even though my original pseudocode required both checks.
- **Infinite loop:** typing a letter at the menu puts `cin` into a fail state, and the menu loops forever.
- **Functional gaps:** lookups are case-sensitive, Windows line endings stay in titles, circular prerequisites go undetected, and there is no way to get a valid course order. The catalog is global and there are no unit tests.

**Planned enhancements**

- Store the catalog in an AVL tree keyed on course number, so lookups become O(log n) and an in-order traversal prints the sorted list in O(n). A duplicate course number updates the existing node.
- Build a prerequisite graph and report missing prerequisites when the file loads.
- Use a topological sort (Kahn's algorithm, O(n + E)) to produce a valid course order, name any courses in a cycle, and group the order into semesters.
- Show the full prerequisite chain for any course using depth-first search.
- Harden input handling, then benchmark the vector and AVL versions on 1,000 to 100,000 courses and add Google Test cases.

**Course outcomes:** 3 (the main outcome: O(n log n) loading in exchange for much faster lookups and printing), 4 (Google Test, benchmarking, and standard graph algorithms), and 5 (validating untrusted file and keyboard input).

---

## Category Three: Databases

**Artifact:** Grazioso Salvare Animal Shelter Dashboard, CS-340 ([original code](https://github.com/samherbst0/samherbst0.github.io/tree/main/reference/cs-340-grazioso-salvare))

**What it does.** `CRUD_Python_Module.py` defines an `AnimalShelter` class that wraps create, read, update, and delete operations on a MongoDB collection using PyMongo. `ProjectTwoDashboard.ipynb` is a Dash web app that filters dogs suited to water, mountain, or disaster rescue and shows the results in a data table, a breed pie chart, and a location map.

**Key findings**

- **Hard-coded credentials:** the database username and password are written into both files, and the constructor ignores the credentials passed to it. (They are masked in the reference copy.)
- **Unsafe writes:** `update` and `delete` accept an empty filter, which matches every document, and query operators such as `$where` are passed straight through.
- **No indexes:** the rescue filters run a full collection scan. `explain()` shows the water-rescue query examining all 10,000 documents to return 17 results.
- **Inefficient data loading:** the dashboard loads the entire collection into pandas at startup and computes the breed chart in Python.
- **No least privilege and no tests:** one account does everything, and the data layer has no automated tests.

**Planned enhancements**

- Read connection settings from a git-ignored environment file, make the constructor use its arguments, and purge the old password from the repository history.
- Create a read-only role for the dashboard and a read-write role for administration.
- Add a compound index on breed, sex, and age, with before-and-after `explain()` output showing the change from `COLLSCAN` to `IXSCAN`.
- Use aggregation pipelines for breed counts and rescue summaries, and fetch only the columns and rows the table shows.
- Allow-list fields and operators, refuse empty-filter updates and deletes, add a JSON Schema validator, and write pytest tests with mongomock.

**Course outcomes:** 5 (the strongest fit: removing exposed credentials, blocking operator injection, and preventing mass deletes), 3 (indexes speed up reads but cost storage and write time), and 4 (indexing, aggregation, schema validation, and pytest).

---

## Summary

All three artifacts work, but each has weaknesses in structure, efficiency, security, and testing. The enhancements address each weakness and back every fix with evidence. This review supports Outcome 2, professional communication with a technical audience, and the repository itself, with documentation and the original code kept for comparison, supports Outcome 1.
