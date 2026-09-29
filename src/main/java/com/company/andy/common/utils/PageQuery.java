package com.company.andy.common.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static com.company.andy.common.utils.SortOrder.ASC;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.fromString;

@Getter
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
public abstract class PageQuery {
    private static final String PROPERTY_DIRECTION_DELIMITER = ",";
    private static final int MAX_SORT_PROPERTIES = 3;
    private static final int DEFAULT_PAGE_SIZE = 25;
    private static final int DEFAULT_PAGE_NUMBER = 0;

    @Schema(description = "Zero-based page index (0..N).", defaultValue = "0")
    @Min(0)
    @Max(value = 10000)
    private int pageNumber;

    @Schema(description = "The size of the page to be returned, default is 25.", defaultValue = "25")
    @Min(0)
    @Max(value = 1000)
    private int pageSize;

    @Schema(description = "The field name to be sorted.")
    @Size(max = 50)
    private String sortField;

    @Schema(description = "The sort order for sortField.", defaultValue = "ASC")
    private SortOrder sortOrder;

    public Pageable pageable() {
        int pageNumber = this.pageNumber > 0 ? this.pageNumber : DEFAULT_PAGE_NUMBER;
        int pageSize = this.pageSize > 0 ? this.pageSize : DEFAULT_PAGE_SIZE;
        Sort sort = isNotBlank(this.sortField) ?
                Sort.by(fromString(Optional.ofNullable(this.sortOrder).orElse(ASC).name()), this.sortField) :
                Sort.unsorted();
        return PageRequest.of(pageNumber, pageSize, sort);
    }
}
