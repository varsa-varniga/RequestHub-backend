package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.dto.WorkflowDto;
import com.varniga.requestmanagement.dto.WorkflowResponseDto;
import com.varniga.requestmanagement.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @PostMapping
    public ResponseEntity<WorkflowResponseDto> createWorkflow(@RequestBody WorkflowDto workflowDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workflowService.createWorkflow(workflowDto));
    }

    @GetMapping
    public ResponseEntity<List<WorkflowResponseDto>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.ok("Workflow deleted successfully");
    }
    @PutMapping("/{id}")
    public ResponseEntity<WorkflowResponseDto> updateWorkflow(
            @PathVariable Long id,
            @RequestBody WorkflowDto workflowDto) {

        return ResponseEntity.ok(workflowService.updateWorkflow(id, workflowDto));
    }
}