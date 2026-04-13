package com.example.cementtablet;

public class CementLog {
    private String dGather;
    private String cDate;
    private String cTime;
    private String cLine;
    private String cSample;
    private String measurement;
    private boolean isSaved;

    public CementLog(String dGather, String cDate, String cTime, String cLine, String cSample, String measurement) {
        this.dGather = dGather;
        this.cDate = cDate;
        this.cTime = cTime;
        this.cLine = cLine;
        this.cSample = cSample;
        this.measurement = measurement;
        this.isSaved = false;
    }

    public CementLog() {
        this.dGather = "";
        this.cDate = "";
        this.cTime = "";
        this.cLine = "";
        this.cSample = "";
        this.measurement = "";
        this.isSaved = false;

        java.util.Date now = new java.util.Date();
        this.dGather = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(now);
        this.cDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(now);
        this.cTime = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(now);
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public String getDGather() { return dGather; }
    public void setDGather(String dGather) { this.dGather = dGather; }

    public String getCDate() { return cDate; }
    public void setCDate(String cDate) { this.cDate = cDate; }

    public String getCTime() { return cTime; }
    public void setCTime(String cTime) { this.cTime = cTime; }

    public String getCLine() { return cLine; }
    public void setCLine(String cLine) { this.cLine = cLine; }

    public String getCSample() { return cSample; }
    public void setCSample(String cSample) { this.cSample = cSample; }

    public String getMeasurement() { return measurement; }
    public void setMeasurement(String measurement) { this.measurement = measurement; }
}
