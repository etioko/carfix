package com.final_project.carfix.logic;

/**
 * Created by etioko on 06/01/2019.
 */

public class Treatment {

    private String carNumber;
    private String mileage;
    private String treatmentNeed;
    private String remarks;
    private String enterDate;
    private String exitDate;
    private String extra;
    private String tokenClient;
    private String vendor;
    private String bill;
    private boolean managerChangeTime;

    public Treatment(String carNumber,String tokenClient, String mileage, String remarks, String treatmentNeed, String enterDate, String exitDate, String extra, String vandor) {
        this.carNumber = carNumber;
        this.tokenClient = tokenClient;
        this.mileage = mileage;
        this.treatmentNeed = treatmentNeed;
        this.remarks =remarks;
        this.enterDate = enterDate;
        this.extra = extra;
        this.vendor = vandor;
        this.exitDate = exitDate;
        this.bill = "";
    }

    public Treatment() {
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getTreatmentNeed() {
        return treatmentNeed;
    }

    public void setTreatmentNeed(String treatmentNeed) {
        this.treatmentNeed = treatmentNeed;
    }

    public String getEnterDate() {
        return enterDate;
    }

    public void setEnterDate(String enterDate) {
        this.enterDate = enterDate;
    }

    public String getExitDate() {
        return exitDate;
    }

    public void setExitDate(String exitDate) {
        this.exitDate = exitDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getBill() {
        return bill;
    }

    public void setBill(String bill) {
        this.bill = bill;
    }

    public String getTokenClient() {
        return tokenClient;
    }

    public void setTokenClient(String tokenClient) {
        this.tokenClient = tokenClient;
    }

    public boolean getManagerChangeTime() {
        return managerChangeTime;
    }

    public void setManagerChangeTime(boolean managerChangeTime) {
        this.managerChangeTime = managerChangeTime;
    }
}