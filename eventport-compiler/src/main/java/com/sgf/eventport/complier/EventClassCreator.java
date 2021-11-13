package com.sgf.eventport.complier;

import com.sgf.eventport.complier.bean.EventMethod;

public interface EventClassCreator extends ClassCreator {

    void putEventMethod(EventMethod eventMethod);
}
