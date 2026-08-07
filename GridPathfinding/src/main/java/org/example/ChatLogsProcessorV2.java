package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatLogsProcessorV2 {
    private final Set<String> chattingPair;
    private final List<ChatEvent> timeline;
    private final Map<String, Integer> userScores;
    private int cursor;
    private int lastQueryTime;

    public ChatLogsProcessorV2() {
        this.chattingPair = new HashSet<>();
        this.timeline = new ArrayList<>();
        this.userScores = new HashMap<>();
        this.cursor = 0;
        this.lastQueryTime = Integer.MIN_VALUE;
    }

    void registerEvent(String sender, String receiver, int currentTime) {
        String key = getChattingPairKey(sender, receiver);
        if (!chattingPair.add(key)) {
            return;
        }
        timeline.add(new ChatEvent(currentTime, sender, receiver));
    }

    String getMostActiveUser(int currentTime) {
        if (currentTime < lastQueryTime) {
            throw new IllegalArgumentException(
                    "currentTime must be non-decreasing across calls: got " + currentTime
                            + " after previous query at " + lastQueryTime);
        }
        lastQueryTime = currentTime;

        while (cursor < timeline.size() && timeline.get(cursor).time() <= currentTime) {
            ChatEvent event = timeline.get(cursor);
            userScores.merge(event.sender(), 1, Integer::sum);
            userScores.merge(event.receiver(), 1, Integer::sum);
            cursor++;
        }

        if (userScores.isEmpty()) {
            return "";
        }
        String mostActiveUser = null;
        int maxScore = 0;
        for (var entry : userScores.entrySet()) {
            if (mostActiveUser == null || entry.getValue() > maxScore
                    || (entry.getValue() == maxScore && entry.getKey().compareTo(mostActiveUser) < 0)) {
                maxScore = entry.getValue();
                mostActiveUser = entry.getKey();
            }
        }
        return mostActiveUser;
    }

    private static String getChattingPairKey(String sender, String receiver) {
        String smaller = sender.trim();
        String larger = receiver.trim();
        if (smaller.compareTo(larger) > 0) {
            String temp = smaller;
            smaller = larger;
            larger = temp;
        }
        return smaller + "#" + larger;
    }

    private record ChatEvent(int time, String sender, String receiver) {
    }
}