package com.example.boschtestjava;

import android.content.Context;
import android.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


public class WeatherServiceProvider {

    //http://api.openweathermap.org/data/2.5/weather?q=Belgrade,rs&units=metric&APPID=1cac422808e3b067231946656086f8b3

    public interface CityWeatherService {
        @GET("data/2.5/weather")
        Call<WeatherInfo> getWeatherInfo(@Query("q") String city, @Query("units") String units,@Query("APPID") String appId);
    }

    private Retrofit retrofit;
    private CityWeatherService weatherService;

    private Consumer<Pair<City, WeatherInfo>> cityWeatherCallback;

    private final String appId;
    private final String units;

    //cache the already fetched weathers
    private Map<City, WeatherInfo> cityWeatherCache;

    public WeatherServiceProvider(Context context, String appID, String units) {
        this.appId = appID;
        this.units = units;
        this.cityWeatherCache = new HashMap<>();

        this.retrofit =  new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.weatherService = retrofit.create(CityWeatherService.class);
    }

    public WeatherServiceProvider(Context context) {
        this(context, "1cac422808e3b067231946656086f8b3", "metric");
    }

    public WeatherServiceProvider setCityWeatherCallback(Consumer<Pair<City, WeatherInfo>> callback) {
        this.cityWeatherCallback = callback;
        return this;
    }

    //returns null
    public WeatherInfo getCachedInfo(City city) {
        if (cityWeatherCache.isEmpty())
            throw new IllegalStateException("Cache empty!");
        return cityWeatherCache.get(city);
    }

    public void getWeatherInfoForCities(Collection<City> cities)
    {
        cities.forEach(city -> {

            if (cityWeatherCache.containsKey(city))
                cityWeatherCallback.accept(Pair.create(city, cityWeatherCache.get(city)));
            else {
                weatherService.getWeatherInfo(city.name , units, appId).enqueue(new Callback<WeatherInfo>() {
                    @Override
                    public void onResponse(Call<WeatherInfo> call, Response<WeatherInfo> response) {

                        WeatherInfo successInfo =  response.body();

                        if (!cityWeatherCache.containsKey(city))
                            cityWeatherCache.put(city, successInfo);

                        cityWeatherCallback.accept(Pair.create(city, successInfo));

                    }

                    @Override
                    public void onFailure(Call<WeatherInfo> call, Throwable t) {
                        WeatherInfo errorInfo = new WeatherInfo();
                        errorInfo.main.put("temp", "Error: " + t.getMessage());
                        cityWeatherCallback.accept(Pair.create(city, errorInfo));
                    }
                });
            }
        });
    }

}
