package com.aidev.workflowmanager.common.page;

import com.aidev.workflowmanager.common.exception.BusinessException;
import com.aidev.workflowmanager.common.exception.ErrorCode;

public class PageQuery {

    public static final long DEFAULT_PAGE_NO = 1L;
    public static final long DEFAULT_PAGE_SIZE = 10L;
    public static final long MAX_PAGE_SIZE = 100L;

    private Long pageNo = DEFAULT_PAGE_NO;
    private Long pageSize = DEFAULT_PAGE_SIZE;

    public long normalizedPageNo() {
        if (pageNo == null) {
            return DEFAULT_PAGE_NO;
        }
        if (pageNo < 1) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "页码必须大于等于 1。");
        }
        return pageNo;
    }

    public long normalizedPageSize() {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "每页数量必须在 1 到 " + MAX_PAGE_SIZE + " 之间。");
        }
        return pageSize;
    }

    public Long getPageNo() {
        return pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}
