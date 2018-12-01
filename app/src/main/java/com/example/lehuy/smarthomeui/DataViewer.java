package com.example.lehuy.smarthomeui;

public class DataViewer {
    private String data;

    // Image name (Without extension)
    private String dataImage;
    private String unit;

    public DataViewer(String data, String dataImage, String unit) {
        this.data = data;
        this.dataImage = dataImage;
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getdataImage() {
        return dataImage;
    }

    public void setDataImage(String dataImage) {
        this.dataImage = dataImage;
    }

    @Override
    public String toString()  {
        return this.data+" (unit: "+ this.unit+")";
    }
}
