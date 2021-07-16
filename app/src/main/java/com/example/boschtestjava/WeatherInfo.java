package com.example.boschtestjava;

import java.util.HashMap;
import java.util.Map;

public class WeatherInfo {

    public Map<String, String> main;

    public WeatherInfo() {
        main = new HashMap<>();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        main.forEach((key, value) -> builder.append(key + " : " + value + "\n"));
        return builder.toString();
    }
}
