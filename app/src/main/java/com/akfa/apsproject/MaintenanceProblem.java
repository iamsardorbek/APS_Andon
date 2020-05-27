package com.akfa.apsproject;

//---------КЛАСС ОБЪЕКТОВ ИСПОЛЬЗУЕТСЯ В QUEST POINT DYNAMIC, REPAIRERS PROBLEMS LIST, REPAIRERS SEP PROB ДЛЯ УДОБНОЙ РАБОТЫ С ДАННЫМИ ПРО ТО ПРОБЛЕМЫ----------//
//---------ДЛЯ ЗАПИСИ NODES В FIREBASE НЕПОСРЕДСТВЕННО ИЗ ОБЪЕКТОВ СУЩ ТРЕБОВАНИЕ, ЧТО У КЛАССА ДОЛЖЕН БЫТЬ ПУСТОЙ КОНСТРУКТОР, PUBLIC ГЕТТЕРЫ---------//
public class MaintenanceProblem {
    public String date, time, detected_by_employee, shop_name, equipment_line_name;
    public int station_no, point_no, shop_no, equipment_line_no;
    public boolean solved;

    public MaintenanceProblem() { }

    public MaintenanceProblem(String detected_by_employee, String date, String time, String shop_name, String equipment_line_name, int shop_no, int equipment_line_no, int station_no, int point_no)
    {
        this.solved = false;
        this.detected_by_employee = detected_by_employee;
        this.date = date;
        this.time = time;
        this.shop_name = shop_name;
        this.equipment_line_name = equipment_line_name;
        this.station_no = station_no;
        this.point_no = point_no;
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
    public int getStation_no()
    {
        return station_no;
    }
    public int getPoint_no()
    {
        return point_no;
    }
    public int getShop_no(){return shop_no;}
    public int getEquipment_line_no(){return equipment_line_no;}
    public boolean getSolved()
    {
        return solved;
    }
}
