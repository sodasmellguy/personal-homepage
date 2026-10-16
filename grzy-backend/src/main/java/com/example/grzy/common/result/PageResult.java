package com.example.grzy.common.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 *
 * @author sodasmell
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据
     */
    private List<T> rows;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    public PageResult() {
    }

    public PageResult(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }

    public PageResult(Long total, List<T> rows, Integer pageNum, Integer pageSize) {
        this.total = total;
        this.rows = rows;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> rows, Integer pageNum, Integer pageSize) {
        return new PageResult<>(total, rows, pageNum, pageSize);
    }
}
