package org.jflame.apidoc.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jflame.apidoc.enums.HttpMethod;
import org.jflame.apidoc.enums.ParamPos;
import org.jflame.apidoc.model.ApiMethod;
import org.jflame.apidoc.model.ApiModule;
import org.jflame.apidoc.model.ApiParam;
import org.jflame.apidoc.util.ArrayUtils;
import org.jflame.apidoc.util.JavadocUtils;
import org.jflame.apidoc.util.StringUtils;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;

/**
 * springmvc框架api文档解析器
 * 
 * @author yucan.zhang
 */
public class SpringMvcDocParser implements ClassDocParser {

    private final static String[] annot_controls = { "Controller","RestController" };// contoller注解
    private final static String[] annot_mappings = { "RequestMapping","GetMapping","PostMapping" };// mapping注解
    private final static String[] annot_parameters = { "RequestParam","RequestHeader","PathVariable" };// 参数注解
    private final static String[] annot_mapping_attrs = { "value","path","method" };// mapping注解的属性
    private final static String[] annot_parameter_attrs = { "value","name","defaultValue","required" };// 参数注解属性
    private final static String annot_resp = "ResponseBody";// @ResponseBody注解

    @Override
    public List<ApiMethod> parse(ClassDoc clsDoc) {
        if (isApiClass(clsDoc) || JavadocUtils.hasAnyAnnotation(clsDoc, annot_deprecated)) {
            return null;
        }
        /* 提取类上的模块,作者,版本号等信息 */
        Optional<String> author = JavadocUtils.getAuthor(clsDoc);
        Optional<String> version = JavadocUtils.getVersion(clsDoc);
        String moduleName = JavadocUtils.getModuleName(clsDoc).orElse(ApiModule.DEFAULT_MODULE);
        String baseUrl = null;

        // 提取类上的RequestMapping
        Optional<AnnotationDesc> classMappingAnnot = JavadocUtils.getAnnotation(clsDoc, annot_mappings[0]);
        boolean hasClassMappingAnnot = classMappingAnnot.isPresent();
        if (hasClassMappingAnnot) {
            Set<String> baseUrls = extractApiUrl(classMappingAnnot.get());
            if (!baseUrls.isEmpty()) {
                baseUrl = baseUrls.iterator().next();
            }
        }
        // 判断类是否有restcontroller注解
        boolean hasClassRestAnnot = JavadocUtils.hasAnyAnnotation(clsDoc, annot_controls[1]);
        // 判断类是否有ResponseBody注解
        boolean hasClassRespAnnot = JavadocUtils.hasAnyAnnotation(clsDoc, annot_resp);
        /*获取所有public方法doc,循环提取接口文档 */
        MethodDoc[] methods = clsDoc.methods(true);
        List<ApiMethod> apiMethods = new ArrayList<>(methods.length);
        ApiMethod apiMethod = null;
        for (MethodDoc methodDoc : methods) {
            if (!isApiMethod(methodDoc, hasClassMappingAnnot, hasClassRestAnnot || hasClassRespAnnot)) {
                continue;
            }
            apiMethod = new ApiMethod();
            if (version.isPresent()) {
                apiMethod.setVersion(version.get());
            }

            // 从方法注释中提取接口名称和描述,注释首个.号前作为接口名
            if (StringUtils.isNotEmpty(methodDoc.commentText())) {
                apiMethod.setApiName(extractApiName(methodDoc.commentText()));
                apiMethod.setApiDesc(methodDoc.commentText());
            } else {
                System.err.println("错误! 没有找到接口方法注释,忽略: " + methodDoc.qualifiedName());
                continue;
            }

            // 提取方法所属模块名,优先使用方法@module注释,没有再使用类模块
            Optional<String> mthModuleName = JavadocUtils.getModuleName(methodDoc);
            apiMethod.setModuleName(mthModuleName.orElse(moduleName));
            // 提取方法作者
            Optional<String> mthAuthor = JavadocUtils.getAuthor(methodDoc);
            if (mthAuthor.isPresent()) {
                apiMethod.setAuthor(mthAuthor.get());
            } else {
                if (author.isPresent()) {
                    apiMethod.setAuthor(author.get());
                } else {
                    System.out.println("警告!没有找到接口作者 " + methodDoc.qualifiedName());
                }
            }
            // 提取http method,先取方法getM,postm,re,
            int mappingType = -1;
            Optional<AnnotationDesc> reqMappingAnnot = JavadocUtils.getAnnotation(methodDoc, annot_mappings[1]);
            if (!reqMappingAnnot.isPresent()) {
                reqMappingAnnot = JavadocUtils.getAnnotation(methodDoc, annot_mappings[2]);
                if (!reqMappingAnnot.isPresent()) {
                    reqMappingAnnot = JavadocUtils.getAnnotation(methodDoc, annot_mappings[0]);
                    if (reqMappingAnnot.isPresent()) {
                        mappingType = 0;
                    }
                } else {
                    mappingType = 2;
                    apiMethod.setRequestMethod(HttpMethod.POST);
                }
            } else {
                mappingType = 1;
                apiMethod.setRequestMethod(HttpMethod.GET);
            }
            if (reqMappingAnnot == null) {
                reqMappingAnnot = classMappingAnnot;
                mappingType = 0;
            }

            AnnotationDesc reqMappingAnnotDesc = reqMappingAnnot.get();
            if (mappingType == 0) {
                apiMethod.setRequestMethod(extractApiReqMethond(reqMappingAnnotDesc));
            }
            // 提取url,多个url以逗号分隔合并
            Set<String> apiUrls = extractApiUrl(reqMappingAnnotDesc);
            if (!apiUrls.isEmpty()) {
                if (StringUtils.isNotEmpty(baseUrl)) {
                    Set<String> fullApiUrls = new HashSet<>();
                    String mergeUrl = "";
                    apiUrls.stream().forEach(u -> {
                        mergeUrl = mergeUrl + "," + StringUtils.mergeUrl(baseUrl, u);
                    });
                    apiMethod.setApiUrl(mergeUrl.substring(1));
                } else {
                    apiMethod.setApiUrl(String.join(",", apiUrls));
                }
            }

            // 提取参数
            if (methodDoc.parameters().length > 0) {
                Set<ApiParam> params = new HashSet<>();
                ApiParam apiParam = null;
                for (Parameter param : methodDoc.parameters()) {
                    apiParam = new ApiParam();
                    /*
                     * 提取参数名,参数位置,缺省值,是否必填. 
                     * 接口参数名如果注解上未设置别名则为方法参数名
                     */
                    String paramName = param.name();
                    if (apiMethod.getRequestMethod() == HttpMethod.GET) {
                        apiParam.setPos(ParamPos.query);
                    } else {
                        apiParam.setPos(ParamPos.body);
                    }
                    // @RequestParam
                    Optional<AnnotationDesc> annotDesc = JavadocUtils.getAnnotation(param, annot_parameters[0]);
                    if (!annotDesc.isPresent()) {
                        // @RequestHeader
                        annotDesc = JavadocUtils.getAnnotation(param, annot_parameters[1]);
                        if (annotDesc.isPresent()) {
                            apiParam.setPos(ParamPos.header);
                        } else {
                            // @PathVariable
                            annotDesc = JavadocUtils.getAnnotation(param, annot_parameters[2]);
                            if (annotDesc.isPresent()) {
                                apiParam.setPos(ParamPos.pathVariable);
                            }
                        }
                    }
                    if (annotDesc.isPresent()) {
                        Optional<Object> namePropVal = JavadocUtils.getAnnotationProperty(annotDesc.get(),
                                annot_parameter_attrs[0]);
                        if (namePropVal.isPresent()) {
                            paramName = namePropVal.get().toString();
                        }
                        namePropVal = JavadocUtils.getAnnotationProperty(annotDesc.get(), annot_parameter_attrs[1]);
                        if (namePropVal.isPresent()) {
                            paramName = namePropVal.get().toString();
                        }
                        Optional<Object> defaultPropVal = JavadocUtils.getAnnotationProperty(annotDesc.get(),
                                annot_parameter_attrs[2]);
                        if (defaultPropVal.isPresent()) {
                            apiParam.setDefaultValue(defaultPropVal.get().toString());
                        }
                        Optional<Object> requiredPropVal = JavadocUtils.getAnnotationProperty(annotDesc.get(),
                                annot_parameter_attrs[3]);
                        if (requiredPropVal.isPresent()) {
                            apiParam.setRequired((boolean) requiredPropVal.get());
                        }
                    }

                    apiParam.setParamName(paramName);
                    Optional<String> paramComment = JavadocUtils.getParamComment(methodDoc, param.name());
                    if (paramComment.isPresent()) {
                        apiParam.setParamDesc(paramComment.get());
                    }
                    if (param.type().qualifiedTypeName().startsWith("java.")) {
                        apiParam.setParamType(param.typeName());
                    }

                    apiParam.setParamType(param.type().typeName());
                    apiParam.getParamDesc();
                    params.add(apiParam);
                }
            }

            // methodDoc.paramTags()
            // 返回值提取

        }
        return apiMethods;
    }

    /**
     * 是否是api接口类
     * 
     * @param cls
     * @return
     */
    private boolean isApiClass(ClassDoc cls) {
        if (cls.isOrdinaryClass()) {
            if (JavadocUtils.hasAnyAnnotation(cls, annot_controls)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是api接口方法.
     * <p>
     * 在一个controller类中,判断方法是rest接口的条件:<br>
     * 1.标注RestController,方法有Mapping注解的是rest接口. <br>
     * 2.未标注RestController,方法有Mapping和ResponseBody注解是rest接口.
     * 
     * @param methodDoc 方法Doc
     * @param hasClassMappingAnnot 类上是否存在RequestMapping注解
     * @param hasRestAnnot 类上是否存在RestController注解
     * @return 方法是rest接口返回true
     */
    private boolean isApiMethod(MethodDoc methodDoc, boolean hasClassMappingAnnot, boolean hasRestAnnot) {
        if (hasRestAnnot && hasClassMappingAnnot) {
            return true;
        }
        if (hasRestAnnot) {
            return JavadocUtils.hasAnyAnnotation(methodDoc, annot_mappings);
        } else {
            return JavadocUtils.hasAnyAnnotation(methodDoc, annot_mappings)
                    && JavadocUtils.hasAnyAnnotation(methodDoc, annot_resp);
        }
    }

    private Set<String> extractApiUrl(AnnotationDesc mappingDesc) {
        Set<String> paths = new HashSet<>();
        if (ArrayUtils.isNotEmpty(mappingDesc.elementValues())) {
            for (ElementValuePair elePair : mappingDesc.elementValues()) {
                if (annot_mapping_attrs[0].equals(elePair.element().name())
                        || annot_mapping_attrs[1].equals(elePair.element().name())) {
                    AnnotationValue[] values = ((AnnotationValue[]) elePair.value().value());
                    paths.add(values[0].value().toString());
                }
            }
        }
        return paths;
    }

    private HttpMethod extractApiReqMethond(AnnotationDesc mappingDesc) {
        if (ArrayUtils.isNotEmpty(mappingDesc.elementValues())) {
            for (ElementValuePair elePair : mappingDesc.elementValues()) {
                if (annot_mapping_attrs[1].equals(elePair.element().name())) {
                    String reqMethodEnumName = elePair.value().value().toString();
                    Optional<HttpMethod> optM = Arrays.stream(HttpMethod.values())
                            .filter(h -> reqMethodEnumName.endsWith(h.name())).findFirst();
                    if (optM.isPresent()) {
                        return optM.get();
                    }
                }
            }
        }
        return null;
    }

}
