package org.example;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StackExpressionEvaluator.calculate")
class StackExpressionEvaluatorTest {

    private final StackExpressionEvaluator solver = new StackExpressionEvaluator();

    @Test
    @DisplayName("Single number expression evaluates to itself")
    void singleNumberExpression() {
        assertThat(solver.calculate("x = 5")).isEqualTo(5);
    }

    @Test
    @DisplayName("Simple addition")
    void simpleAddition() {
        assertThat(solver.calculate("x = 2 + 3")).isEqualTo(5);
    }

    @Test
    @DisplayName("Simple subtraction")
    void simpleSubtraction() {
        assertThat(solver.calculate("x = 10 - 4")).isEqualTo(6);
    }

    @Test
    @DisplayName("Simple multiplication")
    void simpleMultiplication() {
        assertThat(solver.calculate("x = 6 * 7")).isEqualTo(42);
    }

    @Test
    @DisplayName("Simple division")
    void simpleDivision() {
        assertThat(solver.calculate("x = 8 / 2")).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiplication takes precedence over addition")
    void multiplicationTakesPrecedenceOverAddition() {
        assertThat(solver.calculate("x = 2 + 3 * 4")).isEqualTo(14);
    }

    @Test
    @DisplayName("Parentheses override default precedence")
    void parenthesesOverridePrecedence() {
        assertThat(solver.calculate("x = (2 + 3) * 4")).isEqualTo(20);
    }

    @Test
    @DisplayName("Nested parentheses are evaluated correctly")
    void nestedParenthesesEvaluatedCorrectly() {
        assertThat(solver.calculate("x = ((2 + 3) * (4 - 1)) / 3")).isEqualTo(5);
    }

    @Test
    @DisplayName("Leading unary minus negates the following term")
    void leadingUnaryMinus() {
        assertThat(solver.calculate("x = -3 + 4")).isEqualTo(1);
    }

    @Test
    @DisplayName("Unary minus inside parentheses negates the sub-expression")
    void unaryMinusInsideParentheses() {
        assertThat(solver.calculate("x = 2 * (-5)")).isEqualTo(-10);
    }

    @Test
    @DisplayName("Integer division truncates toward zero for positive operands")
    void integerDivisionTruncatesPositive() {
        assertThat(solver.calculate("x = 7 / 2")).isEqualTo(3);
    }

    @Test
    @DisplayName("Integer division truncates toward zero for negative operands")
    void integerDivisionTruncatesNegative() {
        assertThat(solver.calculate("x = -7 / 2")).isEqualTo(-3);
    }

    @Test
    @DisplayName("Extra whitespace around tokens is ignored")
    void extraWhitespaceIsIgnored() {
        assertThat(solver.calculate("x =   2   +   3  *   4  ")).isEqualTo(14);
    }
}
