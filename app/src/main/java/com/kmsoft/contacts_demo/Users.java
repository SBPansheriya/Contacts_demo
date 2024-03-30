package com.kmsoft.contacts_demo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Users implements Serializable {
    public String contactId;
    public String image;
    public String fullName;
    public String first;
    public String last;
    ArrayList<Phone> phoneArrayList;
    public String personPhone;
    public String officePhone;
    private boolean isSelected;
    List<String> accountType;
    List<String> accountName;

    public Users() {
    }

    public Users(String contactId, String image, String fullName,String first, String last,ArrayList<Phone> phoneArrayList ,String personPhone, String officePhone,List<String> accountType,List<String> accountName) {
        this.contactId = contactId;
        this.image = image;
        this.fullName = fullName;
        this.first = first;
        this.last = last;
        this.phoneArrayList = phoneArrayList;
        this.personPhone = personPhone;
        this.officePhone = officePhone;
        this.accountType = accountType;
        this.accountName = accountName;
        isSelected = false;

    }

    public List<String> getAccountName() {
        return accountName;
    }

    public void setAccountName(List<String> accountName) {
        this.accountName = accountName;
    }

    public List<String> getAccountType() {
        return accountType;
    }

    public void setAccountType(List<String> accountType) {
        this.accountType = accountType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public ArrayList<Phone> getPhoneArrayList() {
        return phoneArrayList;
    }

    public void setPhoneArrayList(ArrayList<Phone> phoneArrayList) {
        this.phoneArrayList = phoneArrayList;
    }

    public String getPersonPhone() {
        return personPhone;
    }

    public void setPersonPhone(String personPhone) {
        this.personPhone = personPhone;
    }

    public String getOfficePhone() {
        return officePhone;
    }

    public void setOfficePhone(String officePhone) {
        this.officePhone = officePhone;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
