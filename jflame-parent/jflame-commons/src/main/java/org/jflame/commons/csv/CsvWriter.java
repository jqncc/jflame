package org.jflame.commons.csv;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import org.jflame.commons.convert.ObjectToStringConverter;
import org.jflame.commons.excel.ExcelColumnProperty;
import org.jflame.commons.excel.ExcelUtils;
import org.jflame.commons.excel.IExcelEntity;
import org.jflame.commons.model.Chars;
import org.jflame.commons.util.CharsetHelper;
import org.jflame.commons.util.CollectionHelper;

/**
 * csv writer
 */
public class CsvWriter implements Closeable {

    private Writer outputStream = null;
    private String fileName = null;
    private boolean firstColumn = true;
    private boolean useCustomRecordDelimiter = false;
    private Charset charset;
    // this holds all the values for switches that the user is allowed to set
    private UserSettings userSettings = new UserSettings();

    private boolean initialized = false;
    private boolean closed = false;
    private String systemRecordDelimiter = System.getProperty("line.separator");

    /**
     * Double up the text qualifier to represent an occurrence of the text qualifier.
     */
    public static final int ESCAPE_MODE_DOUBLED = 1;

    /**
     * Use a backslash character before the text qualifier to represent an occurrence of the text qualifier.
     */
    public static final int ESCAPE_MODE_BACKSLASH = 2;

    /**
     * 构造函数
     * 
     * @param fileName csv文件路径
     * @param delimiter 分隔符
     * @param charset 编码
     */
    public CsvWriter(String fileName, char delimiter, Charset charset) {
        if (fileName == null) {
            throw new IllegalArgumentException("Parameter fileName can not be null.");
        }

        if (charset == null) {
            throw new IllegalArgumentException("Parameter charset can not be null.");
        }

        this.fileName = fileName;
        userSettings.Delimiter = delimiter;
        this.charset = charset;
    }

    /**
     * 构造函数.默认使用,号分隔,utf-8编码
     * 
     * @param fileName csv文件路径
     */
    public CsvWriter(String fileName) {
        this(fileName, Chars.COMMA, StandardCharsets.UTF_8);
    }

    /**
     * Creates a {@link CsvWriter} object using a Writer to write data to.
     * 
     * @param outputStream The stream to write the column delimited data to.
     * @param delimiter The character to use as the column delimiter.
     */
    public CsvWriter(Writer outputStream, char delimiter) {
        if (outputStream == null) {
            throw new IllegalArgumentException("Parameter outputStream can not be null.");
        }

        this.outputStream = outputStream;
        userSettings.Delimiter = delimiter;
        initialized = true;
    }

    /**
     * Creates a {@link CsvWriter CsvWriter} object using an OutputStream to write data to.
     * 
     * @param outputStream The stream to write the column delimited data to.
     * @param delimiter The character to use as the column delimiter.
     * @param charset The {@link java.nio.charset.Charset Charset} to use while writing the data.
     */
    public CsvWriter(OutputStream outputStream, char delimiter, Charset charset) {
        this(new OutputStreamWriter(outputStream, charset), delimiter);
    }

    public CsvWriter(OutputStream outputStream) {
        this(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), Chars.COMMA);
    }

    /**
     * Gets the character being used as the column delimiter.
     * 
     * @return The character being used as the column delimiter.
     */
    public char getDelimiter() {
        return userSettings.Delimiter;
    }

    /**
     * Sets the character to use as the column delimiter.
     * 
     * @param delimiter The character to use as the column delimiter.
     */
    public void setDelimiter(char delimiter) {
        userSettings.Delimiter = delimiter;
    }

    public char getRecordDelimiter() {
        return userSettings.RecordDelimiter;
    }

    /**
     * Sets the character to use as the record delimiter.
     * 
     * @param recordDelimiter The character to use as the record delimiter. Default is combination of standard end of
     *            line characters for Windows, Unix, or Mac.
     */
    public void setRecordDelimiter(char recordDelimiter) {
        useCustomRecordDelimiter = true;
        userSettings.RecordDelimiter = recordDelimiter;
    }

    /**
     * Gets the character to use as a text qualifier in the data.
     * 
     * @return The character to use as a text qualifier in the data.
     */
    public char getTextQualifier() {
        return userSettings.TextQualifier;
    }

    /**
     * Sets the character to use as a text qualifier in the data.
     * 
     * @param textQualifier The character to use as a text qualifier in the data.
     */
    public void setTextQualifier(char textQualifier) {
        userSettings.TextQualifier = textQualifier;
    }

    /**
     * Whether text qualifiers will be used while writing data or not.
     * 
     * @return Whether text qualifiers will be used while writing data or not.
     */
    public boolean getUseTextQualifier() {
        return userSettings.UseTextQualifier;
    }

    /**
     * Sets whether text qualifiers will be used while writing data or not.
     * 
     * @param useTextQualifier Whether to use a text qualifier while writing data or not.
     */
    public void setUseTextQualifier(boolean useTextQualifier) {
        userSettings.UseTextQualifier = useTextQualifier;
    }

    public int getEscapeMode() {
        return userSettings.EscapeMode;
    }

    public void setEscapeMode(int escapeMode) {
        userSettings.EscapeMode = escapeMode;
    }

    public void setComment(char comment) {
        userSettings.Comment = comment;
    }

    public char getComment() {
        return userSettings.Comment;
    }

    /**
     * Whether fields will be surrounded by the text qualifier even if the qualifier is not necessarily needed to escape
     * this field.
     * 
     * @return Whether fields will be forced to be qualified or not.
     */
    public boolean getForceQualifier() {
        return userSettings.ForceQualifier;
    }

    /**
     * 所有字段强制使用限定符(如双引号)包围.默认 false.
     * 
     * @param forceQualifier Whether to force the fields to be qualified or not.
     */
    public void setForceQualifier(boolean forceQualifier) {
        userSettings.ForceQualifier = forceQualifier;
    }

    final char SPACE = ' ';
    final char POUND = '#';
    final String DOUBLE_BACKSLASH = "\\\\";
    final String BACKSLASH = "\\";

    /**
     * 将内容写入在当前行新列
     * 
     * @param content The data for the new column.
     * @param preserveSpaces 是否保留内容首尾空格,true=保留,false删除首尾空格
     * @exception CsvAccessException Thrown if an error occurs while writing data to the destination stream.
     */
    public void write(String content, boolean preserveSpaces) throws CsvAccessException {
        checkClosed();
        checkInit();

        if (content == null) {
            content = StringUtils.EMPTY;
        }
        boolean textQualify = userSettings.ForceQualifier;

        if (!preserveSpaces && content.length() > 0) {
            content = content.trim();
        }

        if (!textQualify && userSettings.UseTextQualifier
                && (content.indexOf(userSettings.TextQualifier) > -1 || content.indexOf(userSettings.Delimiter) > -1
                        || (!useCustomRecordDelimiter
                                && (content.indexOf(Chars.LF) > -1 || content.indexOf(Chars.CR) > -1))
                        || (useCustomRecordDelimiter && content.indexOf(userSettings.RecordDelimiter) > -1)
                        || (firstColumn && content.length() > 0 && content.charAt(0) == userSettings.Comment) ||
                        // check for empty first column, which if on its own line must
                        // be qualified or the line will be skipped
                        (firstColumn && content.length() == 0))) {
            textQualify = true;
        }

        if (userSettings.UseTextQualifier && !textQualify && content.length() > 0 && preserveSpaces) {
            char firstLetter = content.charAt(0);

            if (firstLetter == SPACE || firstLetter == Chars.TAB) {
                textQualify = true;
            }

            if (!textQualify && content.length() > 1) {
                char lastLetter = content.charAt(content.length() - 1);

                if (lastLetter == SPACE || lastLetter == Chars.TAB) {
                    textQualify = true;
                }
            }
        }
        try {
            if (!firstColumn) {
                outputStream.write(userSettings.Delimiter);
            }

            if (textQualify) {
                outputStream.write(userSettings.TextQualifier);

                if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH) {
                    /*content = replace(content, StringUtils.EMPTY + Letters.BACKSLASH,
                        StringUtils.EMPTY + Letters.BACKSLASH + Letters.BACKSLASH);
                     content = replace(content, StringUtils.EMPTY + userSettings.TextQualifier,
                        StringUtils.EMPTY + );*/
                    content = StringUtils.replace(content, BACKSLASH, DOUBLE_BACKSLASH);
                    content = StringUtils.replace(content, String.valueOf(userSettings.TextQualifier),
                            BACKSLASH + userSettings.TextQualifier);
                } else {
                    /*content = replace(content, StringUtils.EMPTY + userSettings.TextQualifier,
                        StringUtils.EMPTY + userSettings.TextQualifier + userSettings.TextQualifier);*/
                    content = StringUtils.replace(content, String.valueOf(userSettings.TextQualifier),
                            String.valueOf(userSettings.TextQualifier) + userSettings.TextQualifier);
                }
            } else if (userSettings.EscapeMode == ESCAPE_MODE_BACKSLASH) {
                /*content = replace(content, StringUtils.EMPTY + Letters.BACKSLASH,
                    StringUtils.EMPTY + Letters.BACKSLASH + Letters.BACKSLASH);
                content = replace(content, StringUtils.EMPTY + userSettings.Delimiter,
                    StringUtils.EMPTY + Letters.BACKSLASH + userSettings.Delimiter);*/
                content = StringUtils.replace(content, BACKSLASH, DOUBLE_BACKSLASH);
                content = StringUtils.replace(content, String.valueOf(userSettings.Delimiter),
                        BACKSLASH + userSettings.Delimiter);
                if (useCustomRecordDelimiter) {
                    /*content = replace(content, StringUtils.EMPTY + userSettings.RecordDelimiter,
                        StringUtils.EMPTY + Letters.BACKSLASH + userSettings.RecordDelimiter);*/
                    content = StringUtils.replace(content, String.valueOf(userSettings.RecordDelimiter),
                            BACKSLASH + userSettings.RecordDelimiter);
                } else {
                    /* content = replace(content, StringUtils.EMPTY + Letters.CR,
                        StringUtils.EMPTY + Letters.BACKSLASH + Letters.CR);
                    content = replace(content, StringUtils.EMPTY + Letters.LF,
                        StringUtils.EMPTY + Letters.BACKSLASH + Letters.LF);*/
                    content = StringUtils.replace(content, StringUtils.CR, BACKSLASH + Chars.CR);
                    content = StringUtils.replace(content, StringUtils.LF, BACKSLASH + Chars.LF);
                }

                if (firstColumn && content.length() > 0 && content.charAt(0) == userSettings.Comment) {
                    if (content.length() > 1) {
                        content = BACKSLASH + userSettings.Comment + content.substring(1);
                    } else {
                        content = BACKSLASH + userSettings.Comment;
                    }
                }
            }

            outputStream.write(content);

            if (textQualify) {
                outputStream.write(userSettings.TextQualifier);
            }
        } catch (IOException e) {
            throw new CsvAccessException(e);
        }
        firstColumn = false;
    }

    /**
     * Writes another column of data to this record.&nbsp;Does not preserve leading and trailing whitespace in this
     * column of data.
     * 
     * @param content The data for the new column.
     * @exception CsvAccessException Thrown if an error occurs while writing data to the destination stream.
     */
    public void write(String content) throws CsvAccessException {
        write(content, false);
    }

    public void writeComment(String commentText) throws CsvAccessException {
        checkClosed();
        checkInit();
        try {
            outputStream.write(userSettings.Comment);
            outputStream.write(commentText);
            if (useCustomRecordDelimiter) {
                outputStream.write(userSettings.RecordDelimiter);
            } else {
                outputStream.write(systemRecordDelimiter);
            }
        } catch (IOException e) {
            throw new CsvAccessException(e);
        }
        firstColumn = true;
    }

    /**
     * 写入单行数据
     * 
     * @param values Values to be written.
     * @param preserveSpaces 是否保留内容首尾空格,true=保留,false删除首尾空格
     * @throws CsvAccessException Thrown if an error occurs while writing data to the destination stream.
     */
    public void writeRecord(String[] values, boolean preserveSpaces) throws CsvAccessException {
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                write(values[i], preserveSpaces);
            }

            endRecord();
        }
    }

    /**
     * 写入单行数据
     * 
     * @param values Values to be written.
     */
    public void writeRecord(String[] values) {
        writeRecord(values, false);
    }

    public <T extends IExcelEntity> void writeEntityData(List<T> dataList) throws CsvAccessException {
        writeEntityData(dataList, null);
    }

    public <T extends IExcelEntity> void writeEntityData(List<T> dataList, String group) throws CsvAccessException {
        if (CollectionHelper.isNotEmpty(dataList)) {
            Class<? extends IExcelEntity> dataClass = dataList.get(0)
                    .getClass();
            List<ExcelColumnProperty> columnPropertys = ExcelUtils.resolveExcelColumnProperty(dataClass, true,
                    Optional.ofNullable(group));
            if (CollectionHelper.isEmpty(columnPropertys)) {
                throw new CsvAccessException("没有找到要导入的属性");
            }
            String[] headers = columnPropertys.stream()
                    .map(ExcelColumnProperty::getName)
                    .toArray(String[]::new);
            try {
                writeRecord(headers);
                for (T rowData : dataList) {
                    for (ExcelColumnProperty currentProperty : columnPropertys) {
                        Object currentValue = currentProperty.getPropertyDescriptor()
                                .getReadMethod()
                                .invoke(rowData);
                        if (currentValue == null || StringUtils.EMPTY.equals(currentValue)) {
                            write(StringUtils.EMPTY);
                        } else {
                            write(ExcelUtils.convertToCellValue(currentProperty, currentValue));
                        }
                    }
                    endRecord();
                }
            } catch (Exception e) {
                throw new CsvAccessException("写入CSV失败", e);
            }
        }
    }

    @SuppressWarnings({ "rawtypes","unchecked" })
    public void writeArrayData(List<Object[]> dataList) throws CsvAccessException {
        if (CollectionHelper.isNotEmpty(dataList)) {
            Map<Integer,ObjectToStringConverter> columnConvertMap = new ConcurrentHashMap<>();
            int index = 0;
            ObjectToStringConverter converter = null;
            for (Object[] rowData : dataList) {
                for (index = 0; index < rowData.length; index++) {
                    if (columnConvertMap.containsKey(index)) {
                        converter = columnConvertMap.get(index);
                    } else {
                        converter = ExcelUtils.getDefaultWriteConverter(rowData[index].getClass(), null);
                        columnConvertMap.put(index, converter);
                    }
                    write(converter.convert(rowData[index]));
                }
                endRecord();
            }
        }
    }

    /**
     * 结束一行写入
     * 
     * @exception CsvAccessException Thrown if an error occurs while writing data to the destination stream.
     */
    public void endRecord() throws CsvAccessException {
        checkClosed();

        checkInit();
        try {
            if (useCustomRecordDelimiter) {
                outputStream.write(userSettings.RecordDelimiter);
            } else {
                outputStream.write(systemRecordDelimiter);
            }
        } catch (IOException e) {
            throw new CsvAccessException(e);
        }

        firstColumn = true;
    }

    private void checkInit() {
        if (!initialized) {
            if (fileName != null) {
                try {
                    outputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), charset));
                } catch (IOException e) {
                    throw new CsvAccessException(e);
                }
            }

            initialized = true;
        }
    }

    /**
     * Clears all buffers for the current writer and causes any buffered data to be written to the underlying device.
     * 
     * @exception IOException Thrown if an error occurs while writing data to the destination stream.
     */
    public void flush() throws IOException {
        outputStream.flush();
    }

    /**
     * 关闭并释放资源
     */
    @Override
    public void close() {
        if (!closed) {
            close(true);
            closed = true;
        }
    }

    /**
     * 关闭并释放资源
     */
    private void close(boolean closing) {
        if (!closed) {
            if (closing) {
                charset = null;
            }

            try {
                if (initialized) {
                    outputStream.close();
                }
            } catch (Exception e) {
                // just eat the exception
            }
            outputStream = null;
            closed = true;
        }
    }

    /**
     * 判断是否已关闭
     */
    private void checkClosed() {
        if (closed) {
            throw new CsvAccessException("This instance of the CsvWriter class has already been closed.");
        }
    }

    protected void finalize() {
        close(false);
    }

    private class UserSettings {

        // having these as publicly accessible members will prevent
        // the overhead of the method call that exists on properties
        public char TextQualifier;

        public boolean UseTextQualifier;

        public char Delimiter;

        public char RecordDelimiter;

        public char Comment;

        public int EscapeMode;

        public boolean ForceQualifier;

        public UserSettings() {
            TextQualifier = Chars.QUOTE;
            UseTextQualifier = true;
            Delimiter = Chars.COMMA;
            RecordDelimiter = Chars.NULL;
            Comment = POUND;
            EscapeMode = ESCAPE_MODE_DOUBLED;
            ForceQualifier = false;
        }
    }

    /**
     * 实体数据生成csv文件
     * 
     * @param csvFile csv文件路径
     * @param dataList 数据集合,实现IExcelEntity接口的实体数据集
     * @throws CsvAccessException 生成过程任何异常
     */
    public static <T extends IExcelEntity> void writeCsv(String csvFile, List<T> dataList) throws CsvAccessException {
        try (CsvWriter csvWriter = new CsvWriter(csvFile)) {
            csvWriter.writeEntityData(dataList, null);
        } catch (CsvAccessException e) {
            throw e;
        }
    }

    /**
     * 实体数据生成csv文件
     * 
     * @param outputStream 输出流
     * @param dataList 数据集合,实现IExcelEntity接口的实体数据集
     * @throws CsvAccessException 生成过程任何异常
     */
    public static <T extends IExcelEntity> void writeCsv(OutputStream outputStream, List<T> dataList)
            throws CsvAccessException {
        try (CsvWriter csvWriter = new CsvWriter(outputStream)) {
            csvWriter.writeEntityData(dataList, null);
        } catch (CsvAccessException e) {
            throw e;
        }
    }

    /**
     * 实体数据生成csv文件,并输出到HttpServletResponse 下载流
     * 
     * @param response HttpServletResponse
     * @param fileName 下载文件名
     * @param dataList 数据集合,实现IExcelEntity接口的实体数据集
     * @throws CsvAccessException
     * @throws IOException
     */
    public static <T extends IExcelEntity> void writeCsv(HttpServletResponse response, String fileName,
            List<T> dataList) throws CsvAccessException, IOException {
        response.reset();
        setFileDownloadHeader(response, fileName);
        ServletOutputStream out = response.getOutputStream();
        writeCsv(out, dataList);
    }

    static void setFileDownloadHeader(HttpServletResponse response, String fileName) {
        String encodedfileName = CharsetHelper.reEncodeGBK(fileName);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedfileName + "\"");
        response.setContentType("applicatoin/octet-stream");
    }

    /* public static String replace(String original, String pattern, String replace) {
        final int len = pattern.length();
        int found = original.indexOf(pattern);
    
        if (found > -1) {
            StringBuffer sb = new StringBuffer();
            int start = 0;
    
            while (found != -1) {
                sb.append(original.substring(start, found));
                sb.append(replace);
                start = found + len;
                found = original.indexOf(pattern, start);
            }
    
            sb.append(original.substring(start));
    
            return sb.toString();
        } else {
            return original;
        }
    }*/
}