package com.github.azeroth.game.map;

import com.github.azeroth.game.domain.weather.WeatherState;
import com.github.azeroth.game.weather.Weather;

import java.util.ArrayList;


public class ZoneDynamicInfo {
    public int musicId;
    public Weather defaultWeather;
    public WeatherState weatherId = WeatherState.values()[0];
    public float intensity;
    public ArrayList<LightOverride> lightOverrides = new ArrayList<>();

    public final static class LightOverride {
        public int areaLightId;
        public int overrideLightId;
        public int transitionMilliseconds;

    }
}
