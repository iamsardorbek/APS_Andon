package com.akfa.apsproject;

public class Problem {
    public String date, time, detected_by_employee;
    public int shop, equipment_line, point, subpoint;
    public boolean solved;

    public Problem()
    {

    }

    public Problem(String detected_by_employee, String date, String time, int shop, int equipment_line,
                   int point, int subpoint)
    {
        this.solved = false;
        this.detected_by_employee = detected_by_employee;
        this.date = date;
        this.time = time;
        this.shop = shop;
        this.equipment_line = equipment_line;
        this.point = point;
        this.subpoint = subpoint;
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
    public int getShop()
    {
        return shop;
    }
    public int getEquipment_line()
    {
        return equipment_line;
    }
    public int getPoint()
    {
        return point;
    }
    public int getSubpoint()
    {
        return subpoint;
    }
    public boolean getSolved()
    {
        return solved;
    }
}
