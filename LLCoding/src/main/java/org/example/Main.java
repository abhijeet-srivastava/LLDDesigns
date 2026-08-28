package org.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Main llcoding = new Main();
    }

    List<Integer> countPrefixOccurrences(int n, List<String> edges, String letters, String s) {
        List<List<Integer>> graph = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        for(String edge: edges) {
            String[] arr = edge.split("\\s");
            int u = Integer.parseInt(arr[0]), v = Integer.parseInt(arr[1]);
            graph.get(u).add(v);
            graph.get(v).add(u);
        }
        int[] res = new int[s.length()];
        for(int i = 0; i < n; i++) {
            if(letters.charAt(i) != s.charAt(0)) {
                continue;
            }
            dfs(0, i, -1, graph, letters, s, res);
        }
        return Arrays.stream(res).boxed().toList();

    }

    private void dfs(int idx, int node, int parent, List<List<Integer>> graph, String letters, String s, int[] res) {
        if(letters.charAt(node) != s.charAt(idx)) {
            return;
        }
        res[idx] += 1;
        for(int nxt: graph.get(node)) {
            if(nxt == parent) {
                continue;
            }
            dfs(idx+1, nxt, node, graph, letters, s, res);
        }
    }

    int firstTimedOutJobId(List<String> logs, int timeoutThreshold) {
        Deque<int[]> timeLine = new ArrayDeque<>();
        Set<Integer> completed = new HashSet<>();
        Map<Integer, Integer> jobStart = new HashMap<>();
        for(String log: logs) {
            String[] arr = log.split(",");
            int jobId = Integer.parseInt(arr[0].trim());
            int ts = Integer.parseInt(arr[1].trim());
            String type = arr[2].trim();
            if("START".equals(type.toUpperCase())) {
                if (!jobStart.containsKey(jobId)) {
                    timeLine.addLast(new int[]{jobId, ts});
                }
                jobStart.put(jobId, ts);
            }  else {
                int start = jobStart.get(jobId);
                if(ts-start > timeoutThreshold){
                    return jobId;
                }
                completed.add(jobId);
            }
            while (!timeLine.isEmpty()) {
                int[] earliest = timeLine.peekFirst();
                if(ts - earliest[1] <= timeoutThreshold) {
                    break;
                }
                earliest = timeLine.removeFirst();
                if(completed.contains(earliest[0])) {
                    jobStart.remove(earliest[0]);
                    completed.remove(earliest[0]);
                    continue;
                }
                return earliest[0];
            }
        }
        return -1;
    }

    int maximumCircleScore(List<Integer> tileScores) {
        int t1 = 0;
        int t2 = 0;
        int max = 0;
        for(int score:tileScores) {
            int tmp = t1+score;
            t1 = t2;
            t2 = Math.max(t2, tmp);
            max = Math.max(t1, t2);
        }
        return max;
    }

    int minimumSteps(List<String> grid, int k, int sourceRow, int sourceColumn, int destinationRow, int destinationColumn) {
        int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1, 0}};
        int m = grid.size(), n = grid.get(0).length();
        int[][] DP = new int[m][n];
        for(int[] row: DP) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        DP[sourceRow][sourceColumn] = 0;
        Deque<int[]> queue = new ArrayDeque<>();
        queue.offer(new int[]{sourceRow, sourceColumn, 0});
        int steps = 0;
        while(!queue.isEmpty()) {
            int size = queue.size();
            while(size-- > 0) {
                int[] curr = queue.remove();
                int row = curr[0], col = curr[1], dist = curr[2];
                if(row == destinationRow && col == destinationColumn) {
                    return dist;
                }
                for(int[] dir: dirs) {
                    int dx = dir[0], dy = dir[1], count = 0;
                    int r = row + dx, c = col + dy;
                    while(r >= 0 && r < m
                            && c >= 0 && c < n && count < k
                            && grid.get(r).charAt(c) != '#') {
                        if(DP[r][c] > dist+1) {
                            DP[r][c] = dist + 1;
                            queue.offer(new int[]{r, c, dist+1});
                        }
                        count += 1;
                        r += dx;
                        c += dy;
                    }
                }

            }
            steps += 1;
        }
        return -1;
    }
}