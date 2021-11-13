package com.sgf.eventport.complier;

import com.squareup.javapoet.TypeSpec;

public interface ClassCreator {
    TypeSpec generateJavaCode();

    String getProxyClassFullName();

    String getPackageName();
}
