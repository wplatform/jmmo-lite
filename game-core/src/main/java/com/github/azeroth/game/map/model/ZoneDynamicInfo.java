package com.github.azeroth.game.map.model;

import com.github.azeroth.game.domain.weather.WeatherState;
import com.github.azeroth.game.weather.Weather;


import lombok.Data;

import java.util.List;

@Data
public class ZoneDynamicInfo {


    private int musicId;

    private Weather defaultWeather;
    private WeatherState weatherId;
    private float Intensity;
    private List<LightOverride> LightOverrides;

    ;

    record LightOverride(int areaLightId, int overrideLightId, int transitionMilliseconds) {

    }
}
