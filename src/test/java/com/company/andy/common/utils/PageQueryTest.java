package com.company.andy.common.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.data.domain.Sort.Direction.ASC;

class PageQueryTest {

    @Test
    void should_get_pageable_for_empty_pagination_fields() {
        TestPageQuery query = TestPageQuery.builder().build();
        Pageable pageable = query.pageable();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(25, pageable.getPageSize());
        assertEquals(Sort.unsorted(), pageable.getSort());
    }

    @Test
    void should_get_pageable_for_normal_request() {
        TestPageQuery query = TestPageQuery.builder().pageNumber(1).pageSize(30).sortField("field").sortOrder(SortOrder.ASC).build();

        Pageable pageable = query.pageable();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(30, pageable.getPageSize());
        Sort.Order field1Order = pageable.getSort().getOrderFor("field");
        assertEquals(ASC, field1Order.getDirection());
    }

    @Test
    void should_get_pageable_with_default_sort_direction_of_asc() {
        TestPageQuery query = TestPageQuery.builder().pageNumber(1).pageSize(30).sortField("field").build();

        Pageable pageable = query.pageable();

        Sort.Order field1Order = pageable.getSort().getOrderFor("field");
        assertEquals(ASC, field1Order.getDirection());
    }

    @Getter
    @SuperBuilder
    @NoArgsConstructor(access = PRIVATE, onConstructor_ = @JsonCreator)
    static class TestPageQuery extends PageQuery {
    }
}