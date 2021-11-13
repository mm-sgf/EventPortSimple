package com.sgf.eventport;

import java.util.List;

public interface EventMapping {
    String getSingleReceive(String key);
    List<String> getMultiReceive(String key);
}
