package com.sgf.eventport.complier.bean;
import javax.lang.model.type.TypeMirror;

public class MethodParam {

    private final String mParamName;
    private final TypeMirror mParamType;

    public MethodParam(String paramName, TypeMirror paramType) {
        this.mParamName = paramName;
        this.mParamType = paramType;
    }

    public String getParamName() {
        return  mParamName;
    }

    public TypeMirror getParamType() {
        return mParamType;
    }
}
