package io.phoenix;

public class KubeApplicationStatus {
    private String  message;

    public KubeApplicationStatus() {
    }

    public String getMessage() {
        return message;
    }

    public KubeApplicationStatus(String message){
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}