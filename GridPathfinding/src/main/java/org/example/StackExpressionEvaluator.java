package org.example;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Evaluates a string expression of form "expression = math_expr" using an
 * iterative two-stack (values + operators) evaluator instead of building an
 * expression tree via recursive descent (see TowerReachable#calculate).
 * math_expr contains digits and symbols +, -, /, * or braces ( and ),
 * always a valid expression, evaluated following BODMAS rules.
 */
public class StackExpressionEvaluator {

    int calculate(String expression) {
        String lhs = expression.split("=")[1].trim();
        return evaluate(lhs);
    }

    private int evaluate(String s) {
        Deque<Integer> values = new ArrayDeque<>();
        Deque<Character> ops = new ArrayDeque<>();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (Character.isDigit(c)) {
                int start = i;
                while (i < n && Character.isDigit(s.charAt(i))) {
                    i++;
                }
                values.push(Integer.parseInt(s.substring(start, i)));
                i--;
            } else if (c == '(') {
                ops.push(c);
            } else if (c == ')') {
                while (ops.peek() != '(') {
                    applyTop(values, ops);
                }
                ops.pop();
            } else {
                if ((c == '-' || c == '+') && isUnaryPosition(s, i)) {
                    values.push(0);
                }
                while (!ops.isEmpty() && precedence(ops.peek()) >= precedence(c)) {
                    applyTop(values, ops);
                }
                ops.push(c);
            }
        }
        while (!ops.isEmpty()) {
            applyTop(values, ops);
        }
        return values.pop();
    }

    private boolean isUnaryPosition(String s, int i) {
        int j = i - 1;
        while (j >= 0 && Character.isWhitespace(s.charAt(j))) {
            j--;
        }
        return j < 0 || s.charAt(j) == '(' || isOperator(s.charAt(j));
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private int precedence(char operator) {
        return switch (operator) {
            case '*', '/' -> 2;
            case '+', '-' -> 1;
            default -> 0; // '('
        };
    }

    private void applyTop(Deque<Integer> values, Deque<Character> ops) {
        char operator = ops.pop();
        int b = values.pop();
        int a = values.pop();
        values.push(apply(operator, a, b));
    }

    private int apply(char operator, int a, int b) {
        return switch (operator) {
            case '+' -> a + b;
            case '-' -> a - b;
            case '*' -> a * b;
            case '/' -> a / b;
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }
}
