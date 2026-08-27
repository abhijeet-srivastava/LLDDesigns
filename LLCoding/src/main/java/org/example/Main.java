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
}