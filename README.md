# Longest Valid Parentheses

## Problem Statement

Given a string containing only the characters `(` and `)`, return the length of the longest valid (well-formed) parentheses substring.

### Examples

**Example 1**

```
Input: s = "(()"
Output: 2
```

**Example 2**

```
Input: s = ")()())"
Output: 4
```

**Example 3**

```
Input: s = ""
Output: 0
```

## Solution

This repository contains an optimal Java solution using a stack to efficiently determine the length of the longest valid parentheses substring.

### Algorithm

1. Initialize a stack with `-1`.
2. Traverse the string from left to right.
3. Push the index of every `'('` onto the stack.
4. For every `')'`, pop the stack.
5. If the stack becomes empty, push the current index as the new base.
6. Otherwise, update the maximum valid substring length.

## Complexity

* **Time Complexity:** O(n)
* **Space Complexity:** O(n)

## Language

* Java

## File Structure

```
.
├── README.md
└── Solution.java
```
