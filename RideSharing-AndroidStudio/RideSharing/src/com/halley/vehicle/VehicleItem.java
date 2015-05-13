package com.halley.vehicle;

/**
 * Created by enclaveit on 5/5/15.
 */
public class VehicleItem {
    private String type, license_plate, reg_certificate, license_plate_img, vehicle_img, moto_insurance_img, vehicle_id;

    public VehicleItem() {
    }

    public String getVehicle_id() {
        return vehicle_id;
    }

    public void setVehicle_id(String vehicle_id) {
        this.vehicle_id = vehicle_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReg_certificate() {
        return reg_certificate;
    }

    public void setReg_certificate(String reg_certificate) {
        this.reg_certificate = reg_certificate;
    }

    public String getLicense_plate() {
        return license_plate;
    }

    public void setLicense_plate(String license_plate) {
        this.license_plate = license_plate;
    }

    public String getLicense_plate_img() {
        return license_plate_img;
    }

    public void setLicense_plate_img(String license_plate_img) {
        this.license_plate_img = license_plate_img;
    }

    public String getVehicle_img() {
        return vehicle_img;
    }

    public void setVehicle_img(String vehicle_img) {
        this.vehicle_img = vehicle_img;
    }

    public String getMoto_insurance_img() {
        return moto_insurance_img;
    }

    public void setMoto_insurance_img(String moto_insurance_img) {
        this.moto_insurance_img = moto_insurance_img;
    }
}
