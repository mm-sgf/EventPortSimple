package com.sgf.eventport.complier.mapping;


import com.sgf.eventport.complier.utlis.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EventMappingManager {

    private final EventMapping mEventMapping;

    private final Set<String> mSingleEventSet = new HashSet<>();
    private final Set<String> mMultiEventSet = new HashSet<>();

    private final Map<String, String> singleEventToReceive = new HashMap<>();
    private final Map<String, List<String>> multiEventToReceive = new HashMap<>();

    private final Log mLog;

    public EventMappingManager(EventMapping eventMapping, Log log) {
        this.mEventMapping = eventMapping;
        this.mLog = log;
    }

    public void checkAndTidyEventMapping() {
        for (ModuleMapping moduleMapping : mEventMapping.getModuleMappingMap().values()) {
            // add single event
            mSingleEventSet.addAll(moduleMapping.getSingleEvents());
            // add multi event
            mMultiEventSet.addAll(moduleMapping.getMultiEvents());
        }

        for (ModuleMapping moduleMapping : mEventMapping.getModuleMappingMap().values()) {
            Map<String, List<String>> receiveMap = moduleMapping.getReceiveMap();
            for (Map.Entry<String, List<String>> entry : receiveMap.entrySet()) {
                String receiveName = entry.getKey();
                for (String interfaceName : entry.getValue()) {
                    if (mSingleEventSet.contains(interfaceName)) {
                        if (singleEventToReceive.containsKey(interfaceName)) {
                            mLog.logE("single event interface:" + interfaceName +
                                    " don`t has multi receive : \n" +
                                    "receive class :" + receiveName  +
                                    "\n  receive class :" + singleEventToReceive.get(interfaceName));
                        } else {
                            mLog.logI("single event :" + interfaceName  + "  r:" + receiveName);
                            singleEventToReceive.put(interfaceName, receiveName);
                        }
                    } else if (mMultiEventSet.contains(interfaceName)) {
                        List<String> receiveList = multiEventToReceive.get(interfaceName);
                        if (receiveList == null) {
                            receiveList = new ArrayList<>();
                            multiEventToReceive.put(interfaceName, receiveList);
                        }
                        mLog.logI("multi event interfaceName:" + interfaceName  +
                                "  receiveName:" + receiveName);

                        if (!receiveList.contains(receiveName)) {
                            receiveList.add(receiveName);
                        }
                    }
                }
            }

        }
    }

    public Map<String, String> getSingleEventToReceive() {
        return singleEventToReceive;
    }

    public Map<String, List<String>> getMultiEventToReceive() {
        return multiEventToReceive;
    }
}
