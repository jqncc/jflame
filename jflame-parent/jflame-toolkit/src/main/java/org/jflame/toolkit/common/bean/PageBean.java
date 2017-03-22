package org.jflame.toolkit.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页数据封装.
 * 
 * @author zyc CreateDate:2014年12月17日下午2:58:06
 */
public class PageBean implements Serializable {
    private static final long serialVersionUID = 2539874794729839977L;

    /**
     * 排序选项枚举.
     */
    public enum OrderEnum {
        asc, desc
    }

    // 请求的页码
    private int pageNo = 1;
    // 最大页数
    private int maxPage = 0;
    // 最大行数
    private long maxRowCount = 0;
    // 每页行数
    private int pageSize = 30;
    // 数据
    private List<?> pageData;

    private boolean isAutoCount = true;
    // 起始索引行数
    private int startIndex = 0;

    public PageBean() {
    }

    public PageBean(int rowCount, List<?> pageData) {
        setMaxRowCount(rowCount);
        this.pageData = pageData;
    }

    /**
     * 每页行数.
     * 
     * @return int
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置当前页码和每页行数.
     * 
     * @param curPage 当前页码
     * @param rowsPerPage 每页行数
     */
    public void setPageNo(final int curPage, final int rowsPerPage) {
        this.pageNo = curPage < 1 ? 1 : curPage;
        if (rowsPerPage >= 1) {
            this.pageSize = rowsPerPage;
        }
        startIndex = (pageNo - 1) * pageSize;
        // countMaxPage();
    }

    /**
     * 设置当前起始位置.
     * 
     * @param startIndex 起始位置索引
     * @param rowsPerPage 每页行数
     */
    public void setStartIndex(final int startIndex, final int rowsPerPage) {
        if (maxRowCount <= 0 || startIndex < 0) {
            this.startIndex = 0;
        }
        if (startIndex >= maxRowCount) {
            this.startIndex = (int) maxRowCount;
        }
        this.startIndex = startIndex;
        if (rowsPerPage >= 1) {
            this.pageSize = rowsPerPage;
        }
        pageNo = startIndex / pageSize + 1;
    }

    /**
     * 分页数据.
     * 
     * @return List<?>
     */
    public List<?> getPageData() {
        return pageData == null ? new ArrayList<>(0) : pageData;
    }

    public void setPageData(List<?> pageData) {
        this.pageData = pageData;
    }

    public void setMaxRowCount(long count) {
        maxRowCount = count;
        countMaxPage();
    }

    /**
     * 总行数.
     * 
     * @return long
     */
    public long getMaxRowCount() {
        return maxRowCount;
    }

    /**
     * 总页数.
     * 
     * @return int
     */
    public int getMaxPage() {
        return maxPage;
    }

    /**
     * 当前页码.
     * 
     * @return int
     */
    public int getPageNo() {
        return pageNo;
    }

    /**
     * 是否有下一页.
     */
    public boolean hasNextPage() {
        return this.getPageNo() < this.getMaxPage() - 1;
    }

    /**
     * 是否有上一页.
     */
    public boolean hasPreviousPage() {
        return this.getPageNo() > 1;
    }

    void countMaxPage() {
        if (this.maxRowCount % this.pageSize == 0) {
            this.maxPage = (int) (this.maxRowCount / this.pageSize);
        } else {
            this.maxPage = (int) (this.maxRowCount / this.pageSize + 1);
        }
    }

    public int getStartIndex() {
        return startIndex;
    }

    /**
     * 是否自动查询总数.
     * 
     * @return boolean
     */
    public boolean isAutoCount() {
        return isAutoCount;
    }

    public void setAutoCount(boolean isAutoCount) {
        this.isAutoCount = isAutoCount;
    }

}
