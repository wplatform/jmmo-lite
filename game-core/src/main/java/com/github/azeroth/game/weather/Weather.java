package com.github.azeroth.game.weather;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.listener.interfaces.iweather.IWeatherOnChange;
import com.github.azeroth.game.listener.interfaces.iweather.IWeatherOnUpdate;

public class Weather {
    private final int zone;
    private final intervalTimer timer = new intervalTimer();
    private final WeatherData weatherChances;
    private WeatherType type = WeatherType.values()[0];
    private float intensity;

    public Weather(int zoneId, WeatherData weatherChances) {
        zone = zoneId;
        weatherChances = weatherChances;
        timer.Interval = 10 * time.Minute * time.InMilliseconds;
        type = WeatherType.Fine;
        intensity = 0;

        //Log.outInfo(LogFilter.General, "WORLD: Starting weather system for zone {0} (change every {1} minutes).", m_zone, (m_timer.GetInterval() / (time.Minute * time.InMilliseconds)));
    }

    public static void sendFineWeatherUpdateToPlayer(Player player) {
        player.sendPacket(new WeatherPkt(WeatherState.Fine));
    }

    public final int getZone() {
        return zone;
    }

    public final int getScriptId() {
        return weatherChances.scriptId;
    }

    public final boolean update(int diff) {
        if (timer.Current >= 0) {
            timer.update(diff);
        } else {
            timer.Current = 0;
        }

        // If the timer has passed, ReGenerate the weather
        if (timer.Passed) {
            timer.reset();

            // update only if Regenerate has changed the weather
            if (reGenerate()) {
                // Weather will be removed if not updated (no players in zone anymore)
                if (!updateWeather()) {
                    return false;
                }
            }
        }

        global.getScriptMgr().<IWeatherOnUpdate>RunScript(p -> p.onUpdate(this, diff), getScriptId());

        return true;
    }

    public final boolean reGenerate() {
        if (weatherChances == null) {
            type = WeatherType.Fine;
            intensity = 0.0f;

            return false;
        }

        // Weather statistics:
        // 30% - no change
        // 30% - weather gets better (if not fine) or change weather type
        // 30% - weather worsens (if not fine)
        // 10% - radical change (if not fine)
        var u = RandomUtil.URand(0, 99);

        if (u < 30) {
            return false;
        }

        // remember old values
        var old_type = type;
        var old_intensity = intensity;

        var gtime = GameTime.getGameTime();
        var ltime = time.UnixTimeToDateTime(gtime).ToLocalTime();
        var season = (int) ((ltime.getDayOfYear() - 78 + 365) / 91) % 4;

        String[] seasonName = {"spring", "summer", "fall", "winter"};

        Log.outTrace(LogFilter.Server, "Generating a change in {0} weather for zone {1}.", seasonName[season], zone);

        if ((u < 60) && (intensity < 0.33333334f)) // Get fair
        {
            type = WeatherType.Fine;
            intensity = 0.0f;
        }

        if ((u < 60) && (type != WeatherType.Fine)) // Get better
        {
            _intensity -= 0.33333334f;

            return true;
        }

        if ((u < 90) && (type != WeatherType.Fine)) // Get worse
        {
            intensity += 0.33333334f;

            return true;
        }

        if (type != WeatherType.Fine) {
            // Radical change:
            // if light . heavy
            // if medium . change weather type
            // if heavy . 50% light, 50% change weather type

            if (intensity < 0.33333334f) {
                intensity = 0.9999f; // go nuts

                return true;
            } else {
                if (intensity > 0.6666667f) {
                    // Severe change, but how severe?
                    var rnd = RandomUtil.URand(0, 99);

                    if (rnd < 50) {
                        _intensity -= 0.6666667f;

                        return true;
                    }
                }

                type = WeatherType.Fine; // clear up
                intensity = 0;
            }
        }

        // At this point, only weather that isn't doing anything remains but that have weather data
        var chance1 = weatherChances.Data[season].rainChance;
        var chance2 = chance1 + weatherChances.Data[season].snowChance;
        var chance3 = chance2 + weatherChances.Data[season].stormChance;
        var rn = RandomUtil.URand(1, 100);

        if (rn <= chance1) {
            type = WeatherType.Rain;
        } else if (rn <= chance2) {
            type = WeatherType.Snow;
        } else if (rn <= chance3) {
            type = WeatherType.Storm;
        } else {
            type = WeatherType.Fine;
        }

        // New weather statistics (if not fine):
        // 85% light
        // 7% medium
        // 7% heavy
        // If fine 100% sun (no fog)

        if (type == WeatherType.Fine) {
            intensity = 0.0f;
        } else if (u < 90) {
            intensity = (float) RandomUtil.randomFloat() * 0.3333f;
        } else {
            // Severe change, but how severe?
            rn = RandomUtil.URand(0, 99);

            if (rn < 50) {
                intensity = (float) RandomUtil.randomFloat() * 0.3333f + 0.3334f;
            } else {
                intensity = (float) RandomUtil.randomFloat() * 0.3333f + 0.6667f;
            }
        }

        // return true only in case weather changes
        return type != old_type || intensity != old_intensity;
    }

    public final void sendWeatherUpdateToPlayer(Player player) {
        WeatherPkt weather = new WeatherPkt(getWeatherState(), intensity);
        player.sendPacket(weather);
    }

    public final boolean updateWeather() {
        var player = global.getWorldMgr().findPlayerInZone(zone);

        if (player == null) {
            return false;
        }

        // Send the weather packet to all players in this zone
        if (intensity >= 1) {
            intensity = 0.9999f;
        } else if (intensity < 0) {
            intensity = 0.0001f;
        }

        var state = getWeatherState();

        WeatherPkt weather = new WeatherPkt(state, intensity);

        //- Returns false if there were no players found to update
        if (!global.getWorldMgr().sendZoneMessage(zone, weather)) {
            return false;
        }

        // Log the event
        String wthstr;

        switch (state) {
            case Fog:
                wthstr = "fog";

                break;
            case LightRain:
                wthstr = "light rain";

                break;
            case MediumRain:
                wthstr = "medium rain";

                break;
            case HeavyRain:
                wthstr = "heavy rain";

                break;
            case LightSnow:
                wthstr = "light snow";

                break;
            case MediumSnow:
                wthstr = "medium snow";

                break;
            case HeavySnow:
                wthstr = "heavy snow";

                break;
            case LightSandstorm:
                wthstr = "light sandstorm";

                break;
            case MediumSandstorm:
                wthstr = "medium sandstorm";

                break;
            case HeavySandstorm:
                wthstr = "heavy sandstorm";

                break;
            case Thunders:
                wthstr = "thunders";

                break;
            case BlackRain:
                wthstr = "blackrain";

                break;
            case Fine:
            default:
                wthstr = "fine";

                break;
        }

        Log.outDebug(LogFilter.Server, "Change the weather of zone {0} to {1}.", zone, wthstr);

        global.getScriptMgr().<IWeatherOnChange>RunScript(p -> p.OnChange(this, state, intensity), getScriptId());

        return true;
    }

    public final void setWeather(WeatherType type, float grade) {
        if (type == type && intensity == grade) {
            return;
        }

        type = type;
        intensity = grade;
        updateWeather();
    }

    public final WeatherState getWeatherState() {
        if (intensity < 0.27f) {
            return WeatherState.Fine;
        }

        switch (type) {
            case Rain:
                if (intensity < 0.40f) {
                    return WeatherState.LightRain;
                } else if (intensity < 0.70f) {
                    return WeatherState.MediumRain;
                } else {
                    return WeatherState.HeavyRain;
                }
            case Snow:
                if (intensity < 0.40f) {
                    return WeatherState.LightSnow;
                } else if (intensity < 0.70f) {
                    return WeatherState.MediumSnow;
                } else {
                    return WeatherState.HeavySnow;
                }
            case Storm:
                if (intensity < 0.40f) {
                    return WeatherState.LightSandstorm;
                } else if (intensity < 0.70f) {
                    return WeatherState.MediumSandstorm;
                } else {
                    return WeatherState.HeavySandstorm;
                }
            case BlackRain:
                return WeatherState.BlackRain;
            case Thunders:
                return WeatherState.Thunders;
            case Fine:
            default:
                return WeatherState.Fine;
        }
    }
}
