package com.varniga.requestmanagement.controller;

import com.varniga.requestmanagement.service.SlaMonitoringService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/debug/sla")
public class DebugSlaController {

    private final SlaMonitoringService slaMonitoringService;

    public DebugSlaController(SlaMonitoringService slaMonitoringService) {
        this.slaMonitoringService = slaMonitoringService;
    }

    // 🔥 Trigger SLA for specific request
    @GetMapping("/{id}")
    public String runSlaCheck(@PathVariable Long id) {
        slaMonitoringService.monitorSla();
        return "SLA check triggered for ID: " + id;
    }

    // 🔥 Trigger full SLA scan
    @GetMapping("/run")
    public String runSlaCheckAll() {
        slaMonitoringService.monitorSla();
        return "🚀 SLA monitoring triggered successfully";
    }
}