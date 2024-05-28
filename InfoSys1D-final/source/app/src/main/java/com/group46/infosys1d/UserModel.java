package com.group46.infosys1d;

public class UserModel {

    String userID;
    String userName;
    String userEmail;



    String itemID;
    String itemName;

    public UserModel(String userID, String userName, String userEmail, String userPassword, String itemID, String itemName) {
        this.userID = userID;
        this.userName = userName;
        this.itemID = itemID; // Assigning the parameter value to the field
        this.itemName = itemName; // Assigning the parameter value to the field
    }



    public UserModel() {
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }






    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }



    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
}