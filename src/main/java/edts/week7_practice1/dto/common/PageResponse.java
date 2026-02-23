package edts.week7_practice1.dto.common;

import java.util.List;

/**
 * Generic paginated response DTO.
 * Use this for all paginated endpoints.
 */
public class PageResponse<T> {

    private List<T> content;
    private PageInfo pageable;

    public PageResponse() {
    }

    public PageResponse(List<T> content, org.springframework.data.domain.Page<?> page) {
        this.content = content;
        this.pageable = new PageInfo(
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.hasNext(),
            page.hasPrevious()
        );
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageInfo getPageable() {
        return pageable;
    }

    public void setPageable(PageInfo pageable) {
        this.pageable = pageable;
    }

    /**
     * Page information metadata
     */
    public static class PageInfo {
        private int pageNumber;
        private int pageSize;
        private long totalPages;
        private long totalElements;
        private boolean hasNext;
        private boolean hasPrevious;

        public PageInfo() {
        }

        public PageInfo(int pageNumber, int pageSize, long totalPages, long totalElements,
                        boolean hasNext, boolean hasPrevious) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public long getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(long totalPages) {
            this.totalPages = totalPages;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(long totalElements) {
            this.totalElements = totalElements;
        }

        public boolean isHasNext() {
            return hasNext;
        }

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }

        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }
}
