package com.akfa.apsproject;

public class UrgentProblem {
    public String shopName, equipmentName, qrRandomCode, operatorLogin, whoIsNeededLogin;
    public int stationNo;

    public UrgentProblem()
    {

    }

    public UrgentProblem(int stationNo, String equipmentName, String shopName, String operatorLogin, String whoIsNeededLogin, String qrRandomCode)
    {
        this.stationNo = stationNo;
        this.shopName = shopName;
        this.equipmentName = equipmentName;
        this.qrRandomCode = qrRandomCode;
        this.operatorLogin = operatorLogin;
        this.whoIsNeededLogin = whoIsNeededLogin;
    }

    public int getStationNo() {
        return stationNo;
    }

    public String getShopName() {
        return shopName;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getQrRandomCode() {
        return qrRandomCode;
    }

    public String getOperatorLogin() {
        return operatorLogin;
    }

    public String getWhoIsNeededLogin() {
        return whoIsNeededLogin;
    }
}
