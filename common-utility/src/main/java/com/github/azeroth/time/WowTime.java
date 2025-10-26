package com.github.azeroth.time;

import com.github.azeroth.common.Assert;
import lombok.Data;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Data
public class WowTime {
    private int year = -1;
    private byte month = -1;
    private byte monthDay = -1;
    private byte weekDay = -1;
    private byte hour = -1;
    private byte minute = -1;
    private byte flags = -1;
    private byte holidayOffset = 0;

    public void setDateTime(Instant instant, ZoneId zoneId) {
        ZonedDateTime dateTime = instant.atZone(zoneId);
        this.year = dateTime.getYear() % 100; // remain only last 2 digits
        this.month = (byte) (dateTime.getMonthValue() - 1); // begin from 0
        this.monthDay = (byte) dateTime.getDayOfMonth();
        this.weekDay = (byte) dateTime.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        this.hour = (byte) dateTime.getHour();
        this.minute = (byte) dateTime.getMinute();
    }



    public int getPackedTime() {
        return ((year % 100) & 0x1F) << 24 | (month & 0xF) << 20 | (monthDay & 0x3F) << 14 | (weekDay & 0x7) << 11 | (hour & 0x1F) << 6 | (minute & 0x3F) | (flags & 0x3) << 29;
    }

    public void setPackedTime(int packedTime) {
        year = (packedTime >>> 24) & 0x1F;
        if (year == 31) {
            year = -1;
        }

        month = (byte) ((packedTime >>> 20) & 0xF);
        if (month == 15) {
            month = -1;
        }

        monthDay = (byte) ((packedTime >>> 14) & 0x3F);
        if (monthDay == 63) {
            monthDay = -1;
        }

        weekDay = (byte) ((packedTime >>> 11) & 0x7);
        if (weekDay == 7) {
            weekDay = -1;
        }

        hour = (byte) ((packedTime >>> 6) & 0x1F);
        if (hour == 31) {
            hour = -1;
        }

        minute = (byte) (packedTime & 0x3F);
        if (minute == 63) {
            minute = -1;
        }

        flags = (byte) ((packedTime >>> 29) & 0x3);
        if (flags == 3) {
            flags = -1;
        }
    }


    public void setYear(int year) {
        Assert.isTrue(year == -1 || (year >= 0 && year < 32));
        this.year = year;
    }

    public void setMonth(byte month) {
        Assert.isTrue(month == -1 || (month >= 0 && month < 12));
        this.month = month;
    }

    public void setMonthDay(byte monthDay) {
        Assert.isTrue(monthDay == -1 || (monthDay >= 0 && monthDay < 32));
        this.monthDay = monthDay;
    }

    public void setWeekDay(byte weekDay) {
        Assert.isTrue(weekDay == -1 || (weekDay >= 0 && weekDay < 7));
        this.weekDay = weekDay;
    }

    public void setHour(byte hour) {
        Assert.isTrue(hour == -1 || (hour >= 0 && hour < 24));
        this.hour = hour;
    }

    public void setMinute(byte minute) {
        Assert.isTrue(minute == -1 || (minute >= 0 && minute < 60));
        this.minute = minute;
    }

    public void setFlags(byte flags) {
        Assert.isTrue(flags == -1 || (flags >= 0 && flags < 3));
        this.flags = flags;
    }


}

