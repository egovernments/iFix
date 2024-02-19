package org.digit.program.controller;

import org.digit.program.models.disburse.DisburseSearchRequest;
import org.digit.program.models.disburse.DisburseSearchResponse;
import org.digit.program.models.disburse.DisbursementRequest;
import org.digit.program.service.DisburseService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Controller
@RequestMapping("/v1")
public class DisburseController {

    private final DisburseService disburseService;

    public DisburseController(DisburseService disburseService) {
        this.disburseService = disburseService;
    }

    @PostMapping(value = "/disburse/_create")
    public ResponseEntity<DisbursementRequest> createDisburse(@RequestBody @Valid DisbursementRequest disbursementRequest) {
        return ResponseEntity.ok(disburseService.createDisburse(disbursementRequest, "create"));
    }

    @PostMapping(value = "/disburse/_update")
    public ResponseEntity<DisbursementRequest> updateDisburse(@RequestBody @Valid DisbursementRequest disbursementRequest) {
        return ResponseEntity.ok(disburseService.updateDisburse(disbursementRequest, "update"));
    }

    @PostMapping(value = "/disburse/_search")
    public ResponseEntity<DisburseSearchResponse> searchDisburse(@RequestBody @Valid DisburseSearchRequest disburseSearchRequest) {
        return ResponseEntity.ok(disburseService.searchDisburse(disburseSearchRequest, "search"));

    }

    @PostMapping(value = "/on-disburse/_create")
    public ResponseEntity<DisbursementRequest> onDisburseCreate(@RequestBody @Valid DisbursementRequest disbursementRequest) {
        return ResponseEntity.ok(disburseService.onDisburseCreate(disbursementRequest, "create"));
    }

    @PostMapping(value = "/on-disburse/_update")
    public ResponseEntity<DisbursementRequest> onDisburseUpdate(@RequestBody @Valid DisbursementRequest disbursementRequest) {
        return ResponseEntity.ok(disburseService.onDisburseUpdate(disbursementRequest, "update"));
    }


}