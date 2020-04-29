package com.akfa.apsproject;

public class EquipmentLine {
    String name;
    int numOfPoints;
    int[] numOfSubpoints = new int[numOfPoints];
    public EquipmentLine(String name, int numOfPoints, int[] numOfSubpoints)
    {
        this.name = name;
        this.numOfPoints = numOfPoints;
        this.numOfSubpoints = numOfSubpoints;
    }

}
