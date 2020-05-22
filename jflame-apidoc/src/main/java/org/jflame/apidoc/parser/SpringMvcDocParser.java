package org.jflame.apidoc.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jflame.apidoc.enums.HttpMethod;
import org.jflame.apidoc.enums.JsonDataType;
import org.jflame.apidoc.enums.MediaType;
import org.jflame.apidoc.enums.ParamPos;
import org.jflame.apidoc.model.ApiMethod;
import org.jflame.apidoc.model.ApiModule;
import org.jflame.apidoc.model.ApiParam;
import org.jflame.apidoc.util.ArrayUtils;
import org.jflame.apidoc.util.CollectionUtils;
import org.jflame.apidoc.util.JavadocUtils;
import org.jflame.apidoc.util.StringUtils;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.AnnotationValue;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;

/**
 * springmvc框架api文档解析器
 * 
 * @author yucan.zhang
 */
public class SpringMvcDocParser extends ClassDocParser {

    private final static String[] annot_controls = { "Controller","RestController" };// contoller注解
    private final static String[] annot_mappings = { "RequestMapping","GetMapping","PostMapping" };// mapping注解
    private final static String[] annot_parameters = { "RequestParam","RequestHeader","PathVariable","RequestBody" };// 参数注解
    private final static String[] annot_mapping_attrs = { "value","path","method" };// mapping注解的属性
    private final static String[] annot_parameter_attrs = { "value","name","defaultValue","required" };// 参数注解属性
    private final static String annot_resp = "ResponseBody";// @ResponseBody注解

    @Override
    public List<ApiMethod> parse(ClassDoc clsDoc) {
        if (!isApiClass(clsDoc) || JavadocUtils.hasAnyAnnotation(clsDoc, annot_deprecated)) {
            return null;
        }

        String baseUrl = null;
        AnnotationDesc classMappingAnnot = null;
        Optional<AnnotationDesc> classMappingAnnotOpt = JavadocUtils.getAnnotation(clsDoc, annot_mappings[0]);
        if (classMappingAnnotOpt.isPresent()) {
            classMappingAnnot = classMappingAnnotOpt.get();
        }
        if (classMappingAnnotOpt.isPresent()) {
            Set<String> baseUrls = extractApiUrl(classMappingAnnot);
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
        if (methods.length == 0) {
            return null;
        }
        List<ApiMethod> apiMethods = new ArrayList<>(methods.length);
        ApiMethod apiMethod = null;
        for (MethodDoc methodDoc : methods) {
            if (!isApiMethod(methodDoc, classMappingAnnotOpt.isPresent(), hasClassRestAnnot || hasClassRespAnnot)) {
                continue;
            }
            apiMethod = new ApiMethod();
            // 提取方法名称,模块,版本,作者等基础信息
            extractMethodBaseInfo(clsDoc, methodDoc, apiMethod);
            if (StringUtils.isEmpty(apiMethod.getApiName())) {
                continue;
            }
            // 提取http method
            extractHttpMethod(classMappingAnnot, methodDoc, apiMethod);
            // 提取url,多个url以逗号分隔合并
            extractApiMethodUrl(classMappingAnnot, baseUrl, apiMethod);

            // 提取参数
            if (methodDoc.parameters().length > 0) {
                for (Parameter paramDesc : methodDoc.parameters()) {
                    if (!isIgnoreParam(paramDesc)) {
                        parseParams(methodDoc, paramDesc, apiMethod);
                    }
                }
                // 只有一个参数,且参数类型为实体对象,将参数的子参数作为直接参数
                if (methodDoc.parameters().length == 1) {
                    overrideOnlyComplexParam(apiMethod);
                }
            }

            // 提取 返回值
            apiMethods.add(apiMethod);
            Type returnType = methodDoc.returnType();

        }
        return apiMethods;
    }

    private void overrideOnlyComplexParam(ApiMethod apiMethod) {
        ApiParam param = null;
        if (CollectionUtils.isNotEmpty(apiMethod.getBodyParams())) {
            param = apiMethod.getBodyParams().get(0);
        } else if (CollectionUtils.isNotEmpty(apiMethod.getQueryParams())) {
            param = apiMethod.getQueryParams().get(0);
        } else if (CollectionUtils.isNotEmpty(apiMethod.getHeaderParams())) {
            param = apiMethod.getHeaderParams().get(0);
        }
        String paramDataType = param.getElementDataType();
        if (JsonDataType.object.name().equals(paramDataType)) {
            List<ApiParam> childParams = (List<ApiParam>) param.getChildElements();
            if (param.getPos() == ParamPos.query) {
                apiMethod.getQueryParams().clear();
                apiMethod.getQueryParams().addAll(childParams);
            } else if (param.getPos() == ParamPos.body) {
                apiMethod.getBodyParams().clear();
                apiMethod.getBodyParams().addAll(childParams);
            } else if (param.getPos() == ParamPos.header) {
                apiMethod.getHeaderParams().clear();
                apiMethod.getHeaderParams().addAll(childParams);
            }
        }
    }

    /**
     * 解析出api方法的参数
     * 
     * @param methodDoc
     * @param paramDesc
     * @param apiMethod
     */
    protected void parseParams(MethodDoc methodDoc, Parameter paramDesc, ApiMethod apiMethod) {
        ApiParam apiParam = new ApiParam();
        if (apiMethod.getRequestMethod() == HttpMethod.GET) {
            apiParam.setPos(ParamPos.query);
        } else {
            apiParam.setPos(ParamPos.body);
        }

        // @RequestBody
        Optional<AnnotationDesc> annotDesc = JavadocUtils.getAnnotation(paramDesc, annot_parameters[3]);
        if (annotDesc.isPresent()) {
            apiParam.setPos(ParamPos.body);
            apiMethod.setProduces(MediaType.APPLICATION_JSON);
        } else {
            // @RequestParam
            annotDesc = JavadocUtils.getAnnotation(paramDesc, annot_parameters[0]);
            if (!annotDesc.isPresent()) {
                // @RequestHeader
                annotDesc = JavadocUtils.getAnnotation(paramDesc, annot_parameters[1]);
                if (annotDesc.isPresent()) {
                    apiParam.setPos(ParamPos.header);
                    extractParamAttris(paramDesc, apiParam, annotDesc.get());
                } else {
                    // @PathVariable
                    annotDesc = JavadocUtils.getAnnotation(paramDesc, annot_parameters[2]);
                    if (annotDesc.isPresent()) {
                        apiParam.setPos(ParamPos.query);
                    }
                }
            }
            if (annotDesc.isPresent()) {
                extractParamAttris(paramDesc, apiParam, annotDesc.get());
            } else {
                apiParam.setElementName(paramDesc.name());
            }
        }
        // 参数注释doc
        Optional<String> paramComment = JavadocUtils.getParamComment(methodDoc, paramDesc.name());
        if (paramComment.isPresent()) {
            apiParam.setElementDesc(paramComment.get());
        }
        // 参数类型
        JsonDataType paramDataType = JsonDataType.toType(paramDesc.type());
        apiParam.setElementDataType(paramDataType.name());
        if (paramDataType == JsonDataType.object) {
            extractComplexObject(apiParam, paramDesc.type().asClassDoc());
        }
        if (apiParam.getPos() == ParamPos.header) {
            apiMethod.addHeaderParam(apiParam);
        } else if (apiParam.getPos() == ParamPos.body) {
            apiMethod.addBodyParam(apiParam);
        } else if (apiParam.getPos() == ParamPos.query) {
            apiMethod.addQueryParam(apiParam);
        }
    }

    /**
     * 提取api方法的名称,版本号,模块,作者等信息
     * 
     * @param clsDoc 方法所在类doc
     * @param methodDoc 方法doc
     * @param apiMethod
     */
    private void extractMethodBaseInfo(ClassDoc clsDoc, MethodDoc methodDoc, ApiMethod apiMethod) {
        // 接口名称
        if (StringUtils.isNotEmpty(methodDoc.commentText())) {
            apiMethod.setApiName(extractApiName(methodDoc.commentText()));
            apiMethod.setApiDesc(methodDoc.commentText());
        } else {
            System.err.println("错误! 没有找到接口方法注释,忽略: " + methodDoc.qualifiedName());
        }
        // @version版本号
        Optional<String> version = JavadocUtils.getVersion(clsDoc);
        if (version.isPresent()) {
            apiMethod.setVersion(version.get());
        }

        // @module 提取接口方法所属模块, 先取方法注释再取类注释
        Optional<String> mthModuleName = JavadocUtils.getModuleName(methodDoc);
        if (mthModuleName.isPresent()) {
            apiMethod.setModuleName(mthModuleName.get());
        } else {
            apiMethod.setModuleName(JavadocUtils.getModuleName(clsDoc).orElse(ApiModule.DEFAULT_MODULE));
        }
        // @author 提取接口作者,先取方法注释再取类注释
        Optional<String> mthAuthor = JavadocUtils.getAuthor(methodDoc);
        if (mthAuthor.isPresent()) {
            apiMethod.setAuthor(mthAuthor.get());
        } else {
            Optional<String> author = JavadocUtils.getAuthor(clsDoc);
            if (author.isPresent()) {
                apiMethod.setAuthor(author.get());
            }
        }
    }

    private void extractHttpMethod(AnnotationDesc classMappingAnnot, MethodDoc methodDoc, ApiMethod apiMethod) {
        int mappingType = -1;
        Optional<AnnotationDesc> reqMappingAnnot = JavadocUtils.getAnnotation(methodDoc, annot_mappings[1]);// GetMapping
        if (!reqMappingAnnot.isPresent()) {
            reqMappingAnnot = JavadocUtils.getAnnotation(methodDoc, annot_mappings[2]);// PostMapping
            if (!reqMappingAnnot.isPresent()) {
                reqMappingAnnot = JavadocUtils.getAnnotation(methodDoc, annot_mappings[0]);// RequestMapping
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
        // 如果是RequestMapping再从属性取
        if (reqMappingAnnot.isPresent() && mappingType == 0) {
            apiMethod.setRequestMethod(extractHttpMethondFromReqMapping(reqMappingAnnot.get()));
        } else {
            if (classMappingAnnot != null) {
                apiMethod.setRequestMethod(extractHttpMethondFromReqMapping(classMappingAnnot));
            }
        }
    }

    /**
     * 递归解析复合参数
     * 
     * @param apiParam
     * @param clsDoc
     */
    private void extractComplexObject(ApiParam apiParam, ClassDoc clsDoc) {
        FieldDoc[] fieldDocs = clsDoc.fields(true);
        if (ArrayUtils.isEmpty(fieldDocs)) {
            List<ApiParam> childParamLst = new ArrayList<>();
            ApiParam childParam;
            for (FieldDoc fieldDoc : fieldDocs) {
                if (isIgnoreField(clsDoc, fieldDoc, false)) {
                    continue;
                }
                childParam = new ApiParam();
                childParam.setElementName(childParam.getElementName());
                if (StringUtils.isNotEmpty(fieldDoc.commentText())) {
                    childParam.setElementName(extractApiName(fieldDoc.commentText()));
                    childParam.setElementDesc(fieldDoc.commentText());
                }
                if (isNotNullParam(fieldDoc)) {
                    childParam.setRequired(true);
                }
                JsonDataType paramDataType = JsonDataType.toType(fieldDoc.type());
                childParam.setElementDataType(paramDataType.name());
                if (paramDataType == JsonDataType.object) {
                    extractComplexObject(childParam, fieldDoc.type().asClassDoc());
                }
                childParamLst.add(childParam);
            }
            if (!childParamLst.isEmpty()) {
                apiParam.setChildElements(childParamLst);
            }
        }
    }

    /**
     * 提取参数 名称,默认值,是否必填等属性.不适用RequestBody注解
     * 
     * @param paramDesc 参数doc
     * @param apiParam 新参数对象
     * @param annotDesc 参数注解
     * @return
     */
    private void extractParamAttris(Parameter paramDesc, ApiParam apiParam, AnnotationDesc annotDesc) {
        // "value","name","defaultValue","required"
        if (annotDesc.annotationType().name() == annot_parameters[3]) {
            return;
        }
        // 参数名,默认参数变量名,再从注解里取name或value属性值
        String paramName = paramDesc.name();
        Optional<Object> namePropVal = JavadocUtils.getAnnotationProperty(annotDesc, annot_parameter_attrs[0]);
        if (namePropVal.isPresent()) {
            paramName = namePropVal.get().toString();
        }
        namePropVal = JavadocUtils.getAnnotationProperty(annotDesc, annot_parameter_attrs[1]);
        if (namePropVal.isPresent()) {
            paramName = namePropVal.get().toString();
        }
        apiParam.setElementName(paramName);

        // PathVariable没有默认值且必填
        if (annotDesc.annotationType().name() == annot_parameters[2]) {
            apiParam.setRequired(true);
        } else {
            Optional<Object> requiredPropVal = JavadocUtils.getAnnotationProperty(annotDesc, annot_parameter_attrs[3]);
            if (requiredPropVal.isPresent()) {
                apiParam.setRequired((boolean) requiredPropVal.get());
            }
            // 默认值
            Optional<Object> defaultPropVal = JavadocUtils.getAnnotationProperty(annotDesc, annot_parameter_attrs[2]);
            if (defaultPropVal.isPresent()) {
                apiParam.setDefaultValue(defaultPropVal.get().toString());
            }
        }
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

    /**
     * 是否忽略api方法参数
     * 
     * @param paramDesc
     * @return true忽略
     */

    private boolean isIgnoreParam(Parameter paramDesc) {
        if (paramDesc.typeName().indexOf("ServletRequest") >= 0
                || paramDesc.typeName().indexOf("ServletResponse") >= 0) {
            return true;
        }
        return false;
    }

    /**
     * 提取api方法的url
     * 
     * @param mappingDesc 方法上的mapping注解doc
     * @param baseUrl 类mapping设置基础url
     * @param apiMethod
     */
    private void extractApiMethodUrl(AnnotationDesc mappingDesc, String baseUrl, ApiMethod apiMethod) {
        Set<String> apiUrls = extractApiUrl(mappingDesc);
        if (!apiUrls.isEmpty()) {
            if (StringUtils.isNotEmpty(baseUrl)) {
                String mergeUrl = "";
                for (String u : apiUrls) {
                    mergeUrl = mergeUrl + "," + StringUtils.mergeUrl(baseUrl, u);
                }
                apiMethod.setApiUrl(mergeUrl.substring(1));
            } else {
                apiMethod.setApiUrl(String.join(",", apiUrls));
            }
        }
    }

    private Set<String> extractApiUrl(AnnotationDesc mappingDesc) {
        Set<String> apiUrls = new HashSet<>();
        if (ArrayUtils.isNotEmpty(mappingDesc.elementValues())) {
            for (ElementValuePair elePair : mappingDesc.elementValues()) {
                if (annot_mapping_attrs[0].equals(elePair.element().name())
                        || annot_mapping_attrs[1].equals(elePair.element().name())) {
                    AnnotationValue[] values = ((AnnotationValue[]) elePair.value().value());
                    apiUrls.add(values[0].value().toString());
                }
            }
        }
        return apiUrls;
    }

    /**
     * 从RequestMapping上提取httpMethod
     * 
     * @param mappingDesc
     * @return
     */
    private HttpMethod extractHttpMethondFromReqMapping(AnnotationDesc mappingDesc) {
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
