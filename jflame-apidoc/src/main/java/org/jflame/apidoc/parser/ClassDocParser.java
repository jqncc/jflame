package org.jflame.apidoc.parser;

import java.util.List;

import org.jflame.apidoc.model.ApiMethod;

import com.sun.javadoc.ClassDoc;

public interface ClassDocParser {

    /**
     * 解析一个class文件中的接口文档描述
     * 
     * @param cls ClassDoc,类的文档描述
     * @return
     */
    List<ApiMethod> parse(ClassDoc cls);
}
