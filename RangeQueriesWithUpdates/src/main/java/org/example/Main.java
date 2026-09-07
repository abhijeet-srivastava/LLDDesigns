package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }

    }

    public String shortestCommonSupersequence(String str1, String str2) {
        int m = str1.length(), n = str2.length();
        int[][] DP = new int[m+1][n+1];
        for(int i = 1; i <= m; i++) {
            for(int j = 1; j <= n; j++) {
                DP[i][j] = Math.max(DP[i-1][j], DP[i][j-1]);
                if(str1.charAt(i-1) == str2.charAt(j-1)) {
                    DP[i][j] = Math.max(DP[i][j], 1 + DP[i-1][j-1]);
                }
            }
        }
        int len = m + n - DP[m][n];
        int idx = len-1;
        char[] scs = new char[len];
        int  i = m, j = n;
        while(i > 0 && j > 0) {
            if(str1.charAt(i-1) == str2.charAt(j-1)) {
                scs[idx--] = str1.charAt(i-1);
                i -= 1;
                j -= 1;
            } else if(DP[i-1][j] > DP[i][j-1]) {
                scs[idx--] = str1.charAt(i-1);
                i -= 1;
            } else {
                scs[idx--] = str2.charAt(j-1);
                j -= 1;
            }
        }
        while(i > 0) {
            scs[idx--] = str1.charAt(i-1);
             i -= 1;
        }
        while(j > 0) {
            scs[idx--] = str2.charAt(j - 1);
            j -= 1;
        }
        return new String(scs);
    }
}