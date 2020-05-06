package com.akfa.apsproject;

public class EquipmentLine {
    private int shopNo, equipmentNo;
    private String startQRCode;
    public EquipmentLine(int shopNo, int equipmentNo, String startQRCode)
    {
        this.shopNo = shopNo;
        this.equipmentNo = equipmentNo;
        this.startQRCode = startQRCode;
    }

    public int getShopNo() { return shopNo; }
    public int getEquipmentNo() { return equipmentNo; }
    public String getStartQRCode() { return startQRCode; }

}
