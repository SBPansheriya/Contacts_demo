package com.kmsoft.contacts_demo;

import java.io.Serializable;

public class Phone implements Serializable {
    String phonenumber;
    int phoneType;
    String label;

    public Phone() {
    }

    public Phone(String phonenumber, int phoneType, String label) {
        this.phonenumber = phonenumber;
        this.phoneType = phoneType;
        this.label = label;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public int getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(int phoneType) {
        this.phoneType = phoneType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
