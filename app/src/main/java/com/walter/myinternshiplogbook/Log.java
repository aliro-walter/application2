
package com.walter.myinternshiplogbook;

public class Log {
    private String logContent;
    private byte[] evidenceImage;

    public Log(String logContent, byte[] evidenceImage) {
        this.logContent = logContent;
        this.evidenceImage = evidenceImage;
    }

    public String getLogContent() {
        return logContent;
    }

    public byte[] getEvidenceImage() {
        return evidenceImage;
    }
}
