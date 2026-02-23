package edts.week8_practice1.dto.common;

/**
 * Generic pagination request DTO.
 * Use this for all paginated endpoints.
 */
public class PageRequest {

    private int page = 0;

    private int size = 10;

    public PageRequest() {
    }

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = Math.min(Math.max(1, size), 100); // Max 100 items per page
    }

    public org.springframework.data.domain.PageRequest toSpringPageRequest(SortRequest sortRequest) {
        return org.springframework.data.domain.PageRequest.of(
            page,
            size,
            sortRequest != null ? sortRequest.toSpringSort() : org.springframework.data.domain.Sort.unsorted()
        );
    }

    public org.springframework.data.domain.PageRequest toSpringPageRequest() {
        return toSpringPageRequest(null);
    }
}
