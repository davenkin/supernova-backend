package com.company.andy.feature.demoreservation.query;

import com.company.andy.common.utils.PageQuery;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static lombok.AccessLevel.PRIVATE;

// All pagination queries should extends from PageQuery.

@Getter
@SuperBuilder
@NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
public class PageDemoReservationQuery extends PageQuery {
    @Schema(description = "Search text")
    @Max(50)
    private String search;
}

