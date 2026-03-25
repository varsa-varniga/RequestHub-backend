package com.varniga.requestmanagement.dto;

import lombok.Data;

/**
 * DTO for PATCH/PUT /admin/requests/{id}
 *
 * Why this exists:
 *   AdminController previously accepted a raw Request entity as @RequestBody,
 *   which broke when urgency changed from String to Urgency enum (Phase 2).
 *   This DTO accepts urgency as a plain String and lets AdminController
 *   convert it to the enum safely with a clear error message.
 */
@Data
public class UpdateRequestDto {
    private String title;
    private String description;
    private String urgency;   // "LOW" | "MEDIUM" | "HIGH"  — converted to Urgency enum in controller
}