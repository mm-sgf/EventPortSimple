package com.sgf.eventport.complier.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModuleMapping {

    private Set<String> singleEvents = new HashSet<>();
    private Set<String> multiEvents = new HashSet<>();

    private Map<String, List<String>> receiveMap = new HashMap<>();

    public Map<String, List<String>> getReceiveMap() {
        return receiveMap;
    }

    public void setReceiveMap(Map<String, List<String>> receiveMap) {
        this.receiveMap = receiveMap;
    }

    public Set<String> getSingleEvents() {
        return singleEvents;
    }

    public void setSingleEvents(Set<String> singleEvents) {
        this.singleEvents = singleEvents;
    }

    public void  addSingleEvent(String singleEvent) {
        this.singleEvents.add(singleEvent);
    }

    public Set<String> getMultiEvents() {
        return multiEvents;
    }

    public void setMultiEvents(Set<String> multiEvents) {
        this.multiEvents = multiEvents;
    }

    public void putReceiveMapping(String receiveClass , String interfaceClass) {
        List<String> interfaceList = receiveMap.get(receiveClass);

        if (interfaceList == null) {
            interfaceList = new ArrayList<>();
        }

        interfaceList.add(interfaceClass);

        receiveMap.put(receiveClass,interfaceList);
    }

    public void addMultiEvent(String multiEvent) {
        this.multiEvents.add(multiEvent);
    }

    public void clean() {
        singleEvents.clear();
        multiEvents.clear();
        receiveMap.clear();
    }

    public boolean isEmpty() {
        return singleEvents.isEmpty() && multiEvents.isEmpty() && receiveMap.isEmpty();
    }
}
