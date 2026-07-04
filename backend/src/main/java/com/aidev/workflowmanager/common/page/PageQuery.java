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
            throw new BusinessException(ErrorCode.INVALID_PARAM, "pageNo must be greater than or equal to 1");
        }
        return pageNo;
    }

    public long normalizedPageSize() {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "pageSize must be between 1 and " + MAX_PAGE_SIZE);
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
