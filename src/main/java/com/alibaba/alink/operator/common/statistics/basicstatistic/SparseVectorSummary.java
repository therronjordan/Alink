package com.alibaba.alink.operator.common.statistics.basicstatistic;

import org.apache.flink.types.Row;
import com.alibaba.alink.common.linalg.DenseVector;
import com.alibaba.alink.common.linalg.SparseVector;
import com.alibaba.alink.common.linalg.Vector;
import com.alibaba.alink.common.utils.TableUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It is summary result of sparse vector.
 * You can get vectorSize, mean, variance, and other statistics from this class,
 * and It will return sparse vector when get statistics.
 */
public class SparseVectorSummary extends BaseVectorSummary {

    int colNum = -1;

    Map<Integer, VectorStatCol> cols = new HashMap();

    private static final String[] OUT_COL_NAMES = new String[]{"id", "count",
        "sum", "mean", "variance", "standardDeviation", "min", "max", "normL1", "normL2"};

    /**
     * It will generated by summary.
     */
    SparseVectorSummary() {
    }

    @Override
    public String toString() {
        List<Row> data = new ArrayList<>();

        for (int i = 0; i < vectorSize(); i++) {
            Row row = new Row(OUT_COL_NAMES.length);

            row.setField(0, i);
            row.setField(1, count);
            row.setField(2, sum(i));
            row.setField(3, mean(i));
            row.setField(4, variance(i));
            row.setField(5, standardDeviation(i));
            row.setField(6, min(i));
            row.setField(7, max(i));
            row.setField(8, normL1(i));
            row.setField(9, normL2(i));

            data.add(row);
        }

        return TableUtil.format(OUT_COL_NAMES, data);
    }

    @Override
    public int vectorSize() {
        int maxIndx = -1;
        Integer[] indics = cols.keySet().toArray(new Integer[0]);
        for (int i = 0; i < indics.length; i++) {
            if (maxIndx < indics[i]) {
                maxIndx = indics[i];
            }
        }
        colNum = Math.max(colNum, maxIndx + 1);
        return colNum;
    }

    /**
     * sum of each dimension.
     */
    @Override
    public Vector sum() {
        int[] indices = getIndices();
        double[] data = new double[indices.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = cols.get(indices[i]).sum;
        }

        return new SparseVector(vectorSize(), indices, data);
    }

    /**
     * mean of each dimension.
     */
    @Override
    public Vector mean() {
        int[] indices = getIndices();
        double[] data = new double[indices.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = cols.get(indices[i]).mean(count);
        }

        return new SparseVector(vectorSize(), indices, data);
    }

    /**
     * variance of each dimension.
     */
    @Override
    public Vector variance() {
        int[] indices = getIndices();
        double[] data = new double[indices.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = cols.get(indices[i]).variance(count);
        }

        return new SparseVector(vectorSize(), indices, data);
    }

    /**
     * standardDeviation of each dimension.
     */
    @Override
    public Vector standardDeviation() {
        int[] indices = getIndices();
        double[] data = new double[indices.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = cols.get(indices[i]).standardDeviation(count);
        }

        return new SparseVector(vectorSize(), indices, data);
    }

    /**
     * min of each dimension.
     */
    @Override
    public Vector min() {
        int[] indices = getIndices();
        double[] data = new double[indices.length];
        for (int i = 0; i < data.length; i++) {
            VectorStatCol statCol = cols.get(indices[i]);
            data[i] = 0;
            if(statCol.numNonZero > 0) {
                data[i] = Math.min(statCol.min, 0);
            }
        }

        return new SparseVector(vectorSize(), indices, data);
    }

    /**
     * max of each dimension.
     */
    @Override
    public Vector max() {
        int[] indices = getIndices();
        double[] data = new double[indices.length];
        for (int i = 0; i < data.length; i++) {
            VectorStatCol statCol = cols.get(indices[i]);
            data[i] = 0;
            if(statCol.numNonZero > 0) {
                data[i] = Math.max(statCol.max, 0);
            }
        }

        return new SparseVector(vectorSize(), indices, data);
    }

    /**
     * normL1 of each dimension.
     */
    @Override
    public Vector normL1() {
        int[] indices = getIndices();
        double[] data = new double[indices.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = cols.get(indices[i]).normL1;
        }

        return new SparseVector(vectorSize(), indices, data);
    }

    /**
     * normL2 of each dimension.
     */
    @Override
    public Vector normL2() {
        int[] indices = getIndices();
        double[] data = new double[indices.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = Math.sqrt(cols.get(indices[i]).squareSum);
        }
        return new SparseVector(vectorSize(), indices, data);
    }

    /**
     * the ith entry of vector is the number of values which is not zero.
     */
    public Vector numNonZero() {
        int colNum = vectorSize();
        double[] sum = new double[colNum];

        for (Map.Entry<Integer, VectorStatCol> entry : cols.entrySet()) {
            sum[entry.getKey()] = entry.getValue().numNonZero;
        }

        return new DenseVector(sum);
    }

    /**
     * return the number of values which is not zero.
     */
    public double numNonZero(int idx) {
        return numNonZero().get(idx);
    }

    /**
     * return indices.
     */
    private int[] getIndices() {
        Integer[] indices = cols.keySet().toArray(new Integer[0]);
        int[] out = new int[indices.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = indices[i];
        }
        return out;
    }

}