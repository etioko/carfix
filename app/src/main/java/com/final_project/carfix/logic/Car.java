package com.final_project.carfix.logic;

import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by etioko on 06/01/2019.
 */

public class Car {

    private String carType;
    private String carModel;
    private String carNumber;
    private String carYear;
    private String carCode;
//    List<Treatment> treatmentList = new ArrayList<Treatment>();

    public Car()
    {

    }
    public Car(String carType, String carModel, String carNumber, String carYear, String carCode) {
        this.carType = carType;
        this.carModel = carModel;
        this.carNumber = carNumber;
        this.carYear = carYear;
        this.carCode = carCode;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }
//
//    public List<Treatment> getTreatmentList() {
//        return treatmentList;
//    }
//
//    public void setTreatmentList(List<Treatment> treatmentList) {
//        this.treatmentList = treatmentList;
//    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public String getCarYear() {
        return carYear;
    }

    public void setCarYear(String carYear) {
        this.carYear = carYear;
    }

    public String getCarCode() {
        return carCode;
    }

    public void setCarCode(String carCode) {
        this.carCode = carCode;
    }

//    public void addTreatList(Treatment treatment)
//    {
//        treatmentList.add(treatment);
//    }
    @Override
    public String toString() {
        return "Car{" +
                "carType='" + carType + '\'' +
                ", carModel='" + carModel + '\'' +
                ", carNumber='" + carNumber + '\'' +
                '}';
    }
}
