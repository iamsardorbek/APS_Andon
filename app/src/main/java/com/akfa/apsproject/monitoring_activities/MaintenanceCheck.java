package com.akfa.apsproject.monitoring_activities;

public class MaintenanceCheck {
    private String checked_by, duration, equipment_name, shop_name, time_finished, date_finished;
    private int shop_no, equipment_no, num_of_detected_problems;
    public MaintenanceCheck(String checked_by, String duration, String equipment_name, String shop_name, String time_finished, String date_finished, int shop_no, int equipment_no, int num_of_detected_problems)
    {
        this.checked_by = checked_by;
        this.duration = duration;
        this.equipment_name = equipment_name;
        this.shop_name = shop_name;
        this.time_finished = time_finished;
        this.shop_no = shop_no;
        this.equipment_no = equipment_no;
        this.num_of_detected_problems = num_of_detected_problems;
        this.date_finished = date_finished;
    }

    public MaintenanceCheck(){}

    public String getChecked_by() {
        return checked_by;
    }

    public String getDuration() {
        return duration;
    }

    public String getEquipment_name() {
        return equipment_name;
    }

    public String getShop_name() {
        return shop_name;
    }

    public String getTime_finished() {
        return time_finished;
    }

    public int getNum_of_detected_problems() {
        return num_of_detected_problems;
    }

    public String getDate_finished() {
        return date_finished;
    }

    public int getShop_no() {
        return shop_no;
    }

    public int getEquipment_no() {
        return equipment_no;
    }
}
