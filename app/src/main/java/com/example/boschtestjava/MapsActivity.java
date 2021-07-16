package com.example.boschtestjava;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Pair;

import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.example.boschtestjava.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //map sliding constants
    private final LatLngBounds SERBIA_MAP_BOUNDS = new LatLngBounds(
            new LatLng(42, 19),
            new LatLng(46.25, 23.0)
    );

    //zoom constants
    private final float MIN_ZOOM_PREF = 7f;
    private final float MAX_ZOOM_PREF = 10f;

    private final float BIG_CITIES_ZOOM = MIN_ZOOM_PREF;
    private final float MEDIUM_CITIES_ZOOM = 8f;
    private final float ALL_CITIES_ZOOM = 9;

    //population constants
    private final int BIG_CITIES_POPULATION = 140000;
    private final int MEDIUM_CITIES_POPULATION = 60000;
    private final int ALL_CITIES_POPULATION = 0;

    private float previousCameraZoom = MIN_ZOOM_PREF;

    // google map
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    //providers
    private WeatherServiceProvider weatherController;
    private CitySetProvider citySetProvider;

    //city/marker cache
    private Map<City, Marker> cityMarkerMap;
    private Map<Marker, City> reverseCityMarkerMap;

    private IconGenerator iconFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init providers
        citySetProvider = new SerbiaCityProvider();
        weatherController = new WeatherServiceProvider(this);

        cityMarkerMap = new HashMap<>();
        reverseCityMarkerMap = new HashMap<>();

        iconFactory = new IconGenerator(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setMapType(MAP_TYPE_NORMAL);
        mMap.setLatLngBoundsForCameraTarget(SERBIA_MAP_BOUNDS);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(44.82, 20.44)));
        mMap.setMinZoomPreference(MIN_ZOOM_PREF);
        mMap.setMaxZoomPreference(MAX_ZOOM_PREF);

        Set<City> bigCities = citySetProvider.getCitiesByCriteria(city -> city.population > BIG_CITIES_POPULATION );
        weatherController.setCityWeatherCallback(this::showCityMarker).getWeatherInfoForCities(bigCities);

        mMap.setOnCameraMoveListener(() -> {

                float currentCameraZoom = mMap.getCameraPosition().zoom;

                if (currentCameraZoom != previousCameraZoom) {

                    Set<City> cities = null;

                    if (currentCameraZoom > previousCameraZoom) {//ZOOM IN

                        if (currentCameraZoom > ALL_CITIES_ZOOM)
                            cities = citySetProvider.getCitiesByCriteria(city -> city.population > ALL_CITIES_POPULATION);
                        else if (currentCameraZoom > MEDIUM_CITIES_ZOOM)
                            cities = citySetProvider.getCitiesByCriteria(city -> city.population > MEDIUM_CITIES_POPULATION);
                        else if (currentCameraZoom > BIG_CITIES_ZOOM)
                            cities = citySetProvider.getCitiesByCriteria(city -> city.population > BIG_CITIES_POPULATION);

                    } else {//ZOOM OUT

                        if (currentCameraZoom < MEDIUM_CITIES_ZOOM)
                            cities = citySetProvider.getCitiesByCriteria(city -> city.population > BIG_CITIES_POPULATION);
                        else if (currentCameraZoom < ALL_CITIES_ZOOM)
                            cities = citySetProvider.getCitiesByCriteria(city -> city.population > MEDIUM_CITIES_POPULATION);
                        else
                            cities = citySetProvider.getCitiesByCriteria(city -> city.population > ALL_CITIES_POPULATION);
                    }

                    cityMarkerMap.forEach((city, marker) -> hideCityMarker(city));

                    weatherController.getWeatherInfoForCities(cities);

                    previousCameraZoom = currentCameraZoom;
                }
            });

    }

    private void hideCityMarker(City city) {
        Marker currentMarker =  cityMarkerMap.get(city);
        if (currentMarker != null)
            currentMarker.setVisible(false);
    }

    private void showCityMarker(Pair<City, WeatherInfo> cityAndWeather) {

        Marker currentCityMarker = null;

        if(!cityMarkerMap.containsKey(cityAndWeather.first)) {

            currentCityMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(cityAndWeather.first.latitude, cityAndWeather.first.longitude)));

            Bitmap icon = iconFactory.makeIcon(cityAndWeather.first.name + ", Temp: " + cityAndWeather.second.main.get("temp") + " C");

            currentCityMarker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));

            cityMarkerMap.put(cityAndWeather.first, currentCityMarker);
            reverseCityMarkerMap.put(currentCityMarker, cityAndWeather.first);

            mMap.setOnMarkerClickListener(marker -> {
                TextView text = findViewById(R.id.text);
                City clickedCity = reverseCityMarkerMap.get(marker);
                WeatherInfo weather = weatherController.getCachedInfo(clickedCity);
                text.setText("City: " + clickedCity.name + "\n" + weather.toString());
                return true;
            });

        } else {
            currentCityMarker = cityMarkerMap.get(cityAndWeather.first);
        }

        currentCityMarker.setVisible(true);
    }

}