package org.jflame.commons.excel;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import org.jflame.commons.excel.handler.ArrayRowWriter;
import org.jflame.commons.excel.handler.EntityRowWriter;
import org.jflame.commons.util.CharsetHelper;
import org.jflame.commons.util.CollectionHelper;
import org.jflame.commons.util.IOHelper;

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
 *    }
 *  }
 * </pre>
 * 
 * 静态方法示例:
 * 
 * <pre>
 * {@code 
 *  //导出到本地文件
 *  ExcelCreator.export(pets,"d://xxxx.xlsx");
 *  //导出浏览器HttpServletResponse response
 *  ExcelCreator.export(pets,"excelName.xlsx",response);
 *  
 *  }
 * </pre>
 * 
 * @see ExcelColumn
 * @author zyc
 */
public class ExcelCreator implements Closeable {

    private SXSSFWorkbook workbook;
    private CellStyle defaultTitleStyle;
    private CellStyle titleStyle;
    private Map<Integer,Integer> rowIndexMap = new HashMap<>();// 记录每个sheet的当前行索引
    private SXSSFSheet currentSheet;
    private Integer currentSheetIndex;

    private static final int DEFAULT_ROWACCESS_SIZE = 200;

    /**
     * 构造函数,默认生成office2007工作表.
     */
    public ExcelCreator() {
        this(DEFAULT_ROWACCESS_SIZE);
    }

    /**
     * 构造函数.是否创建标题参数
     * 
     * @param rowAccessWindowSize 内存缓冲行数,超过数据行将写入磁盘.默认200行
     */
    public ExcelCreator(int rowAccessWindowSize) {
        if (rowAccessWindowSize <= 0) {
            rowAccessWindowSize = DEFAULT_ROWACCESS_SIZE;
        }
        workbook = new SXSSFWorkbook(rowAccessWindowSize);
        currentSheet = workbook.createSheet();
    }

    /**
     * 将实体数据集合填充到指定的工作表,不指定分组
     * 
     * @param dataList 实体数据集合
     * @exception ExcelAccessException
     */
    public <T extends IExcelEntity> void fillEntityData(final List<T> dataList) {
        fillEntityData(dataList, null);
    }

    @SuppressWarnings("rawtypes")
    Map<Class<? extends IExcelEntity>,EntityRowWriter> rowWriterMap = new HashMap<>();

    /**
     * 将实体数据集合填充到指定的工作表
     * 
     * @param dataList 实体数据集合
     * @param group 分组
     * @exception ExcelAccessException
     */
    @SuppressWarnings("unchecked")
    public <T extends IExcelEntity> void fillEntityData(final List<T> dataList, final String group) {
        /* 获取有ExcelColumn注解的属性 */
        if (CollectionHelper.isNotEmpty(dataList)) {
            Class<? extends IExcelEntity> dataClass = dataList.get(0)
                    .getClass();
            EntityRowWriter<T> rowHandler = rowWriterMap.get(dataClass);
            if (rowHandler == null) {
                List<ExcelColumnProperty> columnPropertys = ExcelUtils.resolveExcelColumnProperty(dataClass, true,
                        Optional.ofNullable(group));
                if (CollectionHelper.isEmpty(columnPropertys)) {
                    throw new ExcelAccessException("没有找到要导入的属性");
                }
                if (currentSheet == null) {
                    selectSheet(0);
                }
                rowHandler = new EntityRowWriter<>(columnPropertys);
                // 创建标题行
                createTitleRow(currentSheet, columnPropertys);
            }
            // 填充数据
            Row row = null;
            for (T rowData : dataList) {
                row = currentSheet.createRow(getAndMoveRowIndex());
                rowHandler.fillRow(rowData, row);
            }
        }
    }

    /**
     * 将Object[]数据集合填充到工作表.
     * 
     * @param titles 标题行数组,可以为空
     * @param data List&lt;Object[]&gt;
     */
    public void fillArrayData(final String[] titles, final List<Object[]> data) {
        if (currentSheet == null) {
            selectSheet(0);
        }
        if (ArrayUtils.isNotEmpty(titles)) {
            createTitleRow(currentSheet, titles);
        }
        if (CollectionHelper.isNotEmpty(data)) {
            Row row = null;
            ArrayRowWriter rowHandler = new ArrayRowWriter();
            for (Object[] rowData : data) {
                row = currentSheet.createRow(getAndMoveRowIndex());
                rowHandler.fillRow(rowData, row);
            }
        }
    }

    /**
     * 合并单元格
     * 
     * @param startRow 起始行
     * @param endRow 结束行
     * @param startCol 起始列
     * @param endCol 结束列
     */
    public void mergedCell(int startRow, int endRow, int startCol, int endCol) {
        CellRangeAddress region = new CellRangeAddress(startRow, endRow, startCol, endCol);
        currentSheet.addMergedRegion(region);
    }

    /**
     * 创建一个工作表.
     * 
     * @return 创建完成的工作表对象Sheet
     */
    public SXSSFSheet createSheet() {
        return workbook.createSheet();
    }

    /**
     * 创建一个工作表,并指定工作表名.
     * 
     * @param sheetName 工作表名
     * @return 创建完成的工作表对象Sheet
     */
    public SXSSFSheet createSheet(String sheetName) {
        return workbook.createSheet(sheetName);
    }

    /**
     * 按名称返回excel sheet.
     * 
     * @param sheetName excel sheet name
     * @return Sheet
     */
    public SXSSFSheet getSheet(String sheetName) {
        return workbook.getSheet(sheetName);
    }

    /**
     * 按索引返回excel sheet.
     * 
     * @param sheetIndex excel sheet index
     * @return Sheet
     */
    public SXSSFSheet getSheet(int sheetIndex) {
        return workbook.getSheetAt(sheetIndex);
    }

    /**
     * 选择要操作的sheet
     * 
     * @param sheetIndex sheet索引
     */
    public void selectSheet(int sheetIndex) {
        currentSheet = workbook.getSheetAt(sheetIndex);
        currentSheetIndex = sheetIndex;
    }

    /**
     * 选择要操作的sheet
     * 
     * @param sheetName sheet名称
     */
    public void selectSheet(String sheetName) {
        currentSheet = workbook.getSheet(sheetName);
        currentSheetIndex = workbook.getSheetIndex(currentSheet);
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
     * @throws ExcelAccessException
     */
    public void write(OutputStream output) throws ExcelAccessException {
        if (output != null) {
            try {
                workbook.write(output);
                output.flush();
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
                workbook.dispose();
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 取得sheet当前行索引并下移一行
     * 
     * @param sheet
     * @return
     */
    private int getAndMoveRowIndex() {
        Integer index = rowIndexMap.get(currentSheetIndex);
        if (index == null) {
            rowIndexMap.put(currentSheetIndex, 1);
            return 0;
        } else {
            rowIndexMap.put(currentSheetIndex, index + 1);
            return index;
        }
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
     * 设置当前sheet的写索引
     * 
     * @param rowIndex
     */
    public void moveRowIndex(int rowIndex) {
        rowIndexMap.put(currentSheetIndex, rowIndex);
    }

    /**
     * 在第一个工作表上创建标题行.
     * 
     * @param titleNames 标题列的名称
     */
    public void createTitleRow(String[] titleNames) {
        createTitleRow(getSheet(0), titleNames);
    }

    /**
     * 指定的工作表上创建标题行.
     * 
     * @param sheet excel sheet
     * @param titleNames 标题列的名称
     */
    public void createTitleRow(Sheet sheet, String[] titleNames) {
        Row row = sheet.createRow(getAndMoveRowIndex());
        if (titleStyle == null) {
            initDefaultTitleRowStyle();
            row.setRowStyle(defaultTitleStyle);
        } else {
            row.setRowStyle(titleStyle);
        }
        Cell cell;
        int defaultWidth = 20 * 256;
        for (int i = 0; i < titleNames.length; i++) {
            cell = row.createCell(i);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(titleNames[i]);
            currentSheet.setColumnWidth(i, defaultWidth);
        }
    }

    private void createTitleRow(Sheet sheet, List<ExcelColumnProperty> columns) {
        Row row = sheet.createRow(getAndMoveRowIndex());
        if (titleStyle == null) {
            initDefaultTitleRowStyle();
            row.setRowStyle(defaultTitleStyle);
        } else {
            row.setRowStyle(titleStyle);
        }
        Cell cell;
        int size = columns.size();
        for (int i = 0; i < size; i++) {
            cell = row.createCell(i);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(columns.get(i)
                    .getName());
            sheet.setColumnWidth(i, columns.get(i)
                    .getWidth());
        }
    }

    private void initDefaultTitleRowStyle() {
        if (defaultTitleStyle == null) {
            defaultTitleStyle = workbook.createCellStyle();
        }
        defaultTitleStyle.setAlignment(HorizontalAlignment.CENTER);
        org.apache.poi.ss.usermodel.Font titleCellFont = workbook.createFont();// 字体
        titleCellFont.setFontHeightInPoints((short) 14);
        titleCellFont.setBold(true);
        titleCellFont.setColor(IndexedColors.GREEN.getIndex());
        defaultTitleStyle.setFont(titleCellFont);
    }

    /**
     * 导出实体类数据到单表的便捷方法.自动关闭输出流
     * 
     * @param data 要导出数据集
     * @param out 文件输出流
     * @param isCloseOutStream 写入完成后是否关闭输出流
     * @throws ExcelAccessException
     */
    public static void export(final List<? extends IExcelEntity> data, final OutputStream out, boolean isCloseOutStream)
            throws ExcelAccessException {
        ExcelCreator creator = null;
        try {
            creator = new ExcelCreator();
            creator.createSheet();
            creator.fillEntityData(data);
            creator.write(out);
        } catch (ExcelAccessException e) {
            throw e;
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
     * @param excelFile 文件
     * @throws ExcelAccessException
     */
    public static void export(final List<? extends IExcelEntity> data, final String excelFile)
            throws ExcelAccessException {
        OutputStream out;
        try {
            out = Files.newOutputStream(Paths.get(excelFile), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new ExcelAccessException(e);
        }
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
    public static void export(final List<? extends IExcelEntity> data, final String fileName,
            HttpServletResponse response) throws ExcelAccessException, IOException {
        response.reset();
        setFileDownloadHeader(response, fileName);
        ServletOutputStream out = response.getOutputStream();
        ExcelCreator.export(data, out, false);
    }

    static void setFileDownloadHeader(HttpServletResponse response, String fileName) {
        String encodedfileName = CharsetHelper.reEncodeGBK(fileName);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        // response.setContentType("application/vnd.ms-excel");
        response.setContentType("applicatoin/octet-stream");
    }
}
