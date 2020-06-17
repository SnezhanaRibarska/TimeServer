package com.time.service;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "time")
public class Time {
   
    private String cityName;
    private long timeInMilliseconds;
    private String timeZone;
    private long offsetInMilliseconds;
    
    public Time() {}
    
    public Time(String cityName, String timeZone) {
        this.cityName = cityName;
        this.timeZone = timeZone;
    }
    
    @XmlElement(name = "city")
    public String getCityName() {
        return cityName;
    }
    
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
    
    @XmlElement(name = "timeInMillis")
    public long getTimeInMilliseconds() {
        return timeInMilliseconds;
    }
    
    public void setTimeInMilliseconds(long timeInMilliseconds) {
        this.timeInMilliseconds = timeInMilliseconds;
    }
    
    @XmlElement(name = "timezone")
    public String getTimeZone() {
        return timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    @XmlElement(name = "offset")
    public long getOffset() {
        return offsetInMilliseconds;
    }
    
    public void setOffset(long offset) {
        this.offsetInMilliseconds = offset;
    }
    
    public void addOffset(long offset) {
        this.offsetInMilliseconds += offset;
    }
    
    public void setCurrentTime() {
        this.timeInMilliseconds = System.currentTimeMillis() + getOffset();
    }
}
