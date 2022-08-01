package LCE;

public class LCS {
    public int[] LongestCommonSubsequenceofIntegerArray(int[] target, int[] tester) {
        int i = 0;
        int[][] dp = new int[target.length + 1][tester.length + 1];
        for (i = 1; i <= target.length; i++) {
            for (int j = 1; j <= tester.length; j++) {
                if (target[i - 1] == tester[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        int x = target.length;
        int y = tester.length;
        int[] sb = new int[dp[target.length][tester.length]];
        int index = 0;
        while (x != 0 && y != 0) {
            if (target[x - 1] == tester[y - 1]) {
                sb[index] = target[x - 1];
                x--;
                y--;
                index++;
            } else if (dp[x][y] == dp[x - 1][y]) {
                x--;
            } else {
                y--;
            }
        }
        return sb;
    }

    public int[] Backtrack(int[] dp) {
        // reverse the dp array
        int[] sb = new int[dp.length];
        int index = 0;
        for (int i = dp.length - 1; i >= 0; i--) {
            sb[index] = dp[i];
            index++;
        }
        return sb;
    }

    public float ScoreSimilarity(int[] target, int[] tester) {
        // print elements of target and tester
        // System.out.print("[debug] target = ");
        // for (int i = 0; i < target.length; i++) {
        // System.out.print(target[i] + " ");
        // }
        // System.out.println();
        // System.out.print("[debug] tester = ");
        // for (int i = 0; i < tester.length; i++) {
        // System.out.print(tester[i] + " ");
        // }
        // System.out.println();
        // score = (1 - sum / (target.length - dp.length)) * k / k_max
        float score = 1;
        int sum = 0;
        int[] dp = Backtrack(LongestCommonSubsequenceofIntegerArray(target, tester));
        // print elements of dp
        // System.out.print("[debug] dp = ");
        // for (int i = 0; i < dp.length; i++) {
        // System.out.print(dp[i] + " ");
        // }
        // System.out.println();
        int k_max = tester.length;
        int k = dp.length;
        int[] sigma = new int[k + 1];
        // count and sum length of sequences within target that are not included in dp.
        int trigger = 0;
        for (int i = 0; i < dp.length; i++) {
            sigma[i] = 0;
            for (int j = trigger; j < target.length; j++) {
                // find the first element of target that is not included in dp.
                if (target[j] != dp[i]) {
                    sigma[i]++;
                } else {
                    trigger = j + 1;
                    break;
                }
            }
            if (i + 1 == dp.length) {
                sigma[i + 1] = target.length - trigger;
            }
        }
        for (int i = 0; i < sigma.length; i++) {
            // print elements of sigma
            // System.out.println("[debug] sigma[" + i + "] = " + sigma[i] + " ");
        }
        for (int i = 1; i < sigma.length - 1; i++) {
            sum += sigma[i];
        }
        // print sum
        // System.out.println("[debug] sum = " + sum);
        // score = (1 - sum / (target.length - dp.length)) * k / k_max
        // print sum / (target.length - dp.length) as floats
        // System.out.println("[debug] sum / (target.length - dp.length) = " + (float)
        // sum / (target.length - dp.length));
        // prevent zero division
        if (target.length != k)
            score = 1 - (float) sum / (target.length - k);
        // print score at this point
        // System.out.println("[debug] score = " + score);
        // print k/k_max as floats
        // System.out.println("[debug] k / k_max = " + (float) k / k_max);
        score = score * ((float) k / k_max);
        // print score
        // System.out.println("[debug] score = " + score + "\n");
        return score;
    }
}
