package com.time.service.internal;

import java.util.Collection;
import java.util.Map;

import com.time.service.Time;

public interface DataHandler {

	public Map<String, Time> readInputData();
    
    public void writeData(Collection<Time> allTimes);
}
