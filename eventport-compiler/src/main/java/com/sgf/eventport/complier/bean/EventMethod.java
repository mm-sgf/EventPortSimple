package com.sgf.eventport.complier.bean;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;


public class EventMethod {

    private final String mMethodName;
    private final List<MethodParam> mParams = new ArrayList<>();
    private final List<TypeMirror> mThrownTypes = new ArrayList<>();
    private TypeMirror mReturnType;

    public EventMethod(String name) {
        this.mMethodName = name;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public void addMethodParam(MethodParam param) {
        this.mParams.add(param);
    }

    public List<MethodParam> getMethodParams() {
        return mParams;
    }

    public void addThrownType(TypeMirror typeMirror) {
        this.mThrownTypes.add(typeMirror);
    }

    public List<TypeMirror> getThrownTypes() {
        return mThrownTypes;
    }

    public boolean isHasReturn() {
        return mReturnType.getKind() != TypeKind.VOID;
    }

    public TypeMirror getReturnType() {
        return mReturnType;
    }

    public void setReturnType(TypeMirror returnType) {
        this.mReturnType = returnType;
    }
}
