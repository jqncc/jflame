package org.jflame.toolkit.excel;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;
import org.jflame.toolkit.excel.convertor.ICellValueConvertor;
import org.jflame.toolkit.excel.validator.DefaultValidator;
import org.jflame.toolkit.excel.validator.IValidator;
import org.jflame.toolkit.exception.ConvertException;
import org.jflame.toolkit.reflect.BeanHelper;
import org.jflame.toolkit.util.CollectionHelper;

/**
 * excel数据导入工具类 ，导入数据转为对应实体类集合或Object[]集体.导入过程可指定数据验证规则
 * <p>
 * 注:单独格式转换需使用XlsConvertorHolder类注册. 示例:
 * 
 * <pre>
 * 
 * {
 *     &#64;code ExcelImportor xlsImport = new ExcelImportor();
 *     xlsImport.setStepValid(false);
 *     xlsImport.setStartRowIndex(1);
 *     try {
 *         LinkedHashSet&lt;Pet&gt; results = xlsImport.importSheet(sheet1, Pet.class);
 *     } catch (ExcelAccessException e) {
 *         xlsImport.getErrorMap();// 错误信息
 *     }
 *     List&lt;Integer&gt; resultIndexs = xlsImport.getCurRowIndexs();
 * }
 * </pre>
 * 
 * @see ExcelColumn
 * @see ExcelConvertorSupport#registerConvertor(ICellValueConvertor...)
 * @author zyc
 */
public class ExcelImportor {

    private boolean stepValid = true;// 是否执行单行验证,验证失败即中断
    private int startRowIndex = 1;
    private Map<Integer,String> errorMap = new HashMap<>();
    private List<Integer> curRowIndexs = new ArrayList<Integer>();

    /**
     * 取得某行在excel中对应的行索引.
     * 
     * @param index 导入数据集中的索引
     * @return excel中行号
     */
    public Integer getRowIndex(int index) {
        return curRowIndexs.get(index);
    }

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合. 要转换属性及顺序由dataClass的excelColumn注解决定. 使用默认的验证器验证.
     * 
     * @param sheet 工作表
     * @param dataClass 转换类型
     * @param <T> dataClass泛型类型
     * @exception ExcelAccessException
     * @return 返回为LinkedHashSet类型的数据集
     */
    public <T> LinkedHashSet<T> importSheet(final Sheet sheet, final Class<T> dataClass) {
        return importSheet(sheet, dataClass, null, null);
    }

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合. 要转换属性及顺序由dataClass的excelColumn注解决定. 使用指定的验证器验证.
     * 
     * @param sheet 工作表
     * @param dataClass 转换类型
     * @param <T> dataClass泛型类型
     * @param validator 验证规则类
     * @exception ExcelAccessException
     * @return 返回为LinkedHashSet类型的数据集
     */
    public <T> LinkedHashSet<T> importSheet(final Sheet sheet, final Class<T> dataClass, IValidator validator) {
        return importSheet(sheet, dataClass, null, validator);
    }

    /**
     * 导入excel工作表数据转换为指定类型的对象集合.
     * 
     * @param sheet 工作表
     * @param dataClass 转换类型class
     * @param <T> dataClass泛型类型
     * @param propertyNames 指定要转换的dataClass的属性名,数组类元素顺序与工作表列的顺序一至. 如果为null使用dataClass中标有excelColumn注解的属性转换.
     * @param validator 指定数据验证类,为null使用DefaultValidator验证
     * @see DefaultValidator
     * @exception ExcelAccessException
     * @return 返回为LinkedHashSet类型的不重复元素数据集
     */
    public <T> LinkedHashSet<T> importSheet(final Sheet sheet, final Class<T> dataClass, final String[] propertyNames,
            IValidator validator) {
        if (dataClass == null) {
            throw new IllegalArgumentException("参数dataClass不能为null");
        }
        if (validator == null) {
            validator = new DefaultValidator();
        }

        LinkedHashSet<T> results = new LinkedHashSet<>();
        curRowIndexs.clear();
        errorMap.clear();
        PropertyDescriptor[] properties = BeanHelper.getPropertyDescriptors(dataClass);
        if (properties == null) {
            throw new ExcelAccessException("bean属性内省异常,类名:" + dataClass.getName());
        }
        List<ColumnProperty> lstDescriptors;
        if (propertyNames == null || propertyNames.length == 0) {
            lstDescriptors = getColumnPropertysByAnnons(properties);
        } else {
            lstDescriptors = getColumnDescriptorsByName(properties, propertyNames);
        }
        if (CollectionHelper.isNullOrEmpty(lstDescriptors)) {
            throw new ExcelAccessException("没有找到要转换的属性");
        }
        Row curRow;
        int i = 0;
        int size = lstDescriptors.size();
        ColumnProperty cProperty;
        Object newValue;

        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            curRow = rowIterator.next();
            if (curRow.getRowNum() < startRowIndex) {
                continue;
            }
            T newObj = null;
            try {
                newObj = dataClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ExcelAccessException("failed to new instance", e);
            }
            // 转换并赋值
            for (i = 0; i < size; i++) {
                if (curRow.getCell(i) == null) {
                    continue;
                }
                cProperty = lstDescriptors.get(i);
                newValue = convertValue(cProperty, curRow.getCell(i));
                if (newValue != null) {
                    try {
                        cProperty.propertyDescriptor.getWriteMethod().invoke(newObj, newValue);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new ExcelAccessException(
                                "赋值失败,excel行=" + curRow.getRowNum() + " 属性=" + cProperty.propertyDescriptor.getName(),
                                e);
                    }
                }
            }
            // 单行验证
            if (stepValid) {
                if (!validator.valid(newObj, curRow.getRowNum())) {
                    errorMap = validator.getErrors();
                    throw new ExcelAccessException("数据验证失败");
                }
            }
            if (results.add(newObj)) {
                curRowIndexs.add(curRow.getRowNum());
            }
        }
        // 整体验证
        if (!stepValid && !results.isEmpty()) {
            Map<Integer,T> validMap = new HashMap<>();
            Iterator<T> iterator = results.iterator();
            int s = 0;
            while (iterator.hasNext()) {
                validMap.put(curRowIndexs.get(s), iterator.next());
                s++;
            }
            if (!validator.validList(validMap)) {
                errorMap = validator.getErrors();
                results = null;
                curRowIndexs.clear();
                throw new ExcelAccessException("数据验证失败");
            }
        }
        return results;
    }

    /**
     * 导入指定的excel工作表数据,转换为数组列表 数组元素类型只可能是bool,string类型.是否为bool,double类型由单元格类型决定.
     * 
     * @param sheet excel sheet
     * @return Object[]列表
     */
    public List<Object[]> importSheet(final Sheet sheet) {
        List<Object[]> results = new ArrayList<>();
        Row curRow;
        Cell curCell;
        Object[] newObjs;
        int firstIndex;
        int lastIndex;
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            curRow = rowIterator.next();
            if (curRow.getRowNum() < startRowIndex) {
                continue;
            }
            firstIndex = curRow.getFirstCellNum();
            lastIndex = curRow.getLastCellNum();
            newObjs = new Object[lastIndex - firstIndex];
            for (int i = firstIndex, j = 0; i < lastIndex; i++, j++) {
                curCell = curRow.getCell(i);
                newObjs[j] = getCellValue(curCell);
            }
            results.add(newObjs);
        }
        return results;
    }

    private Object getCellValue(Cell curCell) {
        if (curCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return curCell.getBooleanCellValue();
        } else if (curCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(curCell)) {
                return curCell.getDateCellValue();
            } else {
                return curCell.getNumericCellValue();
            }
        } else if (curCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            return curCell.getCellFormula();
        } else {
            return curCell.getStringCellValue();
        }
    }

    private List<ColumnProperty> getColumnDescriptorsByName(final PropertyDescriptor[] properties,
            final String[] propertyNames) {
        List<ColumnProperty> lstDescriptors = new ArrayList<>(propertyNames.length);
        PropertyDescriptor tmProperty;
        ExcelColumn ec;
        for (String pname : propertyNames) {
            tmProperty = getPropertyDescriptorByName(properties, pname);
            if (tmProperty != null) {
                ColumnProperty cp = new ColumnProperty();
                cp.propertyDescriptor = tmProperty;
                ec = tmProperty.getReadMethod().getAnnotation(ExcelColumn.class);
                if (ec != null) {
                    cp.convertor = ec.convert();
                    cp.fmt = ec.fmt();
                    cp.columnName = ec.name();
                }
                lstDescriptors.add(cp);
            }
        }
        return lstDescriptors;
    }

    private PropertyDescriptor getPropertyDescriptorByName(PropertyDescriptor[] properties, String propertyName) {
        for (PropertyDescriptor pd : properties) {
            if (propertyName.equals(pd.getName())) {
                return pd;
            }
        }
        return null;
    }

    private List<ColumnProperty> getColumnPropertysByAnnons(PropertyDescriptor[] properties) {
        List<ColumnProperty> as = new ArrayList<ColumnProperty>();
        Method methodGetX;
        final Class<ExcelColumn> clazz = ExcelColumn.class;
        ColumnProperty newProperty;
        ExcelColumn tmpAnns;
        for (PropertyDescriptor propertyDescriptor : properties) {
            methodGetX = propertyDescriptor.getReadMethod();
            if (methodGetX.isAnnotationPresent(clazz)) {
                tmpAnns = methodGetX.getAnnotation(clazz);

                newProperty = new ColumnProperty();
                newProperty.propertyDescriptor = propertyDescriptor;
                newProperty.columnName = tmpAnns.name();
                newProperty.convertor = tmpAnns.convert();
                newProperty.fmt = tmpAnns.fmt();
                newProperty.order = tmpAnns.order();
                as.add(newProperty);
            }
        }
        Collections.sort(as);
        return as;
    }

    private final String convertErrTpl = "第{0}行{1},格式错误";

    private Object convertValue(ColumnProperty colProperty, Cell curCell) {
        Object newValue = null;
        Object cellValue = null;
        try {
            cellValue = getCellValue(curCell);
            Class<?> paramClass = colProperty.propertyDescriptor.getWriteMethod().getParameterTypes()[0];
            newValue = ExcelConvertorSupport.convertValueFromCellValue(colProperty.convertor, paramClass,
                    colProperty.fmt, cellValue);
        } catch (ConvertException e) {
            String t = StringUtils.isNotEmpty(colProperty.columnName) ? colProperty.columnName
                    : "列" + (curCell.getColumnIndex() + 1);
            throw new ExcelAccessException(MessageFormat.format(convertErrTpl, curCell.getRowIndex() + 1, t), e);
        }

        return newValue;
    }

    public boolean isStepValid() {
        return stepValid;
    }

    /**
     * 设置是否单行数据验证失败立刻返回,默认为true 为false,则所有excel数据转换为java对象后再执行验证.
     * 
     * @param stepValid 是否单行数据验证失败立刻返回
     */
    public void setStepValid(boolean stepValid) {
        this.stepValid = stepValid;
    }

    /**
     * 返回最近一次导入的数据对应的excel行索引集 即最后一次调用importSheet方法返回的数据集对应的excel行索引.
     * 
     * @return 最近一次导入的数据对应的excel行索引集
     */
    public List<Integer> getCurRowIndexs() {
        return curRowIndexs;
    }

    public int getStartRowIndex() {
        return startRowIndex;
    }

    /**
     * 设置从第几行开始导入,行索引从0开始.
     * 
     * @param startRowIndex 开始sheet行索引
     */
    public void setStartRowIndex(int startRowIndex) {
        this.startRowIndex = startRowIndex;
    }

    /**
     * 获取最近一次数据导入的验证结果.
     * 
     * @return 出错行索引与错误信息map
     */
    public Map<Integer,String> getErrorMap() {
        return errorMap;
    }

    private class ColumnProperty implements Comparable<ColumnProperty> {

        public PropertyDescriptor propertyDescriptor;
        public String convertor;
        public String fmt;
        public String columnName;
        public int order;

        public int compareTo(ColumnProperty obj) {
            return this.order - obj.order;
        }
    }
}
