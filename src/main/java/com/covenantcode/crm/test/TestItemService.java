package com.covenantcode.crm.test;

import com.covenantcode.crm.exception.ResourceNotFoundException;
import com.covenantcode.crm.test.dto.TestItemMapper;
import com.covenantcode.crm.test.dto.TestItemRequest;
import com.covenantcode.crm.test.dto.TestItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestItemService {

    private final TestItemRepository repository;
    private final TestItemMapper mapper;

    public List<TestItemResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    public TestItemResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional
    public TestItemResponse create(TestItemRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Transactional
    public TestItemResponse update(Long id, TestItemRequest request) {
        TestItem item = getOrThrow(id);
        mapper.update(request, item);
        return mapper.toResponse(item);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("TestItem", id);
        }
        repository.deleteById(id);
    }

    private TestItem getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TestItem", id));
    }
}
