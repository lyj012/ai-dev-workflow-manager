package com.aidev.workflowmanager.common.page;

import java.util.List;

public class PageResult<T> {

    private final List<T> records;
    private final long total;
    private final long pageNo;
    private final long pageSize;

    public PageResult(List<T> records, long total, long pageNo, long pageSize) {
        this.records = records;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public long getTotal() {
        return total;
    }

    public long getPageNo() {
        return pageNo;
    }

    public long getPageSize() {
        return pageSize;
    }
}
