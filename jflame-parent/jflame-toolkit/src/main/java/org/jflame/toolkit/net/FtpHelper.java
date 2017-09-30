package org.jflame.toolkit.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.jflame.toolkit.exception.RemoteAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ftp客户端常用操作工具类,本类是对apache commons-net中ftplclient的封装
 * <p>
 * 默认端口21,默认编码gbk
 * 
 * @see org.apache.commons.net.ftp.FTPClient
 * @author zyc CreateDate:2015年3月24日上午9:14:43
 */
public final class FtpHelper {

    private final Logger log = LoggerFactory.getLogger(FtpHelper.class);

    private FTPClient ftpClient = new FTPClient();

    private String ftpServerUrl;
    private int ftpPort = 21;
    private String ftpUser;
    private String ftpPassword;
    private String charset;
    private FTPClientConfig config;
    private boolean passiveMode = true;// 被动模式连接
    private boolean binaryTransfer = true;// 是否使用二进制传输

    private int clientTimeout = 1000 * 30;

    private String currentDir = null;// 当前ftp目录

    /**
     * 构造函数,使用默认端口21
     * 
     * @param ftpServerUrl ftp服务器地址
     * @param ftpUser 用户名
     * @param ftpPassword 密码
     */
    public FtpHelper(String ftpServerUrl, String ftpUser, String ftpPassword) {
        this.ftpServerUrl = ftpServerUrl;
        this.ftpUser = ftpUser;
        this.ftpPassword = ftpPassword;
    }

    /**
     * 构造函数
     * 
     * @param ftpServerUrl ftp服务器地址
     * @param ftpUser 用户名
     * @param ftpPassword 密码
     * @param port 端口
     */
    public FtpHelper(String ftpServerUrl, String ftpUser, String ftpPassword, int port) {
        this.ftpServerUrl = ftpServerUrl;
        this.ftpUser = ftpUser;
        this.ftpPassword = ftpPassword;
        this.ftpPort = port;
    }

    /**
     * 连接并登录到ftp服务器
     * 
     * @throws RemoteAccessException
     */
    public boolean login() throws RemoteAccessException {
        boolean isSuccess = false;
        int loginCount = 3;
        final int noop_period = 300;
        int reply;
        // 有异常,隔1秒再试,试3次
        while (loginCount >= 0) {
            try {
                ftpClient.connect(ftpServerUrl, ftpPort);
                ftpClient.setControlEncoding(charset == null ? StandardCharsets.UTF_8.name() : charset);
                if (passiveMode) {
                    ftpClient.enterLocalPassiveMode();
                }
                if (config != null) {
                    ftpClient.configure(config);
                }
                if (binaryTransfer) {
                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                }
                ftpClient.setSoTimeout(clientTimeout);
                ftpClient.setControlKeepAliveTimeout(noop_period);// 每5分钟在控制连接上发送noop命令避免被路由中断
                ftpClient.login(ftpUser, ftpPassword);
                reply = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect();
                } else {
                    isSuccess = true;
                }
                return isSuccess;
            } catch (IOException e) {
                close();
                if (loginCount == 1) {
                    throw new RemoteAccessException(e);
                }
            }
            loginCount--;
            if (isSuccess) {
                break;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        return isSuccess;
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
            } catch (IOException e) {
                log.error("注销ftp异常", e);
            } finally {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    log.error("断开ftp连接异常", e);
                }
            }
        }
    }

    /**
     * 列出当前目录下文件名列表
     * 
     * @return
     * @throws RemoteAccessException
     */
    public String[] listFileNames() throws RemoteAccessException {
        if (!ftpClient.isConnected()) {
            login();
        }
        return listFileNames(null);
    }

    /**
     * 列出指定目录下文件名列表
     * 
     * @param dir 目录
     * @return 返回目录下文件名数组,如果为null表示目录不存在
     * @throws RemoteAccessException
     */
    public String[] listFileNames(String dir) throws RemoteAccessException {
        try {
            if (!ftpClient.isConnected()) {
                login();
            }
            if (dir != null && !"".equals(dir)) {
                if (!dir.equals(currentDir)) {
                    if (ftpClient.changeWorkingDirectory(dir)) {
                        currentDir = dir;
                    } else {
                        return null;
                    }
                }
            }
            return ftpClient.listNames();
        } catch (IOException e) {
            throw new RemoteAccessException(e);
        }
    }

    /**
     * 取得目录下ftp文件对象
     * 
     * @param dir
     * @param filter 文件过滤器
     * @return
     * @throws RemoteAccessException
     */
    public FTPFile[] listFtpFiles(String dir, FTPFileFilter filter) throws RemoteAccessException {
        try {
            if (!ftpClient.isConnected()) {
                login();
            }
            if (filter == null) {
                return ftpClient.listFiles(dir);
            } else {
                return ftpClient.listFiles(dir, filter);
            }
        } catch (IOException e) {
            throw new RemoteAccessException(e);
        }
    }

    /**
     * 下载文件
     * 
     * @param localDir 本地存放目录
     * @param destDir 目标文件目录
     * @param destFileName 待下载文件
     * @return 对应每个文件的下载结果
     * @throws RemoteAccessException ftp访问异常
     */
    public boolean[] downloadFile(String localDir, String destDir, String... destFileName)
            throws RemoteAccessException {
        if (!ftpClient.isConnected()) {
            login();
        }
        boolean[] isOks = new boolean[destFileName.length];
        Arrays.fill(isOks, false);
        File folder = new File(localDir);
        BufferedOutputStream outBufStream = null;
        File localFile;

        // 如果本地文件夹不存在，则创建
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (changeDir(destDir)) {
            for (int i = 0; i < destFileName.length; i++) {
                try {
                    if (localDir.endsWith(File.separator)) {
                        localFile = new File(localDir + destFileName[i]);
                    } else {
                        localFile = new File(localDir + File.separator + destFileName[i]);
                    }
                    outBufStream = new BufferedOutputStream(new FileOutputStream(localFile));
                    isOks[i] = ftpClient.retrieveFile(destFileName[i], outBufStream);

                } catch (IOException e) {
                    isOks[i] = false;
                } finally {
                    if (outBufStream != null) {
                        try {
                            outBufStream.flush();
                            outBufStream.close();
                        } catch (IOException e) {
                            outBufStream = null;
                        }
                    }
                }
            }
        }
        return isOks;
    }

    /**
     * 删除文件
     * 
     * @param pathname 文件路径
     * @throws RemoteAccessException ftp访问异常
     */
    public void deleteFiles(String... pathname) throws RemoteAccessException {
        if (!ftpClient.isConnected()) {
            login();
        }
        for (String fileName : pathname) {
            try {
                ftpClient.deleteFile(fileName);
            } catch (IOException e) {
                log.error("删除文件" + fileName + "失败", e);
            }
        }
    }

    /**
     * 上传文件
     * 
     * @param destDir 指定FTP目录
     * @param updateFiles 待上传文件
     * @return 上传成功返回true
     * @throws RemoteAccessException ftp访问异常
     */
    public boolean uploadFile(String destDir, File... updateFiles) throws RemoteAccessException {
        boolean isOk = false;
        BufferedInputStream inBufStream = null;
        for (File localFile : updateFiles) {
            try {
                inBufStream = new BufferedInputStream(new FileInputStream(localFile));
                ftpClient.changeWorkingDirectory(destDir);
                isOk = ftpClient.storeFile(localFile.getName(), inBufStream);
            } catch (FileNotFoundException e) {
                throw new RemoteAccessException("待上传文件不存在" + localFile.getName(), e);
            } catch (IOException e) {
                throw new RemoteAccessException("ftp上传文件失败" + localFile.getName(), e);
            } finally {
                if (inBufStream != null) {
                    try {
                        inBufStream.close();
                    } catch (IOException e) {
                        inBufStream = null;
                        e.printStackTrace();
                    }
                }
            }
        }
        return isOk;
    }

    /**
     * 切换目录
     * 
     * @param destDir 目标目录
     * @return boolean true成功
     */
    public boolean changeDir(String destDir) {
        if (destDir != null && !"".equals(destDir)) {
            if (destDir.equals(currentDir)) {
                try {
                    if (ftpClient.changeWorkingDirectory(destDir)) {
                        currentDir = destDir;
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    log.error("ftp切换目录异常", e);
                    return false;
                }
            }
        }
        return true;
    }

    public String getCharset() {
        return charset;
    }

    /**
     * 设置编码
     * 
     * @param charset 字符编码集名称
     */
    public void setCharset(String charset) {
        if (Charset.isSupported(charset)) {
            this.charset = charset;
        }
    }

    /**
     * 获取当前使用的端口号
     * 
     * @return
     */
    public int getFtpPort() {
        return ftpPort;
    }

    /**
     * 取得内部FTPClient
     * 
     * @see org.apache.commons.net.ftp.FTPClient
     * @return
     */
    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public FTPClientConfig getConfig() {
        return config;
    }

    /**
     * 设置ftp属性,在执行登录前设置
     * 
     * @param config
     */
    public void setConfig(FTPClientConfig config) {
        this.config = config;
    }

    /**
     * 取得当前所在ftp目录
     * 
     * @return
     */
    public String getCurrentDir() {
        return currentDir;
    }

    public boolean isPassiveMode() {
        return passiveMode;
    }

    /**
     * 设置是否被动模式连接
     * 
     * @param passiveMode
     */
    public void setPassiveMode(boolean passiveMode) {
        this.passiveMode = passiveMode;
    }

    public int getClientTimeout() {
        return clientTimeout;
    }

    public void setClientTimeout(int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

}
