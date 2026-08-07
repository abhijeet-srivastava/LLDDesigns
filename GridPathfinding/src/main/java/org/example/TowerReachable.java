package org.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TowerReachable {

    private static final double EPSILON = 0.01;

    public class Tower {
        double x;
        double y;
        String id;

        public Tower(String towerStr) {
            String[] arr = towerStr.split("\\s");
            this.x = Integer.parseInt(arr[1].trim());
            this.y = Integer.parseInt(arr[2].trim());
            this.id = arr[0].trim();
        }

        public double distance(Tower other) {
            double deltaX = this.x - other.x, deltaY = this.y - other.y;
            return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
        }
    }


    
    public double minRange(String src, String dest, List<String> towerList) {
        int n = towerList.size();
        Map<String, Tower> tm = new HashMap<>();
        for(int i = 0; i < n; i++) {
            Tower tower = new Tower(towerList.get(i));
            tm.put(tower.id, tower);
        }
        if(!tm.containsKey(src) || !tm.containsKey(dest)) {
            return -1.0d;
        }
        if(src.equals(dest)) {
            return 0.0d;
        }
        double l = 0.0d, r = tm.get(src).distance(tm.get(dest));
        while((r-l) > EPSILON) {
            double mid = l + (r-l)/2.0d;
            if(canReach(mid, src, dest, towerList, tm)) {
                r = mid;
            } else {
                l = mid;
            }
        }
        return l;
    }

    private boolean canReach(double range,String src, String dest, List<String> towerList, Map<String, Tower> tm) {
        if(src.equals(dest)) {
            return true;
        }
        Set<String> visited = new HashSet<>();
        return dfs(range, src, dest, visited, towerList, tm);
    }

    private boolean dfs(double range, String src, String dest, Set<String> visited, List<String> towerList, Map<String, Tower> tm) {
        if(src.equals(dest)) {
            return true;
        }else if(visited.contains(src)) {
            return false;
        }
        visited.add(src);
        Tower srcTower = tm.get(src);
        for(String next: towerList) {
            if(srcTower.distance(tm.get(next)) <= range && !visited.contains(next)) {
                if(dfs(range, next, dest, visited, towerList, tm)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Caluclates value of String expression of form (expression = "math_expr")
     * Math expr contains digits and symbold +,-, /, * or braces ( and )
     * Its always a valid expression, and must be evaluated by fallowing BODOMAS rules
     * @param expression
     * @return value of string math_expr
     */
    int calculate(String expression) {
        String lhs = expression.split("=")[1];
        return evaluate(lhs);
    }
    private int evaluate(String expression) {
        Node exprTreeRoot = parseExpression(expression,  new int[]{0});
        return exprTreeRoot.evaluate();
    }


    private Node parseExpression(String s, int[] pos) {
        Node node = parseTerm(s, pos);
        skipWhitespace(s, pos);
        while (pos[0] < s.length() && (s.charAt(pos[0]) == '+' || s.charAt(pos[0]) == '-')) {
            String operator = String.valueOf(s.charAt(pos[0]));
            pos[0]++;
            Node right = parseTerm(s, pos);
            node = new Node(operator, node, right);
            skipWhitespace(s, pos);
        }
        return node;
    }

    private Node parseTerm(String s, int[] pos) {
        Node node = parseFactor(s, pos);
        skipWhitespace(s, pos);
        while (pos[0] < s.length() && (s.charAt(pos[0]) == '*' || s.charAt(pos[0]) == '/')) {
            String operator = String.valueOf(s.charAt(pos[0]));
            pos[0]++;
            Node right = parseFactor(s, pos);
            node = new Node(operator, node, right);
            skipWhitespace(s, pos);
        }
        return node;
    }

    private Node parseFactor(String s, int[] pos) {
        skipWhitespace(s, pos);
        boolean negative = false;
        if (s.charAt(pos[0]) == '-' || s.charAt(pos[0]) == '+') {
            negative = s.charAt(pos[0]) == '-';
            pos[0]++;
            skipWhitespace(s, pos);
        }
        Node node;
        if (s.charAt(pos[0]) == '(') {
            pos[0]++;
            node = parseExpression(s, pos);
            skipWhitespace(s, pos);
            pos[0]++; // consume ')'
        } else {
            int start = pos[0];
            while (pos[0] < s.length() && Character.isDigit(s.charAt(pos[0]))) {
                pos[0]++;
            }
            node = new Node(Integer.parseInt(s.substring(start, pos[0])));
        }
        return negative ? new Node("-", new Node(0), node) : node;
    }

    private void skipWhitespace(String s, int[] pos) {
        while (pos[0] < s.length() && Character.isWhitespace(s.charAt(pos[0]))) {
            pos[0]++;
        }
    }

    /**
     * Class representing an expression tree node: either a numeric leaf
     * (operator == null) or an internal node combining left and right via operator.
     */
    private class Node {
        String operator;// +, -, /, *
        int value;
        Node left;
        Node right;

        public Node(int value) {
            this.value = value;
        }

        public Node(String operator, Node left, Node right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        public int evaluate(){
            if (operator == null) {
                return value;
            }
            int leftVal = left.evaluate();
            int rightVal = right.evaluate();
            return apply(this.operator, leftVal, rightVal);
        }

        private int apply(String operator, int leftVal, int rightVal) {
            return switch (operator) {
                case "+" -> leftVal + rightVal;
                case "-" -> leftVal - rightVal;
                case "*" -> leftVal * rightVal;
                case "/" -> leftVal / rightVal;
                default -> throw new IllegalArgumentException("Unknown operator: " + operator);
            };
        }
    }
}
