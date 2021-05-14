package com.example.common;

public class DeleteRequest extends AbstractMessage{

    private String delPath;

    public DeleteRequest() {
    }

    public DeleteRequest(String delPath) {
        this.delPath = delPath;
    }

    public String getDelPath() {
        return delPath;
    }

    public void setDelPath(String delPath) {
        this.delPath = delPath;
    }
}
