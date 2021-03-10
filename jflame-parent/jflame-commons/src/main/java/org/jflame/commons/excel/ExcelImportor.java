package org.jflame.commons.excel;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jflame.commons.excel.handler.ArrayRowReader;
import org.jflame.commons.excel.handler.EntityRowReader;
import org.jflame.commons.excel.validator.DefaultExcelValidator;
import org.jflame.commons.excel.validator.ExcelValidationException;
import org.jflame.commons.excel.validator.IExcelValidator;
import org.jflame.commons.util.CollectionHelper;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * excel数据导入工具类 ，导入数据转为对应实体类集合或Object[]集体.导入过程可指定数据验证规则
 * <p>
 * 注:单独格式转换需使用XlsConvertorHolder类注册. 由于set是根据equals去重,所以想要导入重复数据不要覆盖equals <br>
 * 示例:
 * 
 * <pre>
 * 
 * try (ExcelImportor xlsImport = new ExcelImportor()) {
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
 * @author zyc
 */
public class ExcelImportor implements Closeable {

    private final Logger log = LoggerFactory.getLogger(ExcelImportor.class);
    // private boolean stepValid = true;// 是否执行单行验证,验证失败即中断
    private boolean allowDuplicate = true;// 是否允许重复数据
    private int startRowIndex = 1;
    // private List<Integer> curRowIndexs = new ArrayList<Integer>();
    private Workbook workbook;
    private OPCPackage pkg;

    public ExcelImportor() {
    }

    /**
     * 构造函数
     * 
     * @param excelFile
     *            指定excel文件
     * @throws ExcelAccessException
     */
    public ExcelImportor(String excelFile) throws ExcelAccessException {
        try {
            pkg = OPCPackage.open(excelFile, PackageAccess.READ);
            workbook = new XSSFWorkbook(pkg);
        } catch (InvalidFormatException | IOException e) {
            throw new ExcelAccessException("未能读取文件,或文件格式损坏," + excelFile);
        }
    }

    public ExcelImportor(File excelFile) throws ExcelAccessException {
        try {
            pkg = OPCPackage.open(excelFile, PackageAccess.READ);
            workbook = new XSSFWorkbook(pkg);
        } catch (IOException | InvalidFormatException e) {
            throw new ExcelAccessException("未能读取文件,或文件格式损坏," + excelFile);
        }
    }

    /**
     * 取得某行在excel中对应的行索引.
     * 
     * @param index
     *            导入数据集中的索引
     * @return excel中行号
     */
    /* public Integer getRowIndex(int index) {
        return curRowIndexs.get(index);
    }*/

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合.从第一张sheet导入
     * 
     * @param dataClass
     * @return
     */
    public <T> List<T> importSheet(final Class<T> dataClass) throws ExcelAccessException, ExcelValidationException {
        return importSheet(workbook.getSheetAt(0), dataClass, null);
    }

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合.从第一张sheet导入
     * 
     * @param dataClass
     * @param validator
     *            自定义验证器
     * @return
     */
    public <T> List<T> importSheet(final Class<T> dataClass, IExcelValidator<T> validator)
            throws ExcelAccessException, ExcelValidationException {
        return importSheet(workbook.getSheetAt(0), dataClass, validator);
    }

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合. 要转换属性及顺序由dataClass的excelColumn注解决定. <br>
     * 使用默认的验证器验证.使用默认单行数据转换器
     * 
     * @param sheet
     *            工作表
     * @param dataClass
     *            转换类型
     * @param <T>
     *            dataClass 泛型类型
     * @exception ExcelAccessException
     * @return
     */
    public <T> List<T> importSheet(Sheet sheet, final Class<T> dataClass)
            throws ExcelAccessException, ExcelValidationException {
        return importSheet(sheet, dataClass, null);
    }

    /**
     * 导入excel工作表数据转换为指定类型的对象集合.
     * 
     * @param sheet
     *            工作表,null时导入第一张sheet
     * @param dataClass
     *            转换类型class
     * @param validator
     *            指定数据验证类,为null使用DefaultValidator验证
     * @param <T>
     *            IExcelEntity
     * @exception ExcelAccessException
     *                导入出错
     * @exception ExcelValidationException
     *                单元格数据验证异常
     * @return
     * @see DefaultExcelValidator
     */
    public <T> List<T> importSheet(Sheet sheet, final Class<T> dataClass, IExcelValidator<T> validator)
            throws ExcelAccessException, ExcelValidationException {
        if (validator == null) {
            validator = new DefaultExcelValidator<T>();
        }

        List<ExcelColumnProperty> lstDescriptors = ExcelConvertUtils.resolveExcelColumnProperty(dataClass, false,
                Optional.empty());
        if (CollectionHelper.isEmpty(lstDescriptors)) {
            throw new ExcelAccessException("没有找到要转换的属性");
        }

        EntityRowReader<T> rowHandler = new EntityRowReader<>(lstDescriptors, dataClass);
        Sheet currentSheet = sheet == null ? workbook.getSheetAt(0) : sheet;

        // curRowIndexs.clear();
        Row curRow;
        T newObj = null;
        List<T> results = new ArrayList<>(sheet.getLastRowNum());

        Iterator<Row> rowIterator = currentSheet.rowIterator();
        while (rowIterator.hasNext()) {
            curRow = rowIterator.next();
            if (curRow.getRowNum() < startRowIndex) {
                continue;
            }
            newObj = rowHandler.extractRow(curRow);
            // 验证
            validator.valid(newObj, curRow.getRowNum());

            if (!allowDuplicate) {
                if (results.contains(newObj)) {
                    log.error("重复数据停止导入,行数:{},对象:{}", (curRow.getRowNum() + 1), newObj);
                    throw new ExcelAccessException("重复数据 第" + (curRow.getRowNum() + 1) + "行");
                }
            }
            results.add(newObj);
        }

        return results;
    }

    /**
     * 导入指定的excel工作表数据,转换为数组列表 数组元素类型只可能是bool,string类型.是否为bool,double类型由单元格类型决定.
     * 
     * @param sheet
     *            excel sheet
     * @return Object[]列表
     */
    public List<Object[]> importSheet(final Sheet sheet) throws ExcelAccessException, ExcelValidationException {
        List<Object[]> results = new ArrayList<>();
        ArrayRowReader rowHandler = new ArrayRowReader();
        Row curRow;
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            curRow = rowIterator.next();
            if (curRow.getRowNum() < startRowIndex) {
                continue;
            }
            results.add(rowHandler.extractRow(curRow));
        }
        return results;
    }

    /**
     * 设置是否单行数据验证失败立刻返回,默认为true 为false,则所有excel数据转换为java对象后再执行验证.
     * 
     * @param stepValid
     *            是否单行数据验证失败立刻返回
     */
    /* public void setStepValid(boolean stepValid) {
        this.stepValid = stepValid;
    }*/

    /**
     * 返回最近一次导入的数据对应的excel行索引集 即最后一次调用importSheet方法返回的数据集对应的excel行索引.
     * 
     * @return 最近一次导入的数据对应的excel行索引集
     */
    /* public List<Integer> getCurRowIndexs() {
        return curRowIndexs;
    }*/

    public int getStartRowIndex() {
        return startRowIndex;
    }

    /**
     * 设置从第几行开始导入,行索引从1开始.
     * 
     * @param startRowIndex
     *            开始sheet行索引
     */
    public void setStartRowIndex(int startRowIndex) {
        this.startRowIndex = startRowIndex;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public Sheet getSheet(int index) {
        return workbook.getSheetAt(index);
    }

    public Sheet getSheet(String sheetName) {
        return workbook.getSheet(sheetName);
    }

    /**
     * 设置是否允许重复数据,数据重复使用对象equals方法比较,所以如果需要去重导入对象类型应重写equals
     * 
     * @param ignoreRepeat
     *            true=允许
     */
    public void setAllowDuplicate(boolean ignoreRepeat) {
        this.allowDuplicate = ignoreRepeat;
    }

    @Override
    public void close() throws IOException {
        if (pkg != null) {
            pkg.close();
        }
        if (workbook != null) {
            workbook.close();
        }
    }

}
