package edts.week7_practice1.dto.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic sort request DTO.
 * Supports multiple sort fields and directions.
 */
public class SortRequest {

    private List<SortField> sorts = new ArrayList<>();

    public SortRequest() {
    }

    public SortRequest(List<SortField> sorts) {
        this.sorts = sorts != null ? sorts : new ArrayList<>();
    }

    public List<SortField> getSorts() {
        return sorts;
    }

    public void setSorts(List<SortField> sorts) {
        this.sorts = sorts != null ? sorts : new ArrayList<>();
    }

    public void addSort(String field, SortDirection direction) {
        this.sorts.add(new SortField(field, direction));
    }

    public void addSort(String field) {
        this.addSort(field, SortDirection.ASC);
    }

    public org.springframework.data.domain.Sort toSpringSort() {
        if (sorts == null || sorts.isEmpty()) {
            return org.springframework.data.domain.Sort.unsorted();
        }

        List<org.springframework.data.domain.Sort.Order> orders = new ArrayList<>();
        for (SortField sortField : sorts) {
            org.springframework.data.domain.Sort.Direction direction =
                sortField.getDirection() == SortDirection.DESC ?
                    org.springframework.data.domain.Sort.Direction.DESC :
                    org.springframework.data.domain.Sort.Direction.ASC;
            orders.add(new org.springframework.data.domain.Sort.Order(direction, sortField.getField()));
        }

        return org.springframework.data.domain.Sort.by(orders);
    }

    /**
     * Single sort field
     */
    public static class SortField {
        private String field;
        private SortDirection direction = SortDirection.ASC;

        public SortField() {
        }

        public SortField(String field, SortDirection direction) {
            this.field = field;
            this.direction = direction;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public SortDirection getDirection() {
            return direction;
        }

        public void setDirection(SortDirection direction) {
            this.direction = direction;
        }
    }

    public enum SortDirection {
        ASC, DESC
    }
}
