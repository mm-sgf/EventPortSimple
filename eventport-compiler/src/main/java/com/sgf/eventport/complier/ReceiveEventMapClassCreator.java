package com.sgf.eventport.complier;

import com.sgf.eventport.complier.mapping.EventMapping;
import com.sgf.eventport.complier.mapping.EventMappingManager;
import com.sgf.eventport.complier.utlis.Log;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;


public class ReceiveEventMapClassCreator implements ClassCreator {
    private final static String RECEIVE_EVENT_MAP_PACKAGE = "com.sgf.eventport";
    private final static String RECEIVE_EVENT_MAP_CLASS_NAME = "ReceiveEventMapManger";

    private final static String EVENT_MAPPING_PACKAGE = "com.sgf.eventport";
    private final static String EVENT_MAPPING_INTERFACE_NAME = "EventMapping";

    private final static String SINGLE_EVENT_MAP_FIELD = "singleEventMap";
    private final static String MULTI_EVENT_MAP_FIELD = "multiEventMap";

    private final static String METHOD_PARAMETER_NAME = "key";
    private final static String METHOD_NAME_SINGLE_RECEIVE = "getSingleReceive";
    private final static String METHOD_NAME_MULTI_RECEIVE = "getMultiReceive";

    private final static String METHOD_NAME_INIT_SINGLE_EVENT_MAPPING = "initSingleEventMapping";
    private final static String METHOD_NAME_INIT_MULTI_EVENT_MAPPING = "initMultiEventMapping";

    private final Log mLog;

    private final EventMappingManager mEventMappingManager;

    public ReceiveEventMapClassCreator(EventMapping eventMapping, Log log) {
        this.mLog = log;
        this.mEventMappingManager = new EventMappingManager(eventMapping, log);
    }

    // 检查和整理
    private void checkAndTidyEventMapping() {
        mEventMappingManager.checkAndTidyEventMapping();
    }

    /**
     * 创建Java代码
     */
    public TypeSpec generateJavaCode() {
        checkAndTidyEventMapping();
        ClassName className = ClassName.get(EVENT_MAPPING_PACKAGE, EVENT_MAPPING_INTERFACE_NAME);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(RECEIVE_EVENT_MAP_CLASS_NAME)
                .addSuperinterface(className)
                .addModifiers(Modifier.PUBLIC);

        ParameterizedTypeName singleEventTypeName = ParameterizedTypeName.get(
                ClassName.get(HashMap.class), ClassName.get(String.class), ClassName.get(String.class));

        FieldSpec singleEventMapField = FieldSpec.builder(singleEventTypeName, SINGLE_EVENT_MAP_FIELD)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new HashMap<>()")
                .build();

        classBuilder.addField(singleEventMapField);

        ParameterizedTypeName multiEventList = ParameterizedTypeName.get(
                ClassName.get(List.class), ClassName.get(String.class));

        ParameterizedTypeName multiEventTypeName = ParameterizedTypeName.get(
                ClassName.get(HashMap.class), ClassName.get(String.class), multiEventList);

        FieldSpec multiEventMapField = FieldSpec.builder(multiEventTypeName, MULTI_EVENT_MAP_FIELD)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new HashMap<>()")
                .build();

        classBuilder.addField(multiEventMapField);

        classBuilder.addMethod(createSingleMethod());
        classBuilder.addMethod(createMultiMethod());
        classBuilder.addMethod(createInitSingleMethod());
        classBuilder.addMethod(createInitMultiMethod());

        return classBuilder.build();
    }

    private MethodSpec createSingleMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(METHOD_NAME_SINGLE_RECEIVE);
        methodBuilder.addModifiers(Modifier.PUBLIC);
        methodBuilder.returns(String.class);
        methodBuilder.addParameter(String.class, METHOD_PARAMETER_NAME);
        methodBuilder.beginControlFlow("if ("+ SINGLE_EVENT_MAP_FIELD + ".size() == 0)");
        methodBuilder.addStatement(METHOD_NAME_INIT_SINGLE_EVENT_MAPPING+"()");
        methodBuilder.endControlFlow();
        methodBuilder.addStatement("return " + SINGLE_EVENT_MAP_FIELD + ".get(" + METHOD_PARAMETER_NAME + ")");
        return methodBuilder.build();
    }

    private MethodSpec createMultiMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(METHOD_NAME_MULTI_RECEIVE);
        methodBuilder.addModifiers(Modifier.PUBLIC);

        ParameterizedTypeName returnMultiEventList = ParameterizedTypeName.get(
                ClassName.get(List.class), ClassName.get(String.class));
        methodBuilder.returns(returnMultiEventList);
        methodBuilder.addParameter(String.class, METHOD_PARAMETER_NAME);
        methodBuilder.beginControlFlow("if ("+ MULTI_EVENT_MAP_FIELD + ".size() == 0)");
        methodBuilder.addStatement(METHOD_NAME_INIT_MULTI_EVENT_MAPPING+"()");
        methodBuilder.endControlFlow();
        methodBuilder.addStatement("return " + MULTI_EVENT_MAP_FIELD + ".get(" + METHOD_PARAMETER_NAME + ")");
        return methodBuilder.build();
    }


    private MethodSpec createInitSingleMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(METHOD_NAME_INIT_SINGLE_EVENT_MAPPING);
        methodBuilder.addModifiers(Modifier.PUBLIC);
        for (Map.Entry<String, String> entry : mEventMappingManager.getSingleEventToReceive().entrySet()) {
            methodBuilder.addStatement(SINGLE_EVENT_MAP_FIELD + ".put(\"" +
                    entry.getKey() + "\",\"" + entry.getValue() + "\")");
        }

        return methodBuilder.build();
    }

    @SuppressWarnings("")
    private MethodSpec createInitMultiMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(METHOD_NAME_INIT_MULTI_EVENT_MAPPING);
        methodBuilder.addModifiers(Modifier.PUBLIC);
        AnnotationSpec.Builder uncheckedAnnotationBuilder = AnnotationSpec
                .builder(SuppressWarnings.class)
                .addMember("value", "\"unchecked\"");
        methodBuilder.addAnnotation(uncheckedAnnotationBuilder.build());
        ClassName arrayList = ClassName.get("java.util", "ArrayList");
        ClassName list = ClassName.get("java.util", "List");
        TypeName listOfHoverboards = ParameterizedTypeName.get(list, ClassName.get(String.class));
        int index = 0;
        for (Map.Entry<String, List<String>> entry : mEventMappingManager.getMultiEventToReceive().entrySet()) {
            String eventInterfaceName = entry.getKey();
            List<String> receiveNameList = entry.getValue();
            String eventListFieldName = "eventList" + index;
            index ++;

            methodBuilder.addStatement("$T " + eventListFieldName + " = new $T()",listOfHoverboards, arrayList);
            for (String receiveName : receiveNameList) {
                methodBuilder.addStatement(eventListFieldName + ".add(\"" +
                        receiveName + "\")");
            }

            methodBuilder.addStatement(MULTI_EVENT_MAP_FIELD + ".put(\"" +
                    eventInterfaceName + "\"," + eventListFieldName + ")");
        }

        return methodBuilder.build();
    }



    public String getProxyClassFullName() {
        return RECEIVE_EVENT_MAP_PACKAGE + "." + RECEIVE_EVENT_MAP_CLASS_NAME;
    }

    public String getPackageName() {
        return RECEIVE_EVENT_MAP_PACKAGE;
    }
}