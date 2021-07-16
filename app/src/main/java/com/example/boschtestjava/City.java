package com.example.boschtestjava;

import java.util.Objects;

public class City implements Comparable{

    public final String name;
    public final double latitude;
    public final double longitude;
    public final int population;

    public City(String name, double latitude, double longitude, int population) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.population = population;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", population=" + population +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        City other = (City) o;
        return Integer.compare(this.population, other.population);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return name.equals(city.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
