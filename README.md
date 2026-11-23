# Advanced Heap Implementation

Java implementation of advanced heap data structures, developed as part of the Data Structures course at Tel Aviv University.
The implementation uses a single configurable heap structure that supports different behaviors through two parameters: "lazyMelds" and "lazyDecreaseKeys". 
Depending on their values, the structure can behave as a Fibonacci heap, a lazy binomial heap, a binomial-style heap, or a binomial heap with cuts.

# Features

- Insert elements with key-value pairs
- Find and delete the minimum element
- Decrease keys
- Delete arbitrary elements
- Meld two heaps
- Cascading cuts
- Heapify-up operations
- Successive linking and tree consolidation
- Tracking of structural operations such as links, cuts, marked nodes, and heapify costs

# Implementation

The heap is represented as a collection of heap-ordered trees using circular doubly linked lists.
Two configuration flags control its behavior:
"lazyMelds" – determines whether consolidation is postponed or performed during meld operations.
"lazyDecreaseKeys" – determines whether decrease-key uses cascading cuts or heapify-up.
This allows the same implementation to demonstrate and compare several advanced heap strategies.

# Technologies

- Java
- Data Structures
- Amortized Analysis
