package org.example;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TreeStringPrefixCount {

    public static void main(String[] args) {
        TreeStringPrefixCount tspc = new TreeStringPrefixCount();
        //tspc.validate();
        tspc.validateTimeoutJob();
    }

    private void validateTimeoutJob() {
        List<String> logs = List.of("1,1,START", "2,2,START", "1,4,END", "3,8,START", "3,15,END");
        int timeoutThreshold = 5;

        int res = firstTimedOutJobId(logs, timeoutThreshold);
        System.out.printf("First time out job: %d\n", res);
    }

    private void validate() {
        List<Integer> res = countPrefixOccurrences(5, List.of("0 1", "1 2", "1 3", "3 4"), "ababa", "aba");
        //List<Integer> res = countPrefixOccurrences(4, List.of("0 1", "0 2", "0 3"), "baca", "abc");
        //List<Integer> res = countPrefixOccurrences(3, List.of("0 1", "1 2"), "aab", "aaa");
        System.out.printf("res: [%s]\n", res.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    /**
     * You are given an undirected tree where each node contains exactly one lowercase English letter. You are also given a string s.
     * For every prefix of s, find how many times that prefix appears in the tree.
     * A prefix appears in the tree if you can choose an ordered simple path of nodes such that the sequence of letters on that path is exactly equal to that prefix.
     * Since the tree is undirected, a valid path may move in any direction across edges. In a rooted view, the path may go down, up, or up and then down.
     * Return the answer as a list where the value at index i is the number of occurrences of the prefix s[0..i].
     * @param n Number of nodes from 0 to n-1
     * @param edges contains exactly n - 1 strings, where each string is in the format "u v" and represents an undirected edge between nodes u and v.
     * @param letters letters.charAt(i) is the letter stored at node i
     * @param s is the given string whose prefixes must be counted.
     * @return The returned list has length s.length()
     * For n = 5, edges = List.of("0 1", "1 2", "1 3", "3 4"), letters = "ababa", s = "aba")
     * return = List.of(3, 3, 2),
     * As Prefix appers 3 times, prefix ab appears 3 times and prefix aba appears twice in a path in tree
     */
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
        count[depth] += 1;
        if(depth+1 == s.length()) {
            return;
        }
        for(int nxt: graph.get(node)) {
            if(nxt == parent || letters.charAt(nxt) != s.charAt(depth+1)) {
                continue;
            }
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

    /**
     * While scanning the logs from left to right, a timeout may become known in two ways.
     * First, when an END log is seen, the matching job times out if its total duration is strictly greater than timeoutThreshold.
     * Second, before seeing an END log, a previously started job may already be known to have timed out if the current log timestamp is far enough ahead of that job's start timestamp.
     * Among all jobs that become known to have timed out at the same timestamp, return the smaller job id.
     *
     * @param logs logs[i] is in the format "jobId,timestamp,eventType"
     * jobId is an integer id of the job
     * timestamp is the time of that log entry
     * eventType is either START or END
     * @param timeoutThreshold
     * @return
     */
    private int firstTimedOutJobId(List<String> logs, int timeoutThreshold) {
        Map<Integer, Integer> jobStart = new HashMap<>();
        TreeMap<Integer, TreeSet<Integer>> timeLine = new TreeMap<>();
        for(String log: logs) {
            String[] logFields = log.split(",");
            int jobId = Integer.parseInt(logFields[0].trim());
            int ts = Integer.parseInt(logFields[1].trim());
            String type = logFields[2].trim();

            Integer candidate = null;
            var expiredJobsMap = timeLine.headMap(ts - timeoutThreshold);
            for(var entry: expiredJobsMap.entrySet()) {
                int smallest = entry.getValue().first();
                if(candidate == null || smallest < candidate) {
                    candidate = smallest;
                }
            }
            List<Integer> expiredTimeStamps = new ArrayList<>(expiredJobsMap.keySet());
            for(int expiredTs: expiredTimeStamps) {
                for(int expiredJobId: timeLine.remove(expiredTs)) {
                    jobStart.remove(expiredJobId);
                }
            }

            if("START".equals(type)) {
                jobStart.put(jobId, ts);
                timeLine.computeIfAbsent(ts, t -> new TreeSet<>()).add(jobId);
            } else {
                Integer startTime = jobStart.remove(jobId);
                if(startTime != null) {
                    if(ts - startTime > timeoutThreshold && (candidate == null || jobId < candidate)) {
                        candidate = jobId;
                    }
                    TreeSet<Integer> bucket = timeLine.get(startTime);
                    if(bucket != null) {
                        bucket.remove(jobId);
                        if(bucket.isEmpty()) {
                            timeLine.remove(startTime);
                        }
                    }
                }
            }

            if(candidate != null) {
                return candidate;
            }
        }
        return -1;
    }

    /**
     * Looking outward from person i in each direction: everyone at or below height[i] is visible
     * (a viewer at least as tall as a stretch of people sees over any dips/rises within it), up to
     * and including the first strictly taller person, who blocks further view. Past that blocker,
     * only strictly increasing "record" heights (relative to the last visible person) remain visible.
     * countVisiblePeople(i) is the sum of visible people looking left and looking right.
     *
     * @param heights Array of height of people
     * @return Number of people a person at index i can see to its left and right
     * For Eg for height = {1, 10, 6, 7, 9, 2, 4, 5}
     * res = {1, 7, 3, 3, 6, 4, 4, 4}
     * Person 0 sees {1}; 1 sees {0,2,3,4,5,6,7}; 2 sees {1,3,4}; 3 sees {1,2,4}; 4 sees {1,2,3,5,6,7};
     * 5 sees {1,4,6,7}; 6 sees {1,4,5,7}; 7 sees {1,4,5,6}
     */
    public int[] countVisiblePeople(int[] heights) {
        int n = heights.length;
        int[] nextGreater = nextGreaterToRight(heights);
        int[] rightChain = new int[n];
        for (int i = n - 1; i >= 0; i--) {
            rightChain[i] = nextGreater[i] == -1 ? 0 : 1 + rightChain[nextGreater[i]];
        }

        int[] prevGreater = nextGreaterToLeft(heights);
        int[] leftChain = new int[n];
        for (int i = 0; i < n; i++) {
            leftChain[i] = prevGreater[i] == -1 ? 0 : 1 + leftChain[prevGreater[i]];
        }

        int[] res = new int[n];
        for (int i = 0; i < n; i++) {
            int right = nextGreater[i] == -1
                    ? (n - 1 - i)
                    : (nextGreater[i] - i) + rightChain[nextGreater[i]];
            int left = prevGreater[i] == -1
                    ? i
                    : (i - prevGreater[i]) + leftChain[prevGreater[i]];
            res[i] = left + right;
        }
        return res;
    }

    private int[] nextGreaterToRight(int[] heights) {
        int n = heights.length;
        int[] nextGreater = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = n - 1; i >= 0; i--) {
            while (!stack.isEmpty() && heights[stack.peek()] <= heights[i]) {
                stack.pop();
            }
            nextGreater[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(i);
        }
        return nextGreater;
    }
    private int[] nextGreaterToLeft(int[] heights) {
        int n = heights.length;
        int[] prevGreater = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            while (!stack.isEmpty() && heights[stack.peek()] <= heights[i]) {
                stack.pop();
            }
            prevGreater[i] = stack.isEmpty() ? -1 : stack.peek();
            stack.push(i);
        }
        return prevGreater;
    }

    long maximumSumSubarray(int[] nums) {
        long runningSum = 0l;
        Map<Integer, Long> minPrefixSumForNum = new HashMap<>();
        long res = Long.MIN_VALUE;
        for(int num: nums) {
            long minPrefixSum = minPrefixSumForNum.containsKey(num)
                    ? Math.min( minPrefixSumForNum.get(num), runningSum):runningSum;
            minPrefixSumForNum.put(num, minPrefixSum);
            runningSum += num;
            long subArrSum = runningSum - minPrefixSum;
            res = Math.max(res, subArrSum);
        }
        return res;
    }


    int longestConstrainedPath(List<String> grid) {
        int m = grid.size(), n = grid.get(0).split(",").length;
        if(m*n == 1) {
            return 1;
        }
        int[][] matrix = new int[m][n];
        for(int i = 0; i < m; i++) {
            String[] arr = grid.get(i).split(",");
            for(int j = 0; j < n; j++) {
                matrix[i][j] = Integer.parseInt(arr[j]);
            }
        }
        int[][] dist = new int[m][n];
        int res = 0;
        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n; j++) {
                if(dist[i][j] == 0) {
                    boolean[][] visited = new boolean[m][n];
                    int currDist = dfs(i, j, 0, matrix, dist, visited);
                    res = Math.max(res, currDist);
                }
            }
        }
        return res;
    }
    int[][] dirs = {{0,1},{1,0}, {0, -1}, {-1, 0}};
    private int dfs(int i, int j, int prev, int[][] matrix,  int[][] dist, boolean[][] visited) {
        if(dist[i][j] > 0) {
            return dist[i][j];
        }
        if(visited[i][j]) {
            return 0;
        }
        visited[i][j] = true;
        int m = matrix.length, n = matrix[0].length;
        int res = 0;
        for(int[] dir: dirs) {
            int r = i + dir[0], c = j + dir[1];
            if(r < 0 || r >= m || c < 0 || c >= n) {
                continue;
            }
            if((prev == 0 && matrix[r][c] <= matrix[i][j])
            || (matrix[r][c] <= matrix[i][j] || matrix[r][c] <= prev)) {
                res = Math.max(res, 1 + dfs(r, c, matrix[i][j], matrix, dist, visited));
            }
        }
        return dist[i][j] = res;
    }

    List<String> possibleIntendedWords(String typed, List<String> dictionary) {
        List<List<CharFreq>> consecutiveCountList = new ArrayList<>();
        for(String word: dictionary) {
            consecutiveCountList.add(createFrequency(word));
        }
        List<CharFreq> type = createFrequency(typed);
        List<String> res = new ArrayList<>();
        for(int i = 0; i < dictionary.size(); i++) {
            List<CharFreq> dictWord = consecutiveCountList.get(i);
            if(dictWord.size() != type.size()) {
                continue;
            }
            boolean isMissType = true;
            for(int j = 0; j < type.size(); j++) {
                if(dictWord.get(j).ch != type.get(j).ch || type.get(j).freq < dictWord.get(j).freq) {
                    isMissType = false;
                    break;
                }
            }
            if(isMissType) {
                res.add(dictionary.get(i));
            }
        }
        return res;
    }

    private List<CharFreq> createFrequency(String word) {
        int len = word.length();
        List<CharFreq> res = new ArrayList<>();
        int currLen = 0;
        for(int i = 0; i < len; i++) {
            if(i == 0 || word.charAt(i) == word.charAt(i-1)) {
                currLen += 1;
            } else if(word.charAt(i) != word.charAt(i-1)) {
                res.add(new CharFreq(word.charAt(i-1), currLen));
                currLen = 1;
            }
            if(i == len-1) {
                res.add(new CharFreq(word.charAt(i), currLen));
            }
        }
        return res;
    }

    private record CharFreq(char ch, int freq){
        public boolean isContained(CharFreq other) {
            return this.ch == other.ch && this.freq >= other.freq;
        }
    };

    List<String> compilePackages(List<String> packages, int threadCount) {
        Map<String, List<String>> dependency = new HashMap<>();
        Map<String, Integer> ib = new HashMap<>();
        for(String pack: packages) {
            String[] arr = pack.split(",");
            for(int i = 1; i < arr.length; i++) {
                dependency.computeIfAbsent(arr[i], dep -> new ArrayList<>()).add(arr[0]);
                ib.merge(arr[0], 1, Integer::sum);
                ib.put(arr[i], ib.getOrDefault(arr[i], 0));
            }
        }
        Deque<String> queue = new ArrayDeque<>();
        List<String> candidatesForQueue = new ArrayList<>();
        for(var entry: ib.entrySet()) {
            if(entry.getValue() == 0) {
                candidatesForQueue.add(entry.getKey());
                //queue.offer(entry.getKey());
            }
        }
        Collections.sort(candidatesForQueue);
        for(String cand: candidatesForQueue) {
            queue.offer(cand);
        }
        int total = ib.size();
        List<String> res = new ArrayList<>();
        while (!queue.isEmpty()) {
            int size = queue.size();
            List<String> candidates = new ArrayList<>();
            for(int i = 0; i < Math.min(size, threadCount); i++) {
                String curr = queue.poll();
                candidates.add(curr);
                for(String deps: dependency.getOrDefault(curr, List.of())) {
                    ib.merge(deps, -1, Integer::sum);
                    if(ib.get(deps) == 0) {
                        ib.remove(deps);
                        queue.offer(deps);
                    }
                }
            }
            Collections.sort(candidates);
            res.addAll(candidates);
        }
        return res.size() < total ? List.of(): res;
    }

    private static final DateTimeFormatter WORKING_TIMELINE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    /**
     * Builds a timeline of who is working during each minute-granular interval, by sweeping over
     * each worker's start/end+1 events rather than pairwise-merging intervals. A pairwise stack
     * merge can miss a genuine overlap that only appears after an earlier split, e.g. when one
     * worker's inclusive end time equals another worker's start time.
     */
    List<String> getWorkingTimeline(List<String> workingHours) {
        Map<LocalTime, Set<String>> adds = new TreeMap<>();
        Map<LocalTime, Set<String>> removes = new TreeMap<>();
        Set<LocalTime> timePoints = new TreeSet<>();
        for(String wh:workingHours) {
            String[] arr = wh.split(",");
            String worker = arr[0].trim();
            LocalTime startTime = LocalTime.parse(arr[1].trim(), WORKING_TIMELINE_FORMATTER);
            LocalTime endTime = LocalTime.parse(arr[2].trim(), WORKING_TIMELINE_FORMATTER);
            adds.computeIfAbsent(startTime, t -> new HashSet<>()).add(worker);
            removes.computeIfAbsent(endTime.plusMinutes(1), t -> new HashSet<>()).add(worker);
            timePoints.add(startTime);
            timePoints.add(endTime.plusMinutes(1));
        }
        List<String> res = new ArrayList<>();
        List<LocalTime> timeline = new ArrayList<>(timePoints);
        Set<String> activeWorkers = new TreeSet<>();
        for(int i = 0; i < timeline.size(); i++) {
            LocalTime now = timeline.get(i);
            activeWorkers.removeAll(removes.getOrDefault(now, Set.of()));
            activeWorkers.addAll(adds.getOrDefault(now, Set.of()));

            if(i+1 < timeline.size() && !activeWorkers.isEmpty()) {
                LocalTime segmentEnd = timeline.get(i+1).minusMinutes(1);
                res.add(String.format("%s to %s -> %s",
                        now.format(WORKING_TIMELINE_FORMATTER), segmentEnd.format(WORKING_TIMELINE_FORMATTER),
                        activeWorkers.stream().collect(Collectors.joining(", "))
                        ));
            }
        }
        return res;
    }


    List<Integer> getDaysWhenAtLeastKPeopleAreFree(List<String> records, int d, int k) {
        int[] assigned = new int[d+2];
        Set<Integer> persons = new HashSet<>();
        for(String record: records) {
            String[] arr = record.split(",");
            int start = Integer.parseInt(arr[1].trim());
            int end = Integer.parseInt(arr[2].trim());
            assigned[start] += 1;
            assigned[end+1] -= 1;
            persons.add(Integer.parseInt(arr[0].trim()));
        }
        //Arrays.sort(assigned);
        List<Integer> res = new ArrayList<>();
        int total = persons.size();
        for(int cd = 1; cd <= d; cd++) {
            total -= assigned[cd];
            if(total >= k) {
                res.add(cd);
            }
        }
        return res;
    }
    int minCpusNeeded(List<Integer> startTimes, int taskLength) {
        int maxStart = startTimes.stream().max(Integer::compareTo).get();
        int targetEndTime = maxStart + taskLength;
        int l = 1, r = startTimes.size();
        int res = r;
        while (l <= r) {
            int mid = l + (r-l)/2;
            if(canComplete(mid, targetEndTime ,startTimes, taskLength)) {
                res = mid;
                r = mid-1;
            } else {
                l = mid + 1;
            }
        }
        return res;
    }

    private boolean canComplete(int cpuCount, int maxEndTime, List<Integer> startTimes, int taskLength) {
        int requiredCpu = 0;
        PriorityQueue<Integer> freeAt = new PriorityQueue<>();
        for(int start: startTimes) {
            if(freeAt.isEmpty() || (freeAt.peek() + taskLength) > maxEndTime) {
                requiredCpu += 1;
                freeAt.offer(start+taskLength);
            } else {
                int tos = freeAt.remove();
                freeAt.offer(tos + taskLength);
            }
            if(requiredCpu > cpuCount) {
                break;
            }
        }
        return requiredCpu <= cpuCount;
    }

    double computeOverallErrorMetric(List<String> checkpoints, List<String> samples) {
        if (checkpoints.isEmpty()) {
            return 0.0d;
        }
        double res = 0.0d;
        int[] prevCheckPointValue = parseValues(checkpoints.get(0));
        int ci = 1;
        for (int si = 0; si < samples.size(); si++) {
            if (ci >= checkpoints.size()) {
                break;
            }
            int[] sampleValue = parseValues(samples.get(si));
            if(sampleValue[0] < prevCheckPointValue[0]) {
                continue;
            }
            if(sampleValue[0] == prevCheckPointValue[0]) {
                res += distance(sampleValue[1], sampleValue[2], prevCheckPointValue[1], prevCheckPointValue[2]);
                continue;
            }
            int[] currCheckPointValues = parseValues(checkpoints.get(ci));
            ci += 1;
            while(ci < checkpoints.size()
                    && currCheckPointValues[0] < sampleValue[0]) {
                prevCheckPointValue = currCheckPointValues;
                currCheckPointValues = parseValues(checkpoints.get(ci));
                ci += 1;
            }
            if(sampleValue[0]  == currCheckPointValues[0]) {
                res += distance(sampleValue[1], sampleValue[2], currCheckPointValues[1], currCheckPointValues[2]);
                continue;
            }
            if(sampleValue[0] > prevCheckPointValue[0] && sampleValue[0] < currCheckPointValues[0]) {
                int[] expected = interpolate(sampleValue, prevCheckPointValue, currCheckPointValues);
                res += distance(sampleValue[1], sampleValue[2], expected[0], expected[1] );
            }
        }
        return res;
    }

    private int[] interpolate(int[] sampleValue, int[] prevCheckPointValue, int[] currCheckPointValues) {
        int x1 = prevCheckPointValue[1], y1 = prevCheckPointValue[2], x2 = currCheckPointValues[1], y2 = currCheckPointValues[2];
        int t = sampleValue[0], t1 = prevCheckPointValue[0], t2 =  currCheckPointValues[0];
        int expectedX = x1 + (x2 - x1) * (t - t1) / (t2 - t1);
        int expectedY = y1 + (y2 - y1) * (t - t1) / (t2 - t1);
        return new int[]{expectedX, expectedY};
    }

    private double distance(int x1, int y1, int x2, int y2) {
        int deltaX = x1-x2, deltaY = y1-y2;
        return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
    }

    private int[] parseValues(String s) {
        String[] arr = s.split(",");
        return new int[] {
                Integer.parseInt(arr[0].trim()),
                Integer.parseInt(arr[1].trim()),
                Integer.parseInt(arr[2].trim())
        };
    }
}
