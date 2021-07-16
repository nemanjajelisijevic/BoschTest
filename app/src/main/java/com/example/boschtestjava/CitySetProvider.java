package com.example.boschtestjava;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CitySetProvider {

    protected Set<City> allCities;

    public CitySetProvider() {
        allCities = new TreeSet<>();
    }

    public boolean addCity(City city) {
        return allCities.add(city);
    }

    public boolean removeCity(City city) {
        return allCities.remove(city);
    }

    public boolean contains(City city) {
        return allCities.contains(city);
    }

    public final Set<City> getCitiesByCriteria(Predicate<City> criteria) {
        return allCities.stream().filter(criteria).collect(Collectors.toSet());
    }
}
