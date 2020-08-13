package com.akfa.apsproject.general_data_classes;

public class PointData {
    private String equipmentName, shopName, qrCode;
    int shopNo, equipmentNo, pointNo;
    public PointData(int shopNo, int equipmentNo, int pointNo, String equipmentName, String shopName, String qrCode)
    {
        this.equipmentName = equipmentName;
        this.shopName = shopName;
        this.pointNo = pointNo;
        this.qrCode = qrCode;
        this.shopNo = shopNo;
        this.equipmentNo = equipmentNo;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public String getShopName() {
        return shopName;
    }

    public int getPointNo() {
        return pointNo;
    }

    public String getQrCode() {
        return qrCode;
    }

    public int getShopNo() {
        return shopNo;
    }

    public int getEquipmentNo() {
        return equipmentNo;
    }
}
