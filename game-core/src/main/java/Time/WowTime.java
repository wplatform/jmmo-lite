package Time;

import com.github.azeroth.common.Assert;
import lombok.Data;

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


    public int GetPackedTime() {
        return ((year % 100) & 0x1F) << 24 | (month & 0xF) << 20 | (monthDay & 0x3F) << 14 | (weekDay & 0x7) << 11 | (hour & 0x1F) << 6 | (minute & 0x3F) | (flags & 0x3) << 29;
    }

    public void SetPackedTime(int packedTime) {
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


    public void SetYear(int year) {
        Assert.state(year == -1 || (year >= 0 && year < 32));
        this.year = year;
    }

    public void SetMonth(byte month) {
        Assert.state(month == -1 || (month >= 0 && month < 12));
        this.month = month;
    }

    public void SetMonthDay(byte monthDay) {
        Assert.state(monthDay == -1 || (monthDay >= 0 && monthDay < 32));
        this.monthDay = monthDay;
    }

    public void SetWeekDay(byte weekDay) {
        Assert.state(weekDay == -1 || (weekDay >= 0 && weekDay < 7));
        this.weekDay = weekDay;
    }

    public void SetHour(byte hour) {
        Assert.state(hour == -1 || (hour >= 0 && hour < 24));
        this.hour = hour;
    }

    public void SetMinute(byte minute) {
        Assert.state(minute == -1 || (minute >= 0 && minute < 60));
        this.minute = minute;
    }

    public void SetFlags(byte flags) {
        Assert.state(flags == -1 || (flags >= 0 && flags < 3));
        this.flags = flags;
    }


}

