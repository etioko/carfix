package com.final_project.carfix.logic;

/**
 * Created by etioko on 20/12/2018.
 */

public class User
{
    private String email;
    private String permission;
    private String token;
    private ClientDetails clientDetails;
    private Car car;

    public User() {
    }

    public User(String email,String permission,String token ,ClientDetails clientDetails, Car car) {
        this.email = email;
        this.permission = permission;
        this.token = token;
        this.clientDetails = clientDetails;
        this.car = car;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }


    public ClientDetails getClientDetails() {
        return this.clientDetails;
    }

    public void setClientDetails(ClientDetails clientDetails) {
        this.clientDetails = clientDetails;
    }

    public Car getCar() {
        return this.car;
    }

    public void setCar(Car car) {
    this.car = car;
}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
