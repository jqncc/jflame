package org.jflame.toolkit.excel;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jflame.toolkit.excel.handler.ArrayToExcelRowWriter;
import org.jflame.toolkit.excel.handler.EntityToExcelWriter;
import org.jflame.toolkit.excel.handler.MapToExcelRowWriter;
import org.jflame.toolkit.util.CharsetHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.jflame.toolkit.util.IOHelper;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

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
 * @author zyc
 */
public class ExcelCreator implements Closeable {

    @Deprecated
    public enum ExcelVersion {
        office2003,
        office2007
    }

    private SXSSFWorkbook workbook;
    private CellStyle defaultTitleStyle;
    private CellStyle titleStyle;
    // private boolean isAutoCreateTitleRow = true;
    private Map<Sheet,Integer> rowIndexMap = new HashMap<>();

    /**
     * 构造函数,默认生成office2007工作表.
     */
    public ExcelCreator() {
        this(100);
    }

    /**
     * 构造函数.是否创建标题参数
     * 
     * @param isCreateTitleRow 否自动创建标题行
     * @param rowAccessWindowSize 内存缓冲行数,超过数据行将写入磁盘.默认100行
     */
    public ExcelCreator(int rowAccessWindowSize) {
        if (rowAccessWindowSize == 0) {
            rowAccessWindowSize = 100;
        }
        workbook = new SXSSFWorkbook(rowAccessWindowSize);
        // isAutoCreateTitleRow = isCreateTitleRow;
        /*if (isCreateTitleRow) {
            setTitleRowStyle();
        }*/
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
    private void createTitleRow(Sheet sheet, String[] titleNames) {
        Row row = sheet.createRow(getAndMoveRowIndex(sheet));
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
    }

    /**
     * 在第一个工作表上创建标题行.
     * 
     * @param titleNames 标题列的名称
     */
    /*   public void createTitleRow(String[] titleNames) {
        createTitleRow(getSheet(0), titleNames);
    }*/

    private void createTitleRow(Sheet sheet, List<ExcelColumnProperty> columns) {
        if (CollectionHelper.isEmpty(columns)) {
            return;
        }
        Row row = sheet.createRow(getAndMoveRowIndex(sheet));
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
     * @exception ExcelAccessException
     */
    public <T extends IExcelEntity> void fillEntityData(final Sheet sheet, final List<T> dataList) {
        /* 获取有ExcelColumn注解的属性 */
        if (CollectionHelper.isNotEmpty(dataList)) {
            Class<? extends IExcelEntity> dataClass = dataList.get(0)
                    .getClass();
            List<ExcelColumnProperty> columnPropertys = ExcelAnnotationResolver.resolveExcelColumnProperty(dataClass,
                    true);
            if (CollectionHelper.isEmpty(columnPropertys)) {
                throw new ExcelAccessException("没有找到要导入的属性");
            }

            EntityToExcelWriter<T> rowHandler = new EntityToExcelWriter<>(columnPropertys);
            // 创建标题行
            createTitleRow(sheet, columnPropertys);
            // 填充数据
            Row row = null;
            for (T rowData : dataList) {
                row = sheet.createRow(getAndMoveRowIndex(sheet));
                rowHandler.fillRow(rowData, row);
            }
        }
    }

    /**
     * 将实体数据集合填充到第一个工作表,根据实体类注解决定被填充属性.
     * 
     * @param data 数据集
     */
    public void fillEntityData(final List<? extends IExcelEntity> data) {
        if (CollectionHelper.isNotEmpty(data)) {
            Sheet sheet;
            try {
                sheet = getSheet(0);
            } catch (IllegalArgumentException e) {
                sheet = createSheet();
            }
            fillEntityData(sheet, data);
        }
    }

    /**
     * 将map数据集合填充到指定工作表.
     * 
     * @param sheet 要填充的工作表
     * @param titles 标题行数组,可以为空
     * @param data LinkedHashMap类型数据集合
     * @param excludeKeys 需要排除的键名,即不填充进excel
     */
    public void fillMapData(final Sheet sheet, final String[] titles, final List<LinkedHashMap<String,Object>> data) {
        if (ArrayUtils.isNotEmpty(titles)) {
            createTitleRow(sheet, titles);
        }
        if (CollectionHelper.isNotEmpty(data)) {
            Row row = null;
            MapToExcelRowWriter rowHandler = new MapToExcelRowWriter();
            for (LinkedHashMap<String,Object> rowData : data) {
                row = sheet.createRow(getAndMoveRowIndex(sheet));
                rowHandler.fillRow(rowData, row);
            }
        }
    }

    /**
     * 将map数据集合填充到第一个工作表.
     * 
     * @param titles 标题行数组,可以为空
     * @param data 要填充的数据
     * @param excludeKeys 需要排除的键名,即不填充进excel
     */
    public void fillMapData(final String[] titles, final List<LinkedHashMap<String,Object>> data) {
        fillMapData(workbook.getSheetAt(0), titles, data);
    }

    /**
     * 将Object[]数据集合填充到工作表.
     * 
     * @param sheet 要填充的工作表
     * @param titles 标题行数组,可以为空
     * @param data List&lt;Object[]&gt;
     */
    public void fillArrayData(final Sheet sheet, final String[] titles, final List<Object[]> data) {
        if (ArrayUtils.isNotEmpty(titles)) {
            createTitleRow(sheet, titles);
        }
        if (CollectionHelper.isNotEmpty(data)) {
            Row row = null;
            ArrayToExcelRowWriter rowHandler = new ArrayToExcelRowWriter();
            for (Object[] rowData : data) {
                row = sheet.createRow(getAndMoveRowIndex(sheet));
                rowHandler.fillRow(rowData, row);
            }
        }
    }

    /**
     * 将Object[]数据集合填充到第一个工作表.
     * 
     * @param data List&lt;Object[]&gt;
     */
    public void fillArrayData(final String[] titles, final List<Object[]> data) {
        if (CollectionHelper.isNotEmpty(data)) {
            Sheet sheet;
            try {
                sheet = getSheet(0);
            } catch (IllegalArgumentException e) {
                sheet = createSheet();
            }
            Row row = null;
            ArrayToExcelRowWriter rowHandler = new ArrayToExcelRowWriter();
            for (Object[] rowData : data) {
                row = sheet.createRow(getAndMoveRowIndex(sheet));
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
    public void write(OutputStream output) throws ExcelAccessException {
        if (output != null) {
            try {
                workbook.write(output);
            } catch (IOException e) {
                throw new ExcelAccessException("excel写入异常", e);
            }
        }
    }

    /**
     * 写入工作薄到到HttpServletResponse,下载excel
     * 
     * @param response
     * @param fileName
     * @throws IOException
     */
    public void write(HttpServletResponse response, String fileName) throws IOException {
        response.reset();
        setFileDownloadHeader(response, fileName);
        ServletOutputStream out = response.getOutputStream();
        write(out);
    }

    @Override
    public void close() {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*private void setTitleRowStyle() {
        if (defaultTitleStyle == null) {
            defaultTitleStyle = workbook.createCellStyle();
        }
        defaultTitleStyle.setAlignment(HorizontalAlignment.CENTER);
        org.apache.poi.ss.usermodel.Font titleCellFont = workbook.createFont();// 字体
        titleCellFont.setFontHeightInPoints((short) 12);
        titleCellFont.setBold(true);
        titleCellFont.setColor(IndexedColors.GREEN.getIndex());
        defaultTitleStyle.setFont(titleCellFont);
    }*/

    /**
     * 取得sheet当前行索引并下移一行
     * 
     * @param sheet
     * @return
     */
    private int getAndMoveRowIndex(Sheet sheet) {
        Integer index = rowIndexMap.get(sheet);
        if (index == null) {
            rowIndexMap.put(sheet, 1);
            return 0;
        } else {
            rowIndexMap.put(sheet, index + 1);
            return index;
        }
    }

    /*public boolean isAutoCreateTitleRow() {
        return isAutoCreateTitleRow;
    }
    
    public void setAutoCreateTitleRow(boolean isCreateTitleRow) {
        this.isAutoCreateTitleRow = isCreateTitleRow;
    }*/

    /**
     * 设置标题行样式.
     * 
     * @param titleStyle 标题行样式
     */
    /* public void setTitleStyle(CellStyle titleStyle) {
        this.titleStyle = titleStyle;
    }*/

    /**
     * 设置第一个sheet的行索引
     * 
     * @param rowIndex
     */
    public void setRowIndex(int rowIndex) {
        rowIndexMap.put(workbook.getSheetAt(0), rowIndex);
    }

    public void setRowIndex(Sheet sheet, int rowIndex) {
        rowIndexMap.put(sheet, rowIndex);
    }

    /**
     * 导出实体类数据到单表的便捷方法.
     * 
     * @param data 要导出数据集
     * @param out 文件输出流
     * @param isCloseOutStream 是否关闭输出流
     * @throws ExcelAccessException
     */
    public static void export(final List<? extends IExcelEntity> data, OutputStream out, boolean isCloseOutStream)
            throws ExcelAccessException {
        ExcelCreator creator = null;
        try {
            creator = new ExcelCreator();
            creator.createSheet();
            creator.fillEntityData(data);
            creator.write(out);
            out.flush();
        } catch (IOException e) {
            throw new ExcelAccessException(e);
        } finally {
            if (creator != null) {
                creator.close();
            }
            if (isCloseOutStream) {
                IOHelper.closeQuietly(out);
            }
        }
    }

    /**
     * 导出实体类数据到单表的便捷方法.自动关闭输出流
     * 
     * @param data 要导出数据集
     * @param out 文件输出流
     * @throws IOException IOException
     */
    public static void export(final List<? extends IExcelEntity> data, OutputStream out) throws ExcelAccessException {
        ExcelCreator.export(data, out, true);
    }

    /**
     * 实体数据生成excel文件,并输出到HttpServletResponse下载流
     * 
     * @param data List&lt;? extends IExcelEntity&gt;实体数据集
     * @param fileName 文件名,浏览器要显示的文件名
     * @param response HttpServletResponse
     * @throws IOException
     */
    public static void exportExcel(final List<? extends IExcelEntity> data, String fileName,
            HttpServletResponse response) throws ExcelAccessException, IOException {
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
            HttpServletResponse response) throws ExcelAccessException, IOException {
        response.reset();
        setFileDownloadHeader(response, fileName);
        ServletOutputStream out = response.getOutputStream();
        ExcelCreator.export(data, titles, out, false);
    }

    /**
     * 导出map键值对集合数据到单表的便捷方法,导出完成后将关闭输出流
     * 
     * @param data 要导出数据集
     * @param titles 标题
     * @param out 文件输出流
     * @throws ExcelAccessException
     */
    public static void export(final List<LinkedHashMap<String,Object>> data, String[] titles, OutputStream out)
            throws ExcelAccessException {
        export(data, titles, out, true);
    }

    /**
     * 导出map键值对集合数据到单表的便捷方法.
     * 
     * @param data 要导出数据集
     * @param titles 标题
     * @param out 输出流,请自行关闭方法类不做处理
     * @param isCloseOutStream 完成后是否关闭输出流,true为关闭,如果为false请务必手动请关闭输出流
     * @throws ExcelAccessException
     */
    public static void export(final List<LinkedHashMap<String,Object>> data, final String[] titles, OutputStream out,
            boolean isCloseOutStream) throws ExcelAccessException {
        ExcelCreator creator = null;
        try {
            creator = new ExcelCreator();
            creator.createSheet();
            creator.fillMapData(titles, data);
            creator.write(out);
            out.flush();
        } catch (IOException e) {
            throw new ExcelAccessException(e);
        } finally {
            if (creator != null) {
                creator.close();
            }
            if (isCloseOutStream) {
                IOHelper.closeQuietly(out);
            }
        }
    }

    public static void setFileDownloadHeader(HttpServletResponse response, String fileName) {
        String encodedfileName = CharsetHelper.reEncodeGBK(fileName);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        response.setContentType("applicatoin/octet-stream");
        // response.setHeader("Content-Length", String.valueOf(fileSize));
    }
}
