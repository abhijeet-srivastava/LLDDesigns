package org.example;

public class NumMatrix {

    int n;
    RowSegmentTree[] rowSums;

    public NumMatrix(int[][] matrix) {
        n = matrix.length;
        rowSums = new RowSegmentTree[n];
        for(int i = 0; i < n; i++) {
            rowSums[i] = new RowSegmentTree(matrix[i]);
        }
    }

    public void update(int row, int col, int val) {
        rowSums[row].update(col, val);
    }

    public int sumRegion(int row1, int col1, int row2, int col2) {
        int sum = 0;
        for(int i = row1; i <= row2; i++) {
            sum += rowSums[i].query(col1, col2);
        }
        return sum;
    }

    public class RowSegmentTree {
        int n;
        int[] rowSum;
        public RowSegmentTree(int[] nums) {
            n = nums.length;
            rowSum = new int[n << 2];
            buildTree(1, 0, n-1, nums);
        }
        private void buildTree(int node, int lo, int hi, int[] nums){
            if(lo == hi) {
                rowSum[node] = nums[lo];
                return;
            }
            int mid = (lo + hi) >> 1;
            buildTree((node<<1), lo, mid, nums);
            buildTree((node<<1)|1, mid+1, hi, nums);
            rowSum[node] = rowSum[(node<<1)] + rowSum[(node<<1)|1];
        }
        public void update(int idx, int val) {
            update(1, 0, n-1, idx, val);
        }
        private void update(int node, int lo, int hi, int idx, int val) {
            if(lo == hi) {
                rowSum[node] = val;
                return;
            }
            int mid = (lo + hi) >> 1;
            if(idx <= mid) {
                update(node << 1, lo, mid, idx, val);
            } else {
                update((node << 1) | 1, mid+1, hi, idx, val);
            }
            rowSum[node] = rowSum[node<<1] + rowSum[(node<<1)|1];
        }
        public int query(int l, int r){
            return query(1, 0, n-1, l, r);
        }
        private int query(int node, int lo, int hi, int l, int r) {
            if(r < lo || hi < l) {
                return 0;
            }
            if(l <= lo && hi <= r) {
                return rowSum[node];
            }
            int mid = (lo + hi)>>1;
            int sl = query(node<<1, lo, mid, l , r);
            int sr = query((node<<1)|1, mid+1, hi, l, r);
            return sl+sr;
        }
    }

}
