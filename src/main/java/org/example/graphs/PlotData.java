package org.example.graphs;

import com.google.gson.Gson;

import java.util.Arrays;

public class PlotData {

    private String title;
    private double[][] data;
    private String[] rows;
    private String rowsLabel;
    private String[] cols;
    private String colsLabel;

    public PlotData(){}

    public PlotData(String title, double[][] data, String[] rows, String[] cols, String rowsLabel, String colsLabel){
        this.title = title;
        this.data = data;
        this.rows = rows;
        this.cols = cols;
        this.rowsLabel = rowsLabel;
        this.colsLabel = colsLabel;
    }

    public String getTitle() {
        return title;
    }

    public double[][] getData() {
        return data;
    }

    public String[] getRows() {
        return rows;
    }

    public String[] getCols() {
        return cols;
    }

    /**
     * Convert this object to its JSON representation.
     * @return JSON string representation of this object.
     */
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return "PlotData{" +
                "title='" + title + '\'' +
                ", data=" + Arrays.deepToString(data) +
                ", rows=" + Arrays.toString(rows) +
                ", cols=" + Arrays.toString(cols) +
                '}';
    }
}
