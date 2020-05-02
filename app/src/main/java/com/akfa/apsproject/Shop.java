package com.akfa.apsproject;

import java.util.TreeMap;

public class Shop {
    public String name;
    public TreeMap<Integer, String> equipmentLines = new TreeMap<>();
    public Shop(String name)
    {
        this.name = name;
    }
}
