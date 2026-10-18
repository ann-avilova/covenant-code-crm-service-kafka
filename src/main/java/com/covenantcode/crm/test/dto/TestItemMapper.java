package com.covenantcode.crm.test.dto;

import com.covenantcode.crm.test.TestItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface TestItemMapper {

    TestItemResponse toResponse(TestItem entity);

    TestItem toEntity(TestItemRequest request);

    void update(TestItemRequest request, @MappingTarget TestItem entity);
}
