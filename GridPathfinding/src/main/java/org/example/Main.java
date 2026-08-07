package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    int[][] dirs = {{1,0}, {0,1}, {-1, 0}, {0,-1}};
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        Main main = new Main();
        //main.solveMinElevationGain();
        main.solveMaxSafePath();
    }

    private void solveMaxSafePath() {
        int m = 10, n = 10;
        int[] start = {0,0}, target = {9,9};
        int[][] cats = {{9,0}, {2,4}, {5, 3}};
        int maxSafeness = this.maximumSafenessFactor(m,n, start, target, cats);
        System.out.printf("Max Safeness = %d\n", maxSafeness);
    }

    private void solveMinElevationGain() {
        int[][] matrix = createElevationGainMatrix(3, 3);
        int minElevation = minimumElevationGain(matrix);
        System.out.printf("Min Elevation: %d\n", minElevation);
    }

    /**
     * You are given a list of routers and their 2D location coordinates, a source router, a destination router, and a range value.
     * Each router, when it receives a message without collision, broadcasts the message to other routers which are within range.
     * Whenever a router broadcasts a message, it shuts down permanently and cannot broadcast again.
     * Write an implementation for a method which determines whether a message starting from a source router is reachable to a destination router without collision.
     * The distance between two routers is calculated using Euclidean distance, and time to travel between 2 router is propotional to distance between them
     * Each router is represented as a string in the format:"routerId,x,y", i.e "A,0,0" means router A is located at coordinate (0, 0)
     * A router can send a message to another router only if the Euclidean distance between them is less than or equal to range.
     * For comparing arrival times, two times are considered equal if their absolute difference is less than or equal to 0.1.
     * If a router receives two or more messages at the same earliest arrival time, those messages collide and are discarded, and not brodcasted by router
     * Messages that arrive at a router after its earliest arrival time are ignored.
     *
     * @param routers
     * @param sourceRouter
     * @param destinationRouter
     * @param range
     * @return
     */

    private static final double ARRIVAL_TIME_TOLERANCE = 0.1;

    boolean canReachDestinationBFS(List<String> routers, String sourceRouter, String destinationRouter, double range) {
        Map<String, Router> routerMap = new HashMap<>();

        for(String r: routers) {
            Router router = new Router(r);
            routerMap.put(router.name, router);
        }
        if(!routerMap.containsKey(sourceRouter) || !routerMap.containsKey(destinationRouter)) {
            return false;
        }
        if(sourceRouter.equals(destinationRouter)) {
            return true;
        }
        Map<String, List<Router>> graph = new HashMap<>();
        List<Router> allRouters = new ArrayList<>(routerMap.values());
        for(int i = 0; i < allRouters.size(); i++) {
            Router a = allRouters.get(i);
            for(int j = i+1; j < allRouters.size(); j++) {
                Router b = allRouters.get(j);
                double distance = a.distance(b);
                if(distance <= range) {
                    graph.computeIfAbsent(a.name, r -> new ArrayList<>()).add(b);
                    graph.computeIfAbsent(b.name, r -> new ArrayList<>()).add(a);
                }
            }
        }
        Router source = routerMap.get(sourceRouter);
        Router destination = routerMap.get(destinationRouter);
        return canReachDestination(source, destination, routerMap, graph);
    }

    private boolean canReachDestination(Router source, Router destination, Map<String, Router> routerMap, Map<String, List<Router>> graph) {
        PriorityQueue<Event> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.time));
        Set<String> settled = new HashSet<>();
        pq.offer(new Event(0.0d, source.name));
        /*for(Router neighbor: graph.getOrDefault(source.name, List.of())) {
            pq.offer(new Event(source.distance(neighbor), neighbor.name));
        }*/
        while (!pq.isEmpty()) {
            Event curr = pq.poll();
            if(settled.contains(curr.target)) {
                continue;
            }
            List<Event> tiedToBroadcastCurr = new ArrayList<>(), others = new ArrayList<>();
            tiedToBroadcastCurr.add(curr);
            while (!pq.isEmpty() && Math.abs(curr.time - pq.peek().time) <= ARRIVAL_TIME_TOLERANCE) {
                Event toq = pq.poll();
                if(toq.target.equals(curr.target)) {
                    tiedToBroadcastCurr.add(toq);
                } else {
                    others.add(toq);
                }
            }
            pq.addAll(others);
            settled.add(curr.target);
            if(curr.target.equals(destination.name)) {
                return tiedToBroadcastCurr.size() == 1;
            }
            if(tiedToBroadcastCurr.size() > 1) {
                // Current Event can not be broadcasted to curr.target
                continue;
            }

            Router currRouter = routerMap.get(curr.target);
            for(Router next: graph.getOrDefault(curr.target, List.of())) {
                pq.offer(new Event(curr.time + currRouter.distance(next), next.name));
            }
        }
        return false;
    }

    boolean canReachDestination(List<String> routers, String sourceRouter, String destinationRouter, double range) {
        Map<String, Router> routerMap = new HashMap<>();
        for (String r : routers) {
            Router router = new Router(r);
            routerMap.put(router.name, router);
        }
        if (!routerMap.containsKey(sourceRouter) || !routerMap.containsKey(destinationRouter)) {
            return false;
        }
        if (sourceRouter.equals(destinationRouter)) {
            return true;
        }

        Map<String, List<Router>> graph = new HashMap<>();
        List<Router> all = new ArrayList<>(routerMap.values());
        for (int i = 0; i < all.size(); i++) {
            for (int j = i + 1; j < all.size(); j++) {
                Router a = all.get(i), b = all.get(j);
                if (a.distance(b) <= range) {
                    graph.computeIfAbsent(a.name, e -> new ArrayList<>()).add(b);
                    graph.computeIfAbsent(b.name, e -> new ArrayList<>()).add(a);
                }
            }
        }

        return simulateBroadcast(routerMap, graph, sourceRouter, destinationRouter);
    }

    /**
     * Propagates messages in non-decreasing order of arrival time (time is proportional to
     * Euclidean distance), Dijkstra-style. A router only broadcasts once it is confirmed to
     * have received a single message at its earliest arrival time; if two or more messages
     * tie for earliest arrival (within ARRIVAL_TIME_TOLERANCE), they collide, the message is
     * discarded, and that router never broadcasts. Messages arriving after a router's earliest
     * arrival time are ignored (a settled router is never re-evaluated).
     */
    private boolean simulateBroadcast(Map<String, Router> routerMap, Map<String, List<Router>> graph,
                                       String sourceRouter, String destinationRouter) {
        Map<String, Boolean> active = new HashMap<>();
        //active.put(sourceRouter, true);
        PriorityQueue<Event> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.time));
        pq.offer(new Event(0.0d, sourceRouter));
        /*Router source = routerMap.get(sourceRouter);
        for (Router neighbor : graph.getOrDefault(sourceRouter, List.of())) {
            pq.offer(new Event(source.distance(neighbor), neighbor.name));
        }*/

        while (!pq.isEmpty()) {
            Event first = pq.poll();
            if (active.containsKey(first.target)) {
                continue;
            }
            List<Event> tiedForEarliest = new ArrayList<>();
            tiedForEarliest.add(first);
            List<Event> other = new ArrayList<>();
            while (!pq.isEmpty() && pq.peek().time - first.time <= ARRIVAL_TIME_TOLERANCE) {
                Event next = pq.poll();
                if (next.target.equals(first.target)) {
                    tiedForEarliest.add(next);
                } else {
                    other.add(next);
                }
            }
            pq.addAll(other);

            if (tiedForEarliest.size() > 1) {
                if(first.target.equals(destinationRouter)) {
                    return false;
                }
                active.put(first.target, false);
                continue;
            }
            active.put(first.target, true);
            if (first.target.equals(destinationRouter)) {
                return true;
            }
            Router node = routerMap.get(first.target);
            for (Router neighbor : graph.getOrDefault(first.target, List.of())) {
                if (!active.containsKey(neighbor.name)) {
                    pq.offer(new Event(first.time + node.distance(neighbor), neighbor.name));
                }
            }
        }
        return false;
    }

    private static class Event {
        final double time;
        final String target;

        Event(double time, String target) {
            this.time = time;
            this.target = target;
        }
    }

    private class Router {
        String name;
        int x;
        int y;
        public Router(String router) {
            String[] arr = router.split(",");
            name = arr[0].trim();
            x = Integer.parseInt(arr[1].trim());
            y = Integer.parseInt(arr[2].trim());
        }

        private double distance(Router other) {
            int deltaX = x - other.x, deltaY = y - other.y;
            return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
        }
    }

    /**
     * Given a grid of m*n, start cell and target cell, there are cats on cell cats
     * A mouse is trying to visit target cell starting from start cell, while maintaning maximum
     * distance from any of the cat, find maximum distance, a mouse can maintain from cat while visiting
     * path from source to target
     * @param m: number of rows
     * @param n: number of columns
     * @param start: start cell
     * @param target: target cell
     * @param cats: cells having cat
     * @return
     */
    public int maximumSafenessFactor(int m, int n, int[] start, int[] target, int[][] cats) {
        int[][] safeness = new int[m][n];
        for(int[] row: safeness) {
            Arrays.fill(row, m*n);
        }
        Deque<int[]> queue = new ArrayDeque<>();
        for(int[] cat: cats) {
            if((cat[0] == start[0] && cat[1] == start[1]) ||
                    (cat[0] == target[0] && cat[1] == target[1])) {
                return 0;
            }
            safeness[cat[0]][cat[1]] = 0;
            queue.offer(new int[]{cat[0], cat[1], 0});
        }
        while(!queue.isEmpty()) {
            int[] curr = queue.poll();
            int row = curr[0], col = curr[1], safenessFactor = curr[2];
            for(int[] dir: dirs) {
                int r = row + dir[0], c = col + dir[1];
                if(r < 0 || r >= m || c < 0 || c >= n || 1+safenessFactor >= safeness[r][c]) {
                    continue;
                }
                safeness[r][c] = 1 + safenessFactor;
                queue.offer(new int[]{r, c, 1+safenessFactor});
            }
        }
        int l = 0, r = m*n;
        int res = m*n;
        String resPath = "";
        List<int[]> path = new ArrayList<>();
        while(l <= r) {
            int mid = l + (r-l)/2;
            if(minSafenessPathExists(mid, start, target, safeness, path)) {
                resPath = createPath(path);
                path.clear();
                res = mid;
                l = mid + 1;
            } else {
                r = mid - 1;
            }
        }
        System.out.println("Path: " + resPath);
        return res;
    }

    private String createPath(List<int[]> path) {
        Collections.reverse(path);
        return path.stream()
                .map(n -> String.format("(%d,%d)", n[0], n[1]))
                .collect(Collectors.joining("->"));
    }
    //0, 1, 2, 3, 4, 5...10
    //t,t,t,t,f,f,f,f

    private boolean minSafenessPathExists(int minSafeness, int[] start, int[] target, int[][] safeness, List<int[]> path) {
        if(safeness[start[0]][start[1]] < minSafeness || safeness[target[0]][target[1]] < minSafeness) {
            return false;
        }
        int m = safeness.length, n = safeness[0].length;
        boolean[][] visited = new boolean[m][n];
        return dfs(minSafeness, start, target, safeness, visited,  path);
    }

    private boolean dfs(int mid, int[] curr, int[] target, int[][] safeness, boolean[][] visited, List<int[]> path) {
        if(curr[0] == target[0] && curr[1] == target[1]) {
            path.add(curr);
            return true;
        }
        if(visited[curr[0]][curr[1]]) {
            return false;
        }
        visited[curr[0]][curr[1]] = true;
        int m = safeness.length, n = safeness[0].length;
        for(int[] dir: dirs) {
            int r = curr[0] + dir[0], c = curr[1] + dir[1];
            if(r < 0 || r >= m || c < 0 || c >= n || safeness[r][c] < mid) {
                continue;
            }
            if(dfs(mid, new int[]{r, c}, target, safeness, visited, path)) {
                path.add(curr);
                return true;
            }
        }
        return false;
    }

    public String findPath(int m, int n, int[] start, int[] target) {
        int sr = start[0], sc = start[1], tr = target[0], tc = target[1];
        if (sr == tr && sc == tc) {
            return String.format("(%d, %d)", sr, sc);
        }

        int[][] prevRow = new int[m][n];
        int[][] prevCol = new int[m][n];
        boolean[][] visited = new boolean[m][n];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{sr, sc});
        visited[sr][sc] = true;

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int row = cur[0], col = cur[1];
            if (row == tr && col == tc) {
                break;
            }
            for (int[] dir : dirs) {
                int r = row + dir[0], c = col + dir[1];
                if (r < 0 || r >= m || c < 0 || c >= n || visited[r][c]) {
                    continue;
                }
                visited[r][c] = true;
                prevRow[r][c] = row;
                prevCol[r][c] = col;
                queue.offer(new int[]{r, c});
            }
        }

        if (!visited[tr][tc]) {
            return "";
        }

        List<int[]> path = new ArrayList<>();
        int r = tr, c = tc;
        while (true) {
            path.add(new int[]{r, c});
            if (r == sr && c == sc) {
                break;
            }
            int pr = prevRow[r][c], pc = prevCol[r][c];
            r = pr;
            c = pc;
        }
        Collections.reverse(path);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                sb.append(" -> ");
            }
            sb.append(String.format("(%d, %d)", path.get(i)[0], path.get(i)[1]));
        }
        return sb.toString();
    }

    public int minimumElevationGain(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        int[][] gainTo = new int[m][n];
        for (int[] row : gainTo) Arrays.fill(row, Integer.MAX_VALUE);
        gainTo[0][0] = 0;
        boolean[][] visited = new boolean[m][n];
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(c -> c[2]));
        pq.offer(new int[]{0, 0, 0});

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int row = curr[0], col = curr[1], gain = curr[2];
            if (visited[row][col]) continue;
            visited[row][col] = true;
            if (row == m - 1 && col == n - 1) return gain;

            for (int[] dir : dirs) {
                int r = row + dir[0], c = col + dir[1];
                if (r < 0 || r >= m || c < 0 || c >= n || visited[r][c]) continue;
                int step = Math.max(0, heights[r][c] - heights[row][col]);
                int newGain = gain + step;
                if (newGain < gainTo[r][c]) {
                    gainTo[r][c] = newGain;
                    pq.offer(new int[]{r, c, newGain});
                }
            }
        }
        return gainTo[m - 1][n - 1];
    }

    public int minimumEffortPathDijkstra(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        int minHeight = heights[0][0], maxHeight = heights[0][0];
        for(int[] row: heights) {
            for(int height: row) {
                minHeight = Math.min(minHeight, height);
                maxHeight = Math.max(maxHeight, height);
            }
        }
        int maxEffort = maxHeight - minHeight;
        int[][] effortTo = new int[m][n];
        for(int[] row: effortTo) {
            Arrays.fill(row, maxEffort);
        }
        boolean[][] visited = new boolean[m][n];
        effortTo[0][0] = 0;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(c -> c[2]));
        pq.offer(new int[]{0, 0, 0});
        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int row = curr[0], col = curr[1], effort = curr[2];
            if(visited[row][col]) {
                continue;
            }
            visited[row][col] = true;
            if(row == m-1 && col == n-1) {
                return effort;
            }
            for(int[] dir: dirs) {
                int r = row + dir[0], c = col + dir[1];
                if(r < 0 || r >= m || c < 0 || c >= n || visited[r][c]) {
                    continue;
                }
                int currEffort = Math.abs(heights[row][col] - heights[r][c]);
                int pathEffort = Math.max(effort, currEffort);
                if(pathEffort < effortTo[r][c]) {
                    effortTo[r][c] = pathEffort;
                    pq.offer(new int[]{r, c, pathEffort});
                }
            }

        }
        return maxEffort;
    }


    public int minimumEffortPathDfs(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        if(m == 1 && n == 1) {
            return 0;
        }
        int minHeight = heights[0][0], maxHeight = heights[0][0];
        for(int[] row: heights) {
            for (int height : row) {
                minHeight = Math.min(minHeight, height);
                maxHeight = Math.max(maxHeight, height);
            }
        }
        int l = 0, r = maxHeight - minHeight;
        int res = r;
        while(l <= r) {
            int mid = l + (r-l)/2;
            boolean[][] visited = new boolean[m][n];
            if(dfs(0, 0, mid, visited, heights)) {
                res = mid;
                r = mid-1;
            } else {
                l = mid + 1;
            }
        }
        return res;
    }
    private boolean dfs(int row, int col, int maxEffort, boolean[][] visited, int[][] heights) {
        int m = heights.length, n = heights[0].length;
        if(visited[row][col]) {
            return false;
        }
        visited[row][col] = true;
        if(row == m-1 && col == n-1) {
            return true;
        }
        for(int[] dir: dirs) {
            int r = row + dir[0], c = col + dir[1];
            if(r < 0 || r >= m || c < 0 || c >= n || visited[r][c]) {
                continue;
            }
            int currEffort = Math.abs(heights[row][col]-heights[r][c]);
            if(currEffort > maxEffort) {
                continue;
            }
            if(dfs(r, c, maxEffort, visited, heights)) {
                return true;
            }
        }
        return false;
    }

    public int minimumEffortPath(int[][] heights) {
        int m = heights.length, n = heights[0].length;
        if(m*n == 1) {
            return 0;
        }
        UnionFind uf = new UnionFind(m*n);
        List<int[]> edges = new ArrayList<>();
        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n; j++) {
                if(i > 0) {
                    edges.add(new int[]{(i-1)*n + j, i*n+j, Math.abs(heights[i][j] - heights[i-1][j])});
                    //uf.join((i-1)*n + j, i*n+j);
                }
                if(j > 0) {
                    edges.add(new int[]{i * n + j - 1, i * n + j, Math.abs(heights[i][j] - heights[i][j-1])});
                    //uf.join(i * n + j - 1, i * n + j);
                }
            }
        }
        Collections.sort(edges, Comparator.comparingInt(c -> c[2]));
        for(int[] edge: edges) {
            uf.join(edge[0], edge[1]);
            if(uf.find(0) == uf.find(m*n-1)) {
                return edge[2];
            }
        }
        return -1;
    }
    private class UnionFind {
        int[] parent;
        int[] rank;

        public UnionFind(int n) {
            parent = new int[n];
            rank = new int[n];
            for(int i = 0; i < n; i++) {
                parent[i] = i;
                rank[i] = 1;
            }
        }
        public int find(int x) {
            if(x != parent[x]) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        public void join(int x, int y) {
            int px = find(x), py = find(y);
            if(px == py) {
                return;
            }
            if(rank[px] < rank[py]) {
                parent[px] = py;
            } else {
                parent[py] = px;
                if(rank[px] == rank[py]) {
                    rank[px] += 1;
                }
            }
        }
    }

    private int[][] createElevationGainMatrix(int m, int n) {
        int[][] matrix = new int[m][n];
        for(int r = 0; r < m; r++) {
            for(int c = 0; c < n; c++) {
                matrix[r][c] = ThreadLocalRandom.current().nextInt(0, 8 + 1);
            }
        }
        return matrix;
    }

    List<Integer> countPrefixOccurrences(int n, List<String> edges, String letters, String s) {
        List<List<Integer>> graph = buildGraph(n, edges);
        int[] count = new int[s.length()];
        for(int i = 0; i < letters.length(); i++) {
            if(letters.charAt(i) != s.charAt(0)) {
                //Can't form a prefix
                continue;
            }
            //count[0] += 1;
            dfs(i, -1, 0, graph, letters, s, count);
        }
        List<Integer> res = new ArrayList<>();
        for(int cnt: count) {
            res.add(cnt);
        }
        return res;
    }

    private void dfs(int node,int parent,  int depth, List<List<Integer>> graph, String letters, String s, int[] count) {
        if(depth == s.length()) {
            return;
        }
        for(int nxt: graph.get(node)) {
            if(nxt == parent || letters.charAt(nxt) != s.charAt(depth)) {
                continue;
            }
            count[depth] += 1;
            dfs(nxt, node, depth+1, graph, letters, s, count);
        }
    }

    private List<List<Integer>> buildGraph(int n, List<String> edges) {
        List<List<Integer>> graph = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        for(String edge: edges) {
            String[] arr = edge.split("\\s");
            int src = Integer.parseInt(arr[0]);
            int dest = Integer.parseInt(arr[1]);
            graph.get(src).add(dest);
            graph.get(dest).add(src);
        }
        return graph;
    }

    int calculateExpression(String expression) {
        Node expressionTreeRoot = parseExpression(expression, new int[]{0});
        return expressionTreeRoot.evaluate();
    }

    private Node parseExpression(String expression, int[] pos) {
        Node node = parseTerm(expression, pos);
        skipWhiteSpaces(expression, pos);
        while (pos[0] < expression.length()
                && (expression.charAt(pos[0]) == '+' || expression.charAt(pos[0]) == '-')) {
            String operator = String.valueOf(expression.charAt(pos[0]));
            pos[0] += 1;
            Node right = parseTerm(expression, pos);
            node = new Node(operator, node, right);
            skipWhiteSpaces(expression, pos);
        }
        return node;
    }

    private Node parseTerm(String expression, int[] pos) {
        Node node = parseFactor(expression, pos);
        skipWhiteSpaces(expression, pos);
        while (pos[0] < expression.length()
                && (expression.charAt(pos[0]) == '*' || expression.charAt(pos[0]) == '/')) {
            String operator = String.valueOf(expression.charAt(pos[0]));
            pos[0] += 1;
            Node right = parseFactor(expression, pos);
            node = new Node(operator, node, right);
            skipWhiteSpaces(expression, pos);
        }
        return node;
    }

    private Node parseFactor(String expression, int[] pos) {
        skipWhiteSpaces(expression, pos);
        boolean isNeg = false;
        if(expression.charAt(pos[0]) == '+' || expression.charAt(pos[0]) == '-') {
            isNeg = expression.charAt(pos[0]) == '-';
            pos[0] += 1;
        }
        Node node;
        if(expression.charAt(pos[0]) == '(') {
            pos[0] += 1;
            node = parseExpression(expression, pos);
            skipWhiteSpaces(expression, pos);
            pos[0] += 1;
        } else {
            int start = pos[0];
            while(pos[0] < expression.length() && Character.isDigit(expression.charAt(pos[0]))) {
                pos[0] += 1;
            }
            node = new Node(Integer.parseInt(expression.substring(start, pos[0])));
        }
        return isNeg ? new Node("-", new Node(0), node) : node;
    }

    private void skipWhiteSpaces(String expression, int[] pos) {
        while(pos[0] < expression.length() && Character.isWhitespace(expression.charAt(pos[0]))) {
            pos[0] += 1;
        }
    }

    private class Node {
        String operator;
        Node left;
        Node right;
        int val;

        public Node(int val) {
            this.val = val;
        }

        public Node(String operator, Node left, Node right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        public int evaluate() {
            if(operator == null) {
                return val;
            }
            int leftVal = left.evaluate();
            int rightVal = right.evaluate();
            return apply(leftVal, rightVal);
        }

        private int apply(int leftVal, int rightVal) {
            return switch (operator.trim().charAt(0)) {
                case '+' ->  leftVal + rightVal;
                case '-' ->  leftVal - rightVal;
                case '*' ->  leftVal * rightVal;
                case '/' ->  leftVal / rightVal;
                default -> throw new IllegalArgumentException("Invalid expression " + operator);
            };
        }
    }
}