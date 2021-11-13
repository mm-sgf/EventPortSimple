package com.sgf.eventport.complier;


import com.sgf.eventport.complier.bean.EventMethod;
import com.sgf.eventport.complier.bean.MethodParam;
import com.sgf.eventport.complier.mapping.EventMapping;
import com.sgf.eventport.complier.mapping.ModuleMapping;
import com.sgf.eventport.complier.utlis.FileUtils;
import com.sgf.eventport.complier.utlis.Log;
import com.sgf.eventport.annotation.MultiEvent;
import com.sgf.eventport.annotation.ReceiveEvent;
import com.sgf.eventport.annotation.SingleEvent;
import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.squareup.javapoet.JavaFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class EventAptProcessor extends AbstractProcessor implements Log {

    private static final String RECEIVE_EVENT_MAP_CREATOR_KEY = "receive_event_map_creator_key";
    private static final String PROJECT_ROOT_PATH = System.getProperty("user.dir");
    private static final String MAPPING_CACHE_FILE =PROJECT_ROOT_PATH + File.separator +"event_port.cache";

    private Elements mElementUtils;
    private Types mTypeUtils;
    private final Map<String, ClassCreator> mEventProxyMap = new HashMap<>();

    private final Gson mGson = new Gson();

    private EventMapping mEventMapping;
    private ModuleMapping mModuleMapping;
    private boolean isNeedCreateEventMapping = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mElementUtils = processingEnv.getElementUtils();
        mTypeUtils = processingEnv.getTypeUtils();

        String createEventMappingFileFlag = processingEnv.getOptions().get("EventPort");
        isNeedCreateEventMapping = createEventMappingFileFlag != null;
        logI("init method createEventMappingFileFlag:"  + createEventMappingFileFlag);

        try {
            String eventMappingString = FileUtils.readForFile(new File(MAPPING_CACHE_FILE));
            if (eventMappingString != null) {
                mEventMapping = mGson.fromJson(eventMappingString, EventMapping.class);
            } else  {
                mEventMapping = new EventMapping();
            }
            logI("init method get event mapping file :"  + eventMappingString);
        } catch (Exception e) {
            logE("read cache file fail ");
            e.printStackTrace();
        }

        String moduleBuildPath = processingEnv.getOptions().get("kapt.kotlin.generated");
        String moduleName =  moduleBuildPath.replace(PROJECT_ROOT_PATH, "").split(File.separator)[1];
        mModuleMapping = mEventMapping.getModuleMapping(moduleName);
        logI("init method====>"  + System.getProperty("user.dir")  + "   moduleName :" + moduleName);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(MultiEvent.class.getCanonicalName());
        types.add(SingleEvent.class.getCanonicalName());
        types.add(ReceiveEvent.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mEventProxyMap.clear();
        //得到所有的注解
        Set<? extends Element> singleEventElements = roundEnvironment.getElementsAnnotatedWith(SingleEvent.class);
        for (Element element : singleEventElements) {
            if (element instanceof  TypeElement) {
                TypeElement eventTypeElement = (TypeElement) element;

                // 获取全名
                String singleEventName = eventTypeElement.getQualifiedName().toString();
                logI("single event name:" + singleEventName);
                mModuleMapping.addSingleEvent(singleEventName);
                EventClassCreator classCreator = (EventClassCreator) mEventProxyMap.get(singleEventName);
                if (classCreator == null) {
                    classCreator = new SingleEventProxyClassCreator(mElementUtils, eventTypeElement,this);
                    mEventProxyMap.put(singleEventName, classCreator);
                }
                eventTypeElementPasser(eventTypeElement, classCreator);
            }
        }


        //得到所有的注解
        Set<? extends Element> multiEventElements = roundEnvironment.getElementsAnnotatedWith(MultiEvent.class);
        for (Element element : multiEventElements) {
            if (element instanceof TypeElement) {
                TypeElement eventTypeElement = (TypeElement) element;

                // 获取全名
                String multiEventName = eventTypeElement.getQualifiedName().toString();
                logI("multi event name:" + multiEventName);
                mModuleMapping.addMultiEvent(multiEventName);
                EventClassCreator classCreator = (EventClassCreator) mEventProxyMap.get(multiEventName);
                if (classCreator == null) {
                    classCreator = new MultiEventProxyClassCreator(mElementUtils, eventTypeElement,this);
                    mEventProxyMap.put(multiEventName, classCreator);
                }
                eventTypeElementPasser(eventTypeElement, classCreator);
            }
        }

        ReceiveEventMapClassCreator receiveEventMapCreator = (ReceiveEventMapClassCreator) mEventProxyMap.get(RECEIVE_EVENT_MAP_CREATOR_KEY);
        if (receiveEventMapCreator == null) {
            receiveEventMapCreator = new ReceiveEventMapClassCreator(mEventMapping,this);
        }

        if (isNeedCreateEventMapping) {
            mEventProxyMap.put(RECEIVE_EVENT_MAP_CREATOR_KEY, receiveEventMapCreator);
        }


        Set<? extends Element> receiveEventElements = roundEnvironment.getElementsAnnotatedWith(ReceiveEvent.class);
        for (Element element : receiveEventElements) {
            if (element instanceof TypeElement) {
                receiveEventTypeElementPasser((TypeElement) element, mModuleMapping);
            }
        }

        try {
            String eventMappingStr = mGson.toJson(mEventMapping);
            FileUtils.writeInFile(new File(MAPPING_CACHE_FILE), eventMappingStr);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (mEventMapping.isEmpty()) {
            logI("event mapping is empty");
            mEventProxyMap.remove(RECEIVE_EVENT_MAP_CREATOR_KEY);
        }

        //通过遍历mProxyMap，创建java文件
        for (String key : mEventProxyMap.keySet()) {
            ClassCreator proxyInfo = mEventProxyMap.get(key);
            JavaFile javaFile = JavaFile.builder(proxyInfo.getPackageName(), proxyInfo.generateJavaCode()).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                logI(" --> create " + proxyInfo.getProxyClassFullName() + "error");
            }
        }

        logI("process finish ...");

        return true;
    }

    private void receiveEventTypeElementPasser(
            TypeElement superTypeElement,
            ModuleMapping moduleMapping) {

        String receiveClassName = superTypeElement.getQualifiedName().toString();
        logI("receive class name:" + receiveClassName);
        List<? extends TypeMirror>  interfaceTypeMirrorList =  superTypeElement.getInterfaces();

        for (TypeMirror interfaceType : interfaceTypeMirrorList) {
            String interfaceName = ((TypeElement)mTypeUtils.asElement(interfaceType)).getQualifiedName().toString();
            logI("==>获取 Receive Class 的接口 ：" + interfaceName);
            if (!(interfaceName.startsWith("android") || interfaceName.startsWith("androidx"))) {
                moduleMapping.putReceiveMapping(receiveClassName, interfaceName);
            }
        }

        TypeMirror  superTypeMirror = superTypeElement.getSuperclass();

        if (receiveClassName.equals("java.lang.Object")) {
            return;
        }

        if (superTypeMirror != null) {
            receiveEventTypeElementPasser(
                    receiveClassName,
                    (TypeElement) mTypeUtils.asElement(superTypeMirror),
                    moduleMapping);
        }
    }

    private void receiveEventTypeElementPasser(
            String receiveClassName ,
            TypeElement superTypeElement,
            ModuleMapping moduleMapping) {
        String receiveSupperClassName = superTypeElement.getQualifiedName().toString();
        logI("receive supper class name:" + receiveSupperClassName);
        List<? extends TypeMirror>  interfaceTypeMirrorList =  superTypeElement.getInterfaces();

        for (TypeMirror interfaceType : interfaceTypeMirrorList) {
            String interfaceName = ((TypeElement)mTypeUtils.asElement(interfaceType)).getQualifiedName().toString();
            logI("==>获取 Receive Class 的接口 ：" + interfaceName);
            if (!(interfaceName.startsWith("android") || interfaceName.startsWith("androidx"))) {
                moduleMapping.putReceiveMapping(receiveSupperClassName, interfaceName);
            }
        }

        TypeMirror  superTypeMirror = superTypeElement.getSuperclass();

        if (receiveSupperClassName.equals("java.lang.Object") || receiveClassName.equals(receiveSupperClassName)) {
            return;
        }

        if (superTypeMirror != null) {
            receiveEventTypeElementPasser(
                    receiveClassName,
                    (TypeElement) mTypeUtils.asElement(superTypeMirror),
                    moduleMapping);
        }
    }

    private void eventTypeElementPasser(TypeElement eventTypeElement, EventClassCreator classCreator) {
        // 判断是不是接口类型
        if (eventTypeElement.getKind() != ElementKind.INTERFACE) {
            logE("Event annotation must use interface : \n" + eventTypeElement.getQualifiedName());
        }

        // 获取所有的方法
        List<? extends Element>  methodElements = eventTypeElement.getEnclosedElements();
        // 遍历接口中的所有方法名
        for(Element element : methodElements) {
            ExecutableElement methodElement = (ExecutableElement) element;
            logI("方法名 ===>" + methodElement.getSimpleName());
            EventMethod eventMethod = new EventMethod(methodElement.getSimpleName().toString());
            classCreator.putEventMethod(eventMethod);

            // 获取返回值
            TypeMirror returnType =  methodElement.getReturnType();
            eventMethod.setReturnType(returnType);
            logI("返回值类型 :" + returnType);
            // 方法异常
            List<? extends TypeMirror> thrownTypes = methodElement.getThrownTypes();
            for (TypeMirror thrownType: thrownTypes) {
                eventMethod.addThrownType(thrownType);
                TypeElement ttElement = (TypeElement) mTypeUtils.asElement(thrownType);
                logI("方法向外抛的异常 ===>: " + ttElement.getQualifiedName());
            }

            List<? extends VariableElement> methodParameterElements = methodElement.getParameters();
            for (VariableElement methodParameterElement: methodParameterElements) {
                logI("方法参数 :" + methodParameterElement  + " 方法参数类型： " + methodParameterElement.asType() );

                try {
                    MethodParam methodParam = new MethodParam(
                            methodParameterElement.toString(), methodParameterElement.asType());
                    eventMethod.addMethodParam(methodParam);
                } catch (Exception e) {
                    logE(e.toString());
                }
            }
        }
    }

    @Override
    public void logI(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
    }

    @Override
    public void logE(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

}
