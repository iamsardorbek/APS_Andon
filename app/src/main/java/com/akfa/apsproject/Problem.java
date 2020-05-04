package com.akfa.apsproject;

public class Problem {
    public String date, time, detected_by_employee, shop_name, equipment_line_name;
    public int point, subpoint, shop_no, equipment_line_no;
    public boolean solved;

    public Problem()
    {

    }

    public Problem(String detected_by_employee, String date, String time, String shop_name, String equipment_line_name, int shop_no, int equipment_line_no, int point, int subpoint)
    {
        this.solved = false;
        this.detected_by_employee = detected_by_employee;
        this.date = date;
        this.time = time;
        this.shop_name = shop_name;
        this.equipment_line_name = equipment_line_name;
        this.point = point;
        this.subpoint = subpoint;
        this.shop_no = shop_no;
        this.equipment_line_no = equipment_line_no;
    }

    public String getDetected_by_employee()
    {
        return detected_by_employee;
    }
    public String getDate()
    {
        return date;
    }
    public String getTime()
    {
        return time;
    }
    public String getShop_name()
    {
        return shop_name;
    }
    public String getEquipment_line_name()
    {
        return equipment_line_name;
    }
    public int getPoint()
    {
        return point;
    }
    public int getSubpoint()
    {
        return subpoint;
    }
    public int getShop_no(){return shop_no;}
    public int getEquipment_line_no(){return equipment_line_no;}
    public boolean getSolved()
    {
        return solved;
    }
}
