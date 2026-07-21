import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * Longest Valid Parentheses
 *
 * Given a string containing only '(' and ')', find the length of the longest
 * substring that is well-formed (every '(' has a matching ')' after it, and
 * the pairs are properly nested).
 *
 * Two independent solutions are provided:
 *
 *   1. {@link #longestValidParentheses(String)} - index stack, O(n) time / O(n) space.
 *   2. {@link #longestValidParenthesesConstantSpace(String)} - two counter sweeps,
 *      O(n) time / O(1) space.
 *
 * The second is asymptotically better in space; the first is easier to reason
 * about. The test harness in main() runs both against each other so a bug in
 * either one shows up immediately.
 */
public class Solution {

    /**
     * Stack-based solution.
     *
     * The insight: instead of pushing the characters, push their *indices*. The
     * stack then always holds the index of the last position that could not be
     * matched. Subtracting that index from the current one gives the length of
     * the valid run ending here, in one step.
     *
     * The stack is seeded with -1 to act as a sentinel "boundary just before the
     * string". Without it we would need a special case for a valid substring
     * that starts at index 0.
     *
     * Time:  O(n) - one pass, each index is pushed and popped at most once.
     * Space: O(n) - worst case "(((((", where nothing ever matches.
     *
     * @param s string made up of '(' and ')' characters
     * @return length of the longest valid parentheses substring
     */
    public int longestValidParentheses(String s) {
        if (s == null || s.length() < 2) {
            return 0; // Nothing shorter than "()" can ever be valid.
        }

        // ArrayDeque instead of java.util.Stack: Stack extends Vector, so every
        // operation is synchronized and pays for a lock we do not need.
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(-1); // Sentinel: the boundary before the string starts.

        int maxLength = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                stack.push(i);
            } else {
                stack.pop(); // Consume the most recent unmatched position.

                if (stack.isEmpty()) {
                    // We just popped the sentinel, which means this ')' has no
                    // partner. It becomes the new boundary: no valid substring
                    // can ever span across it.
                    stack.push(i);
                } else {
                    // Everything between the new top and i is matched.
                    maxLength = Math.max(maxLength, i - stack.peek());
                }
            }
        }

        return maxLength;
    }

    /**
     * Two-pass counter solution - same O(n) time, but O(1) space.
     *
     * Sweeping left to right, track how many '(' and ')' we have seen:
     *   - open == close  -> the run we are standing on is balanced, record 2*close.
     *   - close > open   -> this run can never be repaired, reset both counters.
     *
     * That single sweep misses cases like "(()", where the string ends while
     * open is still ahead and we never hit equality again. Sweeping right to
     * left with the mirrored rule (reset when open > close) catches exactly
     * those. The answer is the best of the two sweeps.
     *
     * Time:  O(n) - two passes.
     * Space: O(1) - four ints, no matter how long the input is.
     *
     * @param s string made up of '(' and ')' characters
     * @return length of the longest valid parentheses substring
     */
    public int longestValidParenthesesConstantSpace(String s) {
        if (s == null || s.length() < 2) {
            return 0;
        }

        int maxLength = 0;
        int open = 0;
        int close = 0;

        // Left to right: catches every valid substring except those with
        // leftover '(' hanging off the front.
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                open++;
            } else {
                close++;
            }

            if (open == close) {
                maxLength = Math.max(maxLength, 2 * close);
            } else if (close > open) {
                open = 0;
                close = 0;
            }
        }

        // Right to left: mirrored, so it catches the ones the first pass missed.
        open = 0;
        close = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '(') {
                open++;
            } else {
                close++;
            }

            if (open == close) {
                maxLength = Math.max(maxLength, 2 * open);
            } else if (open > close) {
                open = 0;
                close = 0;
            }
        }

        return maxLength;
    }

    // ------------------------------------------------------------------
    // Test harness
    // ------------------------------------------------------------------

    private static int failures = 0;

    public static void main(String[] args) {
        Solution solution = new Solution();

        // Cases from the problem statement.
        check(solution, "(()", 2);
        check(solution, ")()())", 4);
        check(solution, "", 0);

        // Edge cases.
        check(solution, "(", 0);                 // single unmatched open
        check(solution, ")", 0);                 // single unmatched close
        check(solution, "()", 2);                // smallest valid input
        check(solution, ")(", 0);                // right characters, wrong order
        check(solution, "((((", 0);              // never closes
        check(solution, "))))", 0);              // never opens

        // Structure: nesting, repetition, and valid runs split by junk.
        check(solution, "()()", 4);              // adjacent pairs merge
        check(solution, "(())", 4);              // nesting
        check(solution, "(()())", 6);            // nesting plus adjacency
        check(solution, "()(()", 2);             // trailing garbage
        check(solution, "()(())", 6);            // whole string valid
        check(solution, "(()))())(", 4);         // best run sits at the front
        check(solution, ")()())()()(", 4);       // two equal runs, tie
        check(solution, "()(()))))", 6);         // valid prefix, junk suffix

        // Null is handled rather than throwing.
        check(solution, null, 0);

        // Long input: 5000 nested pairs, to confirm there is no O(n^2) blowup
        // and that the stack solution copes with deep nesting.
        int pairs = 5000;
        String deep = "(".repeat(pairs) + ")".repeat(pairs);
        check(solution, deep, 2 * pairs);

        randomCrossCheck(solution);

        System.out.println();
        if (failures == 0) {
            System.out.println("All tests passed.");
        } else {
            System.out.println(failures + " test(s) FAILED.");
            System.exit(1);
        }
    }

    /** Runs both implementations and reports whether they agree with expected. */
    private static void check(Solution solution, String input, int expected) {
        int stackResult = solution.longestValidParentheses(input);
        int constantSpaceResult = solution.longestValidParenthesesConstantSpace(input);

        boolean passed = stackResult == expected && constantSpaceResult == expected;
        if (!passed) {
            failures++;
        }

        // Keep long inputs from flooding the console.
        String shown = input == null ? "null"
                : input.length() > 20 ? "\"" + input.substring(0, 17) + "...\" (len " + input.length() + ")"
                : "\"" + input + "\"";

        System.out.printf("%-4s %-34s expected=%-5d stack=%-5d constantSpace=%d%n",
                passed ? "PASS" : "FAIL", shown, expected, stackResult, constantSpaceResult);
    }

    /**
     * Fuzz test: the two implementations were derived differently, so it would
     * take an unlikely coincidence for both to be wrong the same way on a random
     * input. Any disagreement is a real bug in one of them.
     */
    private static void randomCrossCheck(Solution solution) {
        Random random = new Random(42); // Fixed seed so failures are reproducible.
        int trials = 20000;

        for (int trial = 0; trial < trials; trial++) {
            int length = random.nextInt(30);
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(random.nextBoolean() ? '(' : ')');
            }
            String input = sb.toString();

            int stackResult = solution.longestValidParentheses(input);
            int constantSpaceResult = solution.longestValidParenthesesConstantSpace(input);
            int bruteForceResult = bruteForce(input);

            if (stackResult != bruteForceResult || constantSpaceResult != bruteForceResult) {
                failures++;
                System.out.printf("FAIL random \"%s\" bruteForce=%d stack=%d constantSpace=%d%n",
                        input, bruteForceResult, stackResult, constantSpaceResult);
                return; // First mismatch is enough to debug from.
            }
        }

        System.out.printf("PASS %-34s %d random strings vs brute force%n", "randomised cross-check", trials);
    }

    /**
     * Obviously-correct O(n^2) reference: check every substring directly. Far too
     * slow to submit, but perfect as an oracle for the fuzz test above.
     */
    private static int bruteForce(String s) {
        int best = 0;
        for (int start = 0; start < s.length(); start++) {
            int balance = 0;
            for (int end = start; end < s.length(); end++) {
                balance += s.charAt(end) == '(' ? 1 : -1;
                if (balance < 0) {
                    break; // More ')' than '(' - this and every longer substring is invalid.
                }
                if (balance == 0) {
                    best = Math.max(best, end - start + 1);
                }
            }
        }
        return best;
    }
}
