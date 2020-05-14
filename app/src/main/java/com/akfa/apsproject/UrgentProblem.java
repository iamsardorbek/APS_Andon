package com.akfa.apsproject;

public class UrgentProblem {
    public String shop_name, equipment_name, qr_random_code, operator_login, who_is_needed_position, date_detected, time_detected, status;
    public int station_no;
    public UrgentProblem()
    {

    }

    public UrgentProblem(int stationNo, String equipmentName, String shopName, String operatorLogin, String whoIsNeededLogin, String qrRandomCode,
                         String dateTimeDetected, String time_detected, String status)
    {
        this.station_no = stationNo;
        this.shop_name = shopName;
        this.equipment_name = equipmentName;
        this.qr_random_code = qrRandomCode;
        this.operator_login = operatorLogin;
        this.who_is_needed_position = whoIsNeededLogin;
        this.date_detected = dateTimeDetected;
        this.time_detected = time_detected;
        this.status = status;
    }

    public int getStation_no() {
        return station_no;
    }

    public String getShop_name() {
        return shop_name;
    }

    public String getEquipment_name() {
        return equipment_name;
    }

    public String getQr_random_code() {
        return qr_random_code;
    }

    public String getOperator_login() {
        return operator_login;
    }

    public String getWho_is_needed_position() {
        return who_is_needed_position;
    }

    public String getDate_detected() {
        return date_detected;
    }

    public String getTime_detected() {
        return time_detected;
    }

    public String getStatus() {
        return status;
    }
}
