package org.jflame.toolkit.excel;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jflame.toolkit.excel.handler.BaseEntitySheetRowHandler;
import org.jflame.toolkit.excel.handler.DefaultEntitySheetRowHandler;
import org.jflame.toolkit.excel.handler.MapSheetRowHandler;
import org.jflame.toolkit.reflect.BeanHelper;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;

/**
 * 创建excel文件的工具类,可创建多个工作表.
 * <p>
 * 支持生成office2003或最新office2007格式文件,默认为2007
 * <p>
 * 支持使用普通javabean对象,有序map集合 填充数据
 * <p>
 * 可根据注解自动创建标题行.属性使用excelColumn注解,可指定值格式化器 示例:
 * 
 * <pre>
 * {@code
 *  ExcelCreator xlsCreator=new ExcelCreator(EXCEL_VERSION.office2003);
 *  HSSFSheet sheet=xlsCreator.createSheet("sheet1");
 *  List<Pet> pets=new ArrayList<>();
 *  //填充pets...
 *  xlsCreator.fillEntityData(sheet,pets);
 *  FileOutPutStream out=new FileOutPutStream("xxxx.xls")
 *  xlsCreator.write(out);
 *  out.close();
 *  }
 * </pre>
 * 
 * 支持便捷静态方法，生成数据输出到流
 * 
 * <pre>
 * {@code 
 *  ExcelCreator.export(pets,response.getOutputStream());}
 * </pre>
 * 
 * @see ExcelColumn
 * @author zyc
 */
public class ExcelCreator {

    public enum ExcelVersion {
        office2003, office2007
    }

    private Workbook workbook;
    private CellStyle defaultTitleStyle;
    private CellStyle cellStyle;
    private CellStyle titleStyle;

    private boolean isAutoCreateTitleRow = true;
    private ExcelAnnotationResolver annotResolver = new ExcelAnnotationResolver();

    /**
     * 构造函数,默认生成office2003工作表.
     */
    public ExcelCreator() {
        this(ExcelVersion.office2007);
    }

    /**
     * 构造函数.指定要excel版本
     * 
     * @param excelVersion 要生成的excel版本
     */
    public ExcelCreator(ExcelVersion excelVersion) {
        if (excelVersion == ExcelVersion.office2007) {
            workbook = new XSSFWorkbook();
        } else {
            workbook = new HSSFWorkbook();
        }
        setTitleRowStyle();
    }

    /**
     * 构造函数.指定excel版本和是否创建标题参数
     * 
     * @param isCreateTitleRow 是否自动创建标题行
     * @param excelVersion excel版本
     */
    public ExcelCreator(ExcelVersion excelVersion, boolean isCreateTitleRow) {
        this(excelVersion);
        isAutoCreateTitleRow = isCreateTitleRow;
    }

    /**
     * 创建一个工作表.
     * 
     * @return 创建完成的工作表对象Sheet
     */
    public Sheet createSheet() {
        return workbook.createSheet();
    }

    /**
     * 创建一个工作表,并指定工作表名.
     * 
     * @param sheetName 工作表名
     * @return 创建完成的工作表对象Sheet
     */
    public Sheet createSheet(String sheetName) {
        return workbook.createSheet(sheetName);
    }

    /**
     * 指定的工作表上创建标题行.
     * 
     * @param sheet excel sheet
     * @param titleNames 标题列的名称
     */
    public void createTitleRow(Sheet sheet, String[] titleNames) {
        Row row = sheet.createRow((short) 0);
        Cell cell;
        int defaultWidth = 20 * 256;
        for (int i = 0; i < titleNames.length; i++) {
            cell = row.createCell(i);
            if (titleStyle != null) {
                cell.setCellStyle(titleStyle);
            } else {
                cell.setCellStyle(defaultTitleStyle);
            }
            cell.setCellType(Cell.CELL_TYPE_STRING);
            cell.setCellValue(titleNames[i]);
            sheet.setColumnWidth(i, defaultWidth);
        }
        isAutoCreateTitleRow = false;
    }

    /**
     * 在第一个工作表上创建标题行.
     * 
     * @param titleNames 标题列的名称
     */
    public void createTitleRow(String[] titleNames) {
        createTitleRow(getSheet(0), titleNames);
    }

    private void createTitleRow(Sheet sheet, List<ExcelColumnProperty> columns) {
        if (columns == null || columns.isEmpty()) {
            return;
        }
        Row row = sheet.createRow((short) 0);
        Cell cell;
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(defaultTitleStyle);
            cell.setCellType(Cell.CELL_TYPE_STRING);
            cell.setCellValue(columns.get(i).name);
            sheet.setColumnWidth(i, columns.get(i).width);
        }
    }

    /**
     * 将实体数据集合填充到指定的工作表
     * 
     * @param sheet 要填充的工作表
     * @param dataList 实体数据集合
     * @param propertyNames 数据对象的属性名.传入null为空数组将根据实体数据excel注解自动创建
     * @param sheetRowHandler 自定义单行数据转换器,缺省使用{@link DefaultEntitySheetRowHandler}
     * @exception ExcelAccessException
     */
    public void fillEntityData(Sheet sheet, final List<? extends IExcelEntity> dataList, final String[] propertyNames,
            final BaseEntitySheetRowHandler<IExcelEntity> sheetRowHandler) {
        BaseEntitySheetRowHandler<IExcelEntity> rowHandler;
        if (CollectionHelper.isNotEmpty(dataList)) {
            if (sheetRowHandler == null) {
                /* 获取有ExcelColumn注解的属性 */
                List<ExcelColumnProperty> columnPropertys = null;
                Class<?> dataClass = dataList.iterator().next().getClass();
                PropertyDescriptor[] properties = BeanHelper.getPropertyDescriptors(dataClass);
                if (properties == null) {
                    throw new ExcelAccessException("bean属性内省异常,类名:" + dataClass.getName());
                }
                // propertyNames为空则从实体类在提取所有
                if (ArrayUtils.isEmpty(propertyNames)) {
                    columnPropertys = annotResolver.getColumnPropertysByAnnons(properties);
                } else {
                    columnPropertys = annotResolver.getColumnPropertysByName(properties, propertyNames);
                }
                if (columnPropertys == null || columnPropertys.isEmpty()) {
                    throw new ExcelAccessException("没有找到要导入的属性");
                }
                rowHandler = new DefaultEntitySheetRowHandler<>(properties, columnPropertys);
            } else {
                rowHandler = sheetRowHandler;
            }
            if (isAutoCreateTitleRow) {
                createTitleRow(sheet, rowHandler.getColumnPropertys());
            }

            // 填充数据
            Row row = null;
            int rowIndex = 0;
            for (IExcelEntity rowData : dataList) {
                row = sheet.createRow(++rowIndex);
                if (cellStyle != null) {
                    row.setRowStyle(cellStyle);
                }
                rowHandler.fillRow(rowData, row);
            }
        }
    }

    /**
     * 将实体数据集合填充到填充第一个工作表,并指定被填充的属性.
     * 
     * @param data 数据集
     * @param <T> 含相关excel注解的实体bean类型
     * @param propertyNames 数据对象的属性名,顺序应与标题对应
     */
    public <T> void fillEntityData(final List<? extends IExcelEntity> data, String[] propertyNames) {
        fillEntityData(getSheet(0), data, propertyNames, null);
    }

    /**
     * 将实体数据集合填充到第一个工作表,根据实体类注解决定被填充属性.
     * 
     * @param data 数据集
     */
    public void fillEntityData(final List<? extends IExcelEntity> data) {
        fillEntityData(getSheet(0), data, null, null);
    }

    /**
     * 将实体数据集合填充到指定工作表,根据实体类注解决定被填充属性.
     * 
     * @param sheet 指定工作表sheet
     * @param data 数据集
     */
    public void fillEntityData(Sheet sheet, final List<? extends IExcelEntity> data) {
        fillEntityData(sheet, data, null, null);
    }

    /**
     * 将map数据集合填充到指定工作表.
     * 
     * @param sheet 要填充的工作表
     * @param data LinkedHashMap类型数据集合
     * @param excludeKeys 需要排除的键名,即不填充进excel
     */
    public void fillMapData(Sheet sheet, final List<LinkedHashMap<String,Object>> data, String[] excludeKeys) {
        if (data != null && !data.isEmpty()) {
            int rowIndex = isAutoCreateTitleRow ? 0 : 1;
            Row row = null;
            MapSheetRowHandler rowHandler = new MapSheetRowHandler();
            rowHandler.setExcludeKeys(excludeKeys);
            for (LinkedHashMap<String,Object> rowData : data) {
                row = sheet.createRow(rowIndex++);
                if (cellStyle != null) {
                    row.setRowStyle(cellStyle);
                }
                rowHandler.fillRow(rowData, row);
            }
        }
    }

    /**
     * 将map数据集合填充到第一个工作表.
     * 
     * @param data 要填充的数据
     * @param excludeKeys 需要排除的键名,即不填充进excel
     */
    public void fillMapData(final List<LinkedHashMap<String,Object>> data, String[] excludeKeys) {
        fillMapData(workbook.getSheetAt(0), data, excludeKeys);
    }

    /**
     * 按名称返回excel sheet.
     * 
     * @param sheetName excel sheet name
     * @return Sheet
     */
    public Sheet getSheet(String sheetName) {
        return workbook.getSheet(sheetName);
    }

    /**
     * 按索引返回excel sheet.
     * 
     * @param sheetIndex excel sheet index
     * @return Sheet
     */
    public Sheet getSheet(int sheetIndex) {
        return workbook.getSheetAt(sheetIndex);
    }

    /**
     * 返回当前excel sheet.
     * 
     * @return Workbook
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * 写入工作薄到一个输入流.
     * 
     * @param output 输出流
     * @throws IOException IOException
     */
    public void write(OutputStream output) throws IOException {
        if (output != null) {
            workbook.write(output);
        }
    }

    private void setTitleRowStyle() {
        if (defaultTitleStyle == null) {
            defaultTitleStyle = workbook.createCellStyle();
        }
        defaultTitleStyle.setAlignment(CellStyle.ALIGN_CENTER);

        org.apache.poi.ss.usermodel.Font titleCellFont = workbook.createFont();// 字体
        titleCellFont.setFontHeightInPoints((short) 12);
        titleCellFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleCellFont.setColor(IndexedColors.GREEN.getIndex());
        defaultTitleStyle.setFont(titleCellFont);
    }

    public boolean isAutoCreateTitleRow() {
        return isAutoCreateTitleRow;
    }

    public void setAutoCreateTitleRow(boolean isCreateTitleRow) {
        this.isAutoCreateTitleRow = isCreateTitleRow;
    }

    /**
     * 设置标题行样式.
     * 
     * @param titleStyle 标题行样式
     */
    public void setTitleStyle(CellStyle titleStyle) {
        this.titleStyle = titleStyle;
    }

    /**
     * 设置单元格样式.
     * 
     * @param cellStyle 单元格样式
     */
    public void setCellStyle(CellStyle cellStyle) {
        this.cellStyle = cellStyle;
    }

    /**
     * 导出实体类数据到单表的便捷方法. <br>
     * 注:请自行关闭方法输出流
     * 
     * @param data 要导出数据集
     * @param out 文件输出流,请自行关闭方法类不做处理
     * @throws IOException IOException
     */
    public static void export(final List<? extends IExcelEntity> data, OutputStream out) throws IOException {
        ExcelCreator creator = new ExcelCreator();
        creator.createSheet();
        creator.fillEntityData(data);
        creator.write(out);
    }

    /**
     * Map数据生成excel文件,并输出到HttpServletResponse
     * 
     * @param data 数据集Map
     * @param propertyNames 要导出的Key
     * @param titles 标题名与propertyNames key顺序对应
     * @param fileName 文件名,浏览器要显示的文件名
     * @param response HttpServletResponse
     * @throws IOException
     */
    public static void exportExcel(final List<LinkedHashMap<String,Object>> data, String[] propertyNames,
            String[] titles, String fileName, HttpServletResponse response) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCreator.export(data, propertyNames, titles, out);
        setFileDownloadHeader(response, fileName, out.size());
        out.writeTo(response.getOutputStream());
        out.close();
    }

    /**
     * 实体数据生成excel文件,并输出到HttpServletResponse
     * 
     * @param data List&lt;? extends IExcelEntity&gt;实体数据集
     * @param fileName 文件名,浏览器要显示的文件名
     * @param response HttpServletResponse
     * @throws IOException
     */
    public static void exportExcel(final List<? extends IExcelEntity> data, String fileName,
            HttpServletResponse response) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ExcelCreator.export(data, out);
        setFileDownloadHeader(response, fileName, out.size());
        out.writeTo(response.getOutputStream());
    }

    private static void setFileDownloadHeader(HttpServletResponse response, String fileName, long fileSize) {
        String encodedfileName = CharsetHelper.reEncodeGBK(fileName);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        response.setContentType("applicatoin/octet-stream");
        response.setHeader("Content-Length", String.valueOf(fileSize));
    }

    /**
     * 导出map键值对集合数据到单表的便捷方法.<br>
     * 注:请自行关闭方法输出流
     * 
     * @param data 要导出数据集
     * @param titles 标题
     * @param out 文件输出流,请自行关闭方法类不做处理
     * @throws IOException IOException
     */
    public static void export(final List<LinkedHashMap<String,Object>> data, String[] titles, OutputStream out)
            throws IOException {
        export(data, new String[0], titles, out);
    }

    /**
     * 导出map键值对集合数据到单表的便捷方法.<br>
     * 注:请自行关闭方法输出流
     * 
     * @param data 要导出数据集
     * @param exculdeKeyNames 要排除的键名
     * @param titles 标题
     * @param out 输出流,请自行关闭方法类不做处理
     * @throws IOException
     */
    public static void export(final List<LinkedHashMap<String,Object>> data, String[] exculdeKeyNames, String[] titles,
            OutputStream out) throws IOException {
        ExcelCreator creator = new ExcelCreator();
        creator.createSheet();
        creator.createTitleRow(titles);
        creator.fillMapData(data, exculdeKeyNames);
        creator.write(out);
    }

}
