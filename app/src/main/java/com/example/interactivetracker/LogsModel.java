package com.example.interactivetracker;

public class LogsModel {
    String logCallSign;
    String logMessage;
    String msgColor;

    public LogsModel(String logCallSign, String logMessage, String msgColor) {
        this.logCallSign = logCallSign;
        this.logMessage = logMessage;
        this.msgColor = msgColor;
    }

    public String getLogCallSign() {
        return logCallSign;
    }
    public String getLogMessage() {
        return logMessage;
    }
    public String getMsgColor() {
        return msgColor;
    }


    public void setLogCallSign(String logCallSign) {
        this.logCallSign = logCallSign;
    }
    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
    public void setMsgColor(String msgColor) {
        this.msgColor = msgColor;
    }
}
