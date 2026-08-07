package org.example;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ChatLogsProcessor {
    Set<String> chattingPair;
    Map<String, TreeSet<Integer>> userScoreUpdateEvents;

    public ChatLogsProcessor() {
        this.chattingPair = new HashSet<>();
        //this.scoreUpdateEvents = new TreeMap<>();
        this.userScoreUpdateEvents = new HashMap<>();
    }

    void registerEvent(String sender, String receiver, int currentTime) {
        String key = getChattingPairKey(sender, receiver);
        if(!chattingPair.add(key)) {
            return;
        }
        userScoreUpdateEvents.computeIfAbsent(sender, s-> new TreeSet<>()).add(currentTime);
        userScoreUpdateEvents.computeIfAbsent(receiver, s-> new TreeSet<>()).add(currentTime);
    }

    String getMostActiveUser(int currentTime) {
        if(userScoreUpdateEvents.isEmpty()) {
            return "";
        }
        int maxScore = 0;
        String maxAttractiveUser = null;
        for(var entry: userScoreUpdateEvents.entrySet()) {
            int currUserScore = entry.getValue().headSet(currentTime, true).size();
            if(maxAttractiveUser == null || currUserScore > maxScore) {
                maxScore = currUserScore;
                maxAttractiveUser = entry.getKey();
            } else if(currUserScore == maxScore && maxAttractiveUser.compareTo(entry.getKey()) > 0) {
                maxAttractiveUser = entry.getKey();
            }
        }
        return maxAttractiveUser;
    }

    private static String getChattingPairKey(String sender, String receiver) {
        String smaller = sender, larger = receiver;
        if(smaller.compareTo(larger) > 0) {
            smaller = receiver;
            larger = sender;
        }
        String key = smaller.trim() + "#" + larger.trim();
        return key;
    }
}
