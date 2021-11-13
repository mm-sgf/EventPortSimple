package com.sgf.eventport.complier.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EventMapping {

    private Map<String, ModuleMapping> moduleMappingMap = new HashMap<>();

    public Map<String, ModuleMapping> getModuleMappingMap() {
        return moduleMappingMap;
    }

    public void setModuleMappingMap(Map<String, ModuleMapping> moduleMappingMap) {
        this.moduleMappingMap = moduleMappingMap;
    }

    public ModuleMapping getModuleMapping(String moduleName) {
        ModuleMapping moduleMapping = moduleMappingMap.get(moduleName);

        if (moduleMapping == null) {
            moduleMapping = new ModuleMapping();
            moduleMappingMap.put(moduleName, moduleMapping);
        } else  {
            moduleMapping.clean();
        }
        return moduleMapping;
    }

    public boolean isEmpty() {
        List<String> removeEmptyList = new ArrayList<>();
        for (Map.Entry<String, ModuleMapping> moduleMapping : moduleMappingMap.entrySet()) {
            if (moduleMapping.getValue().isEmpty()) {
                removeEmptyList.add(moduleMapping.getKey());
            }
        }

        for (String key : removeEmptyList) {
            moduleMappingMap.remove(key);
        }

        return moduleMappingMap.isEmpty();
    }
}
