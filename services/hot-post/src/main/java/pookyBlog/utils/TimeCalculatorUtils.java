package pookyBlog.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeCalculatorUtils {
    public static Duration calculatorDurationToMidnight(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT);
        return Duration.between(now, midnight);
    }
}
