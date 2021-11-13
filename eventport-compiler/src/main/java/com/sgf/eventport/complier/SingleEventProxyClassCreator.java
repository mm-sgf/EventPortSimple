package com.sgf.eventport.complier;

import com.sgf.eventport.complier.bean.EventMethod;
import com.sgf.eventport.complier.bean.MethodParam;
import com.sgf.eventport.complier.utlis.Log;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;


public class SingleEventProxyClassCreator implements EventClassCreator {
    private final static String EVENT_MANAGE_PACKAGE = "com.sgf.eventport";
    private final static String EVENT_MANAGE_NAME = "EventManager";
    private final static String EVENT_KEY = "mEventKey";
    private final static String EVENT_MANAGE_FIELD_NAME = "eventHandler";

    private final String mEventProxyClassName;
    private final String mPackageName;
    private final TypeElement mEventInterface;

    private final List<EventMethod> mMethodMapping = new ArrayList<>();

    public SingleEventProxyClassCreator(Elements elementUtils, TypeElement classElement, Log log) {
        PackageElement packageElement = elementUtils.getPackageOf(classElement);
        String packageName = packageElement.getQualifiedName().toString();
        String className = classElement.getSimpleName().toString();
        this.mPackageName = packageName;
        this.mEventInterface = classElement;
        this.mEventProxyClassName = className + "_proxy";
    }

    public void putEventMethod(EventMethod eventMethod) {
        mMethodMapping.add(eventMethod);
    }


    /**
     * 创建Java代码
     */
    public TypeSpec generateJavaCode() {
        ClassName className = ClassName.get(mPackageName, mEventInterface.getSimpleName().toString());
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(mEventProxyClassName)
//                .superclass(className)
                .addSuperinterface(className)
                .addModifiers(Modifier.PUBLIC);

        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(ClassName.get(mPackageName, mEventInterface.getSimpleName().toString())));

        FieldSpec eventKeyField = FieldSpec.builder(typeName, EVENT_KEY)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer(className + ".class")
                .build();

        classBuilder.addField(eventKeyField);

        for (EventMethod method : mMethodMapping) {
            classBuilder.addMethod(createMethod(method, classBuilder, mEventInterface.getSimpleName().toString()));
        }

        return classBuilder.build();
    }

    private MethodSpec createMethod(EventMethod eventMethod,TypeSpec.Builder classBuilder, String className) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(eventMethod.getMethodName());
        methodBuilder.addModifiers(Modifier.PUBLIC);
        TypeName returnType = ClassName.get(eventMethod.getReturnType());
        methodBuilder.returns(returnType);

        if (eventMethod.isHasReturn()) {
            FieldSpec eventKeyField = FieldSpec.builder(returnType, eventMethod.getMethodName())
                    .addModifiers(Modifier.PRIVATE)
                    .build();

            classBuilder.addField(eventKeyField);
        }

        StringBuilder eventObj = new StringBuilder();
        eventObj.append(className).append(" ").append(EVENT_MANAGE_FIELD_NAME);
        eventObj.append(" = $T.Companion.getInstance().findEventInterface(").append(EVENT_KEY).append(")");
        ClassName eventManage = ClassName.get(EVENT_MANAGE_PACKAGE, EVENT_MANAGE_NAME);
        methodBuilder.addStatement(eventObj.toString(), eventManage);

        StringBuilder eventManagerMethod = new StringBuilder();
        if (eventMethod.isHasReturn()) {
            eventManagerMethod.append("return ");
        }

        eventManagerMethod.append(EVENT_MANAGE_FIELD_NAME).append(".").append(eventMethod.getMethodName()).append("(");
        // 设置异常
        List<TypeMirror> thrownTypes = eventMethod.getThrownTypes();
        for (TypeMirror TypeMirror : thrownTypes) {
            List<TypeName> thrownTypeTypeNames = new ArrayList<>();
            thrownTypeTypeNames.add(TypeName.get(TypeMirror));
            methodBuilder.addExceptions(thrownTypeTypeNames);
        }

        // 设置参数
        List<MethodParam> methodParams = eventMethod.getMethodParams();
        for (MethodParam param : methodParams) {
            List<ParameterSpec> eventParams = new ArrayList<>();
            TypeName typeName = TypeName.get(param.getParamType());
            ParameterSpec parameterSpec = ParameterSpec.builder(typeName, param.getParamName()).build();

            eventParams.add(parameterSpec);
            methodBuilder.addParameters(eventParams);
            eventManagerMethod.append(param.getParamName()).append(",");
        }
        if (methodParams.size() > 0) {
            eventManagerMethod.delete(eventManagerMethod.length() - 1, eventManagerMethod.length());
        }
        eventManagerMethod.append(")");
        methodBuilder.beginControlFlow("if ("+ EVENT_MANAGE_FIELD_NAME + " != null)");
        methodBuilder.addStatement(eventManagerMethod.toString());
        methodBuilder.endControlFlow();
        if (eventMethod.isHasReturn()) {
            methodBuilder.addStatement("return " + eventMethod.getMethodName());
        }
        return methodBuilder.build();
    }

    public String getProxyClassFullName() {
        return mPackageName + "." + mEventProxyClassName;
    }

    public String getPackageName() {
        return mPackageName;
    }
}