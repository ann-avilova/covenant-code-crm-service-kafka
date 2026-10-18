package com.covenantcode.crm.test;

import com.covenantcode.crm.test.dto.TestItemRequest;
import com.covenantcode.crm.test.dto.TestItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
@Tag(name = "Test CRUD", description = "Temporary endpoint for deployment verification")
public class TestItemController {

    private final TestItemService service;

    @GetMapping
    @Operation(summary = "Get all test items")
    public List<TestItemResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get test item by id")
    public TestItemResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create test item")
    public TestItemResponse create(@Valid @RequestBody TestItemRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update test item")
    public TestItemResponse update(@PathVariable Long id, @Valid @RequestBody TestItemRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete test item")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
