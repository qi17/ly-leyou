package com.leyou.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 对前端返回的复杂数据类型进行封装，便于进行分页操作
 * viewObject
 * @param <T>
 */
@Data
public class PageResult<T> {
    private Long total;//总条数
    private Integer totalPage;//总页数
    private List<T> items;//总条目

    public PageResult() {
    }

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public PageResult(Long total, Integer totalPage, List<T> items) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }

    public PageResult(Long total, Integer totalPage) {
        this.total = total;
        this.totalPage = totalPage;
    }

    public PageResult(Long total) {
        this.total = total;
    }
}
