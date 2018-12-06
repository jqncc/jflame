package org.jflame.toolkit.excel;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jflame.toolkit.excel.convertor.ICellValueConvertor;
import org.jflame.toolkit.excel.handler.ArraySheetRowHandler;
import org.jflame.toolkit.excel.handler.BaseEntitySheetRowHandler;
import org.jflame.toolkit.excel.handler.DefaultEntitySheetRowHandler;
import org.jflame.toolkit.excel.handler.ISheetRowHandler;
import org.jflame.toolkit.excel.handler.MapSheetRowHandler;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.IOHelper;

/**
 * <p>
 * 本类基于Apache POI封装提供Excel文件(只支持office2007+格式)的创建,数据导出功能.<br>
 * 支持数据源：实体类集合、LinkdHashMap集合、和数组.
 * </p>
 * <ol>
 * <li>要导出实体类集合, Bean需实现IExcelEntity接口,IExcelEntity接口只是一个标记接口所以无需实现任何方法, 在属性或对应的get方法上作@ExcelCoumn注解指定列名格式等</li>
 * <li>Map的导出,因为需要保证顺序所以请使用LinkdHashMap.如果您还需指定列名请在导出方法参数中单独指定</li>
 * </ol>
 * 示例:
 * 
 * <pre>
 * {@code
 *  try(ExcelCreator xlsCreator=new ExcelCreator()){
 *      Sheet sheet=xlsCreator.createSheet("sheet1");
 *      List<Pet> pets=new ArrayList<>();
 *      //填充数据集合...
 *      xlsCreator.fillEntityData(sheet,pets);
 *      FileOutPutStream out=new FileOutPutStream("xxxx.xlsx")
 *      //输出到文件流
 *      xlsCreator.write(out);
 *  }
 *  }
 * </pre>
 * 
 * 静态方法示例:
 * 
 * <pre>
 * {@code 
 *  FileOutPutStream out=new FileOutPutStream("xxxx.xlsx")
 *  ExcelCreator.export(pets,out);
 *  //导出浏览器HttpServletResponse response
 *  ExcelCreator.exportExcel(pets,"excelName.xlsx",response);
 *  
 *  }
 * </pre>
 * 
 * @see ExcelColumn
 * @see ICellValueConvertor
 * @see ISheetRowHandler
 * @author zyc
 */
public class ExcelCreator implements Closeable {

    @Deprecated
    public enum ExcelVersion {
        office2003,
        office2007
    }

    // private Workbook workbook;
    private SXSSFWorkbook workbook;
    private CellStyle defaultTitleStyle;
    private CellStyle cellStyle;
    private CellStyle titleStyle;

    private boolean isAutoCreateTitleRow = true;
    private ExcelAnnotationResolver annotResolver = new ExcelAnnotationResolver();
    private int rowIndex = 0;

    /**
     * 构造函数,默认生成office2003工作表.
     */
    public ExcelCreator() {
        this(ExcelVersion.office2007, true);
    }

    /**
     * 构造函数.指定要excel版本
     * 
     * @param excelVersion 要生成的excel版本
     */
    @Deprecated
    public ExcelCreator(ExcelVersion excelVersion) {
        /*if (excelVersion == ExcelVersion.office2007) {
            workbook = new XSSFWorkbook();
        } else {
            workbook = new HSSFWorkbook();
        }*/
        this(excelVersion, true);
    }

    /**
     * 构造函数.指定excel版本和是否创建标题参数
     * 
     * @param isCreateTitleRow 是否自动创建标题行
     * @param excelVersion excel版本
     */
    public ExcelCreator(ExcelVersion excelVersion, boolean isCreateTitleRow) {
        workbook = new SXSSFWorkbook(500);
        isAutoCreateTitleRow = isCreateTitleRow;
        if (isCreateTitleRow) {
            setTitleRowStyle();
        }
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
        Row row = sheet.createRow(rowIndex++);
        Cell cell;
        int defaultWidth = 20 * 256;
        for (int i = 0; i < titleNames.length; i++) {
            cell = row.createCell(i);
            if (titleStyle != null) {
                cell.setCellStyle(titleStyle);
            } else {
                cell.setCellStyle(defaultTitleStyle);
            }
            cell.setCellType(CellType.STRING);
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
        if (CollectionHelper.isEmpty(columns)) {
            return;
        }
        Row row = sheet.createRow(rowIndex++);
        Cell cell;
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            cell = row.createCell(i);
            cell.setCellStyle(defaultTitleStyle);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(columns.get(i)
                    .getName());
            sheet.setColumnWidth(i, columns.get(i)
                    .getWidth());
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
    @SuppressWarnings("unchecked")
    public <T extends IExcelEntity> void fillEntityData(Sheet sheet, final List<T> dataList,
            final String[] propertyNames, final BaseEntitySheetRowHandler<T> sheetRowHandler) {
        BaseEntitySheetRowHandler<T> rowHandler;
        if (CollectionHelper.isNotEmpty(dataList)) {
            if (sheetRowHandler == null) {
                /* 获取有ExcelColumn注解的属性 */
                List<ExcelColumnProperty> columnPropertys = null;
                Class<? extends IExcelEntity> dataClass = dataList.get(0)
                        .getClass();
                /* PropertyDescriptor[] properties = BeanHelper.getPropertyDescriptors(dataClass);
                if (properties == null) {
                    throw new ExcelAccessException("bean属性内省异常,类名:" + dataClass.getName());
                }*/
                // propertyNames为空则从实体类在提取所有
                if (ArrayUtils.isEmpty(propertyNames)) {
                    columnPropertys = annotResolver.getColumnPropertysByAnnons(dataClass);
                } else {
                    columnPropertys = annotResolver.getColumnPropertysByName(dataClass, propertyNames);
                }
                if (CollectionHelper.isEmpty(columnPropertys)) {
                    throw new ExcelAccessException("没有找到要导入的属性");
                }
                rowHandler = new DefaultEntitySheetRowHandler<>(columnPropertys, (Class<T>) dataClass);
            } else {
                rowHandler = sheetRowHandler;
            }
            if (isAutoCreateTitleRow) {
                createTitleRow(sheet, rowHandler.getColumnPropertys());
            }

            // 填充数据
            Row row = null;
            // int rowIndex = 0;
            for (T rowData : dataList) {
                row = sheet.createRow(rowIndex++);
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
        Sheet sheet;
        try {
            sheet = getSheet(0);
        } catch (IllegalArgumentException e) {
            sheet = createSheet();
        }

        fillEntityData(sheet, data, propertyNames, null);
    }

    /**
     * 将实体数据集合填充到第一个工作表,根据实体类注解决定被填充属性.
     * 
     * @param data 数据集
     */
    public void fillEntityData(final List<? extends IExcelEntity> data) {
        Sheet sheet;
        try {
            sheet = getSheet(0);
        } catch (IllegalArgumentException e) {
            sheet = createSheet();
        }
        fillEntityData(sheet, data, null, null);
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
            // int rowIndex = isAutoCreateTitleRow ? 0 : 1;
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
     * 将Object[]数据集合填充到工作表.
     * 
     * @param sheet 要填充的工作表
     * @param data List&lt;Object[]&gt;
     */
    public void fillArrayData(Sheet sheet, final List<Object[]> data) {
        if (data != null && !data.isEmpty()) {
            // int rowIndex = isAutoCreateTitleRow ? 0 : 1;
            Row row = null;
            ArraySheetRowHandler rowHandler = new ArraySheetRowHandler();
            for (Object[] rowData : data) {
                row = sheet.createRow(rowIndex++);
                if (cellStyle != null) {
                    row.setRowStyle(cellStyle);
                }
                rowHandler.fillRow(rowData, row);
            }
        }
    }

    /**
     * 将Object[]数据集合填充到第一个工作表.
     * 
     * @param data List&lt;Object[]&gt;
     */
    public void fillArrayData(final List<Object[]> data) {
        if (data != null && !data.isEmpty()) {
            // int rowIndex = isAutoCreateTitleRow ? 0 : 1;
            Row row = null;
            ArraySheetRowHandler rowHandler = new ArraySheetRowHandler();
            for (Object[] rowData : data) {
                row = workbook.getSheetAt(0)
                        .createRow(rowIndex++);
                if (cellStyle != null) {
                    row.setRowStyle(cellStyle);
                }
                rowHandler.fillRow(rowData, row);
            }
        }
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

    @Override
    public void close() throws IOException {
        if (workbook != null) {
            workbook.dispose();
        }
    }

    private void setTitleRowStyle() {
        if (defaultTitleStyle == null) {
            defaultTitleStyle = workbook.createCellStyle();
        }
        defaultTitleStyle.setAlignment(HorizontalAlignment.CENTER);
        org.apache.poi.ss.usermodel.Font titleCellFont = workbook.createFont();// 字体
        titleCellFont.setFontHeightInPoints((short) 12);
        titleCellFont.setBold(true);
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

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    /**
     * 导出实体类数据到单表的便捷方法.
     * 
     * @param data 要导出数据集
     * @param out 文件输出流
     * @param isCloseOutStream 是否关闭输出流
     * @throws IOException IOException
     */
    public static void export(final List<? extends IExcelEntity> data, OutputStream out, boolean isCloseOutStream)
            throws IOException {
        try (ExcelCreator creator = new ExcelCreator()) {
            creator.createSheet();
            creator.fillEntityData(data);
            creator.write(out);
            out.flush();
        }
        if (isCloseOutStream) {
            IOHelper.closeQuietly(out);
        }
    }

    /**
     * 导出实体类数据到单表的便捷方法.自动关闭输出流
     * 
     * @param data 要导出数据集
     * @param out 文件输出流
     * @throws IOException IOException
     */
    public static void export(final List<? extends IExcelEntity> data, OutputStream out) throws IOException {
        ExcelCreator.export(data, out, true);
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
        // ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        // ExcelCreator.export(data, outStream, false);
        // ServletOutputStream out = response.getOutputStream();
        // setFileDownloadHeader(response, fileName, outStream.size());
        // out.write(outStream.toByteArray());
        // out.flush();
        response.reset();
        setFileDownloadHeader(response, fileName);
        ServletOutputStream out = response.getOutputStream();
        ExcelCreator.export(data, out, false);
    }

    /**
     * Map数据生成excel文件,并输出到HttpServletResponse
     * 
     * @param data 数据集 LinkedHashMap
     * @param titles 标题名 顺序与map对应
     * @param fileName 文件名,浏览器要显示的文件名
     * @param response HttpServletResponse
     * @throws IOException
     */
    public static void exportExcel(final List<LinkedHashMap<String,Object>> data, String[] titles, String fileName,
            HttpServletResponse response) throws IOException {
        /* ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ExcelCreator.export(data, titles, outStream, false);
        ServletOutputStream out = response.getOutputStream();
        setFileDownloadHeader(response, fileName, outStream.size());
        out.write(outStream.toByteArray());
        out.flush();*/
        response.reset();
        setFileDownloadHeader(response, fileName);
        ServletOutputStream out = response.getOutputStream();
        ExcelCreator.export(data, titles, out, false);
    }

    /**
     * 导出map键值对集合数据到单表的便捷方法.<br>
     * 
     * @param data 要导出数据集
     * @param titles 标题
     * @param out 文件输出流
     * @throws IOException IOException
     */
    public static void export(final List<LinkedHashMap<String,Object>> data, String[] titles, OutputStream out)
            throws IOException {
        export(data, titles, out, true);
    }

    /**
     * 导出map键值对集合数据到单表的便捷方法.<br>
     * 注:请自行关闭方法输出流
     * 
     * @param data 要导出数据集
     * @param titles 标题
     * @param out 输出流,请自行关闭方法类不做处理
     * @param isCloseOutStream 是否关闭输出流
     * @throws IOException
     */
    public static void export(final List<LinkedHashMap<String,Object>> data, String[] titles, OutputStream out,
            boolean isCloseOutStream) throws IOException {
        try (ExcelCreator creator = new ExcelCreator()) {
            creator.createSheet();
            creator.createTitleRow(titles);
            creator.fillMapData(data, new String[0]);
            creator.write(out);
            out.flush();
            if (isCloseOutStream) {
                IOHelper.closeQuietly(out);
            }
        }
    }

    private static void setFileDownloadHeader(HttpServletResponse response, String fileName) {
        String encodedfileName = CharsetHelper.reEncodeGBK(fileName);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        response.setContentType("applicatoin/octet-stream");
        // response.setHeader("Content-Length", String.valueOf(fileSize));
    }
}
