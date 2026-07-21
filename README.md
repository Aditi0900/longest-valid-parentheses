# Longest Valid Parentheses

Given a string containing only the characters `(` and `)`, return the length of the
longest **valid** (well-formed) parentheses substring.

A substring is valid when every `(` has a matching `)` after it and the pairs are
properly nested — `"(())"` is valid, `")("` is not.

---

## Problem Statement

**Input:** a string `s` consisting only of `(` and `)`
**Output:** an integer — the length of the longest valid parentheses substring

**Constraints**

- `0 <= s.length <= 3 * 10^4`
- `s[i]` is `'('` or `')'`

### Examples

| # | Input | Output | Why |
|---|-------|--------|-----|
| 1 | `"(()"` | `2` | The longest valid substring is `"()"` |
| 2 | `")()())"` | `4` | The longest valid substring is `"()()"` |
| 3 | `""` | `0` | No valid substring exists |

Note that the answer is always even, and the substring must be **contiguous** —
`"()(()"` is `2`, not `4`, because the two valid pairs are not adjacent.

---

## Repository Structure

```
.
├── README.md
└── Solution.java     # both solutions + the test cases
```

## Running It

Requires a JDK (8 or newer).

```bash
javac Solution.java
java Solution
```

`main` runs the full test suite and prints a `PASS`/`FAIL` line per case, exiting
with a non-zero status if anything fails.

---

## Approach 1 — Index Stack · O(n) time, O(n) space

The natural instinct is to push parentheses onto a stack. The better idea is to push
their **indices**.

Doing that, the stack always holds the position of the most recent character that
could *not* be matched. So the moment a `)` closes a pair, the distance from the
current index back to whatever is left on top of the stack *is* the length of the
valid run ending here — no counting required.

The stack is seeded with `-1`, a sentinel standing for "the boundary just before the
string". It removes the special case for a valid substring that starts at index `0`.

**The rules**

1. Push `-1` as the initial boundary.
2. On `(` — push its index.
3. On `)` — pop, then:
   - stack now empty → this `)` has no partner, so push its index as the new boundary;
   - otherwise → `maxLength = max(maxLength, i - stack.top())`.

**Worked trace on `")()())"`**

| i | char | action | stack after | max |
|---|------|--------|-------------|-----|
| — | — | seed sentinel | `[-1]` | 0 |
| 0 | `)` | pop `-1`, empty → push `0` | `[0]` | 0 |
| 1 | `(` | push `1` | `[0, 1]` | 0 |
| 2 | `)` | pop `1`, top is `0` → `2 - 0` | `[0]` | **2** |
| 3 | `(` | push `3` | `[0, 3]` | 2 |
| 4 | `)` | pop `3`, top is `0` → `4 - 0` | `[0]` | **4** |
| 5 | `)` | pop `0`, empty → push `5` | `[5]` | 4 |

Answer: **4**.

Index `0` survives on the stack as a wall the whole time — nothing after it can pair
with anything before it, which is exactly what a boundary should do.

> **Implementation note:** `ArrayDeque` is used rather than `java.util.Stack`.
> `Stack` extends the legacy `Vector`, so every push and pop pays for a
> synchronization lock this single-threaded algorithm never needs.

## Approach 2 — Two Counter Sweeps · O(n) time, O(1) space

The stack exists only to remember boundaries. Two counters can do the same job
without the memory.

Sweeping left to right, count `open` and `close`:

- `open == close` → the current run is balanced, record `2 * close`;
- `close > open` → too many `)`, this run is unrecoverable, reset both to zero.

That alone is not enough. On `"((()"` the sweep ends with `open` still ahead and
never hits equality again, so the valid `"()"` inside is missed. A second sweep from
the right with the mirrored rule (reset when `open > close`) catches exactly those
cases. The answer is the larger of the two sweeps.

This trades one pass of extra work for dropping O(n) memory down to four `int`s —
worth it on the upper end of the input constraint.

## Complexity

| Approach | Time | Space | Notes |
|----------|------|-------|-------|
| Brute force (every substring) | O(n²) | O(1) | Correct but far too slow at n = 30,000 |
| Dynamic programming | O(n) | O(n) | Works, but the recurrence is fiddlier than the stack |
| **Index stack** | **O(n)** | **O(n)** | Clearest to reason about; each index pushed and popped once |
| **Two counter sweeps** | **O(n)** | **O(1)** | Optimal on both axes |

Both implementations ship in `Solution.java`; neither is dead code, since the tests
run them against each other.

---

## Testing

`main` runs the three examples from the problem statement plus the edge cases that
usually break this problem: empty and single-character inputs, `")("`, all-open and
all-close strings, nesting, adjacent pairs that merge into one longer run, valid runs
buried in junk, and `null`.

Every case runs through both implementations, so the two act as a check on each
other — if they ever disagree, at least one is wrong.

Sample output:

```
PASS  "(()"          expected 2  got 2, 2
PASS  ")()())"       expected 4  got 4, 4
PASS  ""             expected 0  got 0, 0
PASS  ")("           expected 0  got 0, 0
PASS  "(()))())("    expected 4  got 4, 4
...

All tests passed.
```

## Language

Java (no external dependencies — compiles and runs with a plain JDK).
