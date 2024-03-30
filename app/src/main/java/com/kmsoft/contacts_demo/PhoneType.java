package com.kmsoft.contacts_demo;

public class PhoneType {
    int type;
    String label;

    public PhoneType() {
    }

    public PhoneType(int type, String label) {
        this.type = type;
        this.label = label;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
