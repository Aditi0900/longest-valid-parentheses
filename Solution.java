import java.util.ArrayDeque;
import java.util.Deque;

public class Solution {

    // Push indices rather than the characters themselves. The stack then holds the
    // position of the last thing we couldn't match, so when a pair closes, the gap
    // back to the top of the stack is the length of the valid run ending here.
    // -1 is seeded as the boundary sitting just before the string.
    public int longestValidParentheses(String s) {
        if (s == null || s.length() < 2) {
            return 0;
        }

        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(-1);
        int maxLength = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                stack.push(i);
            } else {
                stack.pop();
                if (stack.isEmpty()) {
                    stack.push(i); // this ')' has no partner, so it's the new boundary
                } else {
                    maxLength = Math.max(maxLength, i - stack.peek());
                }
            }
        }

        return maxLength;
    }

    // Same O(n) time, but O(1) space. Count opens and closes as we go: equal counts
    // mean the current run is balanced, and more closes than opens means the run is
    // beyond saving, so reset. Going left to right misses runs with leftover '(' in
    // front ("((()" never balances), so a mirrored pass from the right catches those.
    public int longestValidParenthesesConstantSpace(String s) {
        if (s == null || s.length() < 2) {
            return 0;
        }

        int maxLength = 0;
        int open = 0;
        int close = 0;

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

    private static int failures = 0;

    public static void main(String[] args) {
        Solution solution = new Solution();

        check(solution, "(()", 2);
        check(solution, ")()())", 4);
        check(solution, "", 0);
        check(solution, "(", 0);
        check(solution, ")", 0);
        check(solution, "()", 2);
        check(solution, ")(", 0);
        check(solution, "((((", 0);
        check(solution, "))))", 0);
        check(solution, "()()", 4);
        check(solution, "(())", 4);
        check(solution, "(()())", 6);
        check(solution, "()(()", 2);
        check(solution, "()(())", 6);
        check(solution, "(()))())(", 4);
        check(solution, ")()())()()(", 4);
        check(solution, "()(()))))", 6);
        check(solution, null, 0);

        if (failures == 0) {
            System.out.println("\nAll tests passed.");
        } else {
            System.out.println("\n" + failures + " test(s) failed.");
            System.exit(1);
        }
    }

    // Runs both versions so they act as a check on each other.
    private static void check(Solution solution, String input, int expected) {
        int stackResult = solution.longestValidParentheses(input);
        int constantSpaceResult = solution.longestValidParenthesesConstantSpace(input);
        boolean passed = stackResult == expected && constantSpaceResult == expected;

        if (!passed) {
            failures++;
        }

        System.out.printf("%s  %-14s expected %-2d got %d, %d%n",
                passed ? "PASS" : "FAIL",
                input == null ? "null" : "\"" + input + "\"",
                expected, stackResult, constantSpaceResult);
    }
}
