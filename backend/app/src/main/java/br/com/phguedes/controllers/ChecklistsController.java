package br.com.phguedes.controllers;

import br.com.phguedes.dto.ChecklistsDto;
import br.com.phguedes.domain.services.ChecklistsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/checklists")
@RequiredArgsConstructor
@Tag(name = "Checklists", description = "Checklists API")
public class ChecklistsController {

    private final ChecklistsService checklistsService;

    @Operation(summary = "List checklists with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de checklists retornada com sucesso")
    })
    @GetMapping("/filters")
    public ResponseEntity<List<ChecklistsDto>> list(
            @RequestParam(defaultValue = "") String bench,
            @RequestParam(defaultValue = "") String shift,
            @RequestParam(defaultValue = "") String user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate
    ) {
        if (startDate == null) {
            startDate = Instant.now().minusSeconds(30L * 24 * 60 * 60);
        }

        if (endDate == null) {
            endDate = Instant.now();
        }

        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().build();
        }

        List<ChecklistsDto> checklists = checklistsService.findAllByFilters(bench, shift, startDate, endDate, user);
        return ResponseEntity.ok(checklists);
    }

    @Operation(summary = "Find by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ChecklistsDto> getById(@PathVariable Long id) {
        ChecklistsDto dto = checklistsService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Delete by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        checklistsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Count checklists today")
    @GetMapping("/count/today")
    public ResponseEntity<Long> getTodayChecklistCount() {
        return ResponseEntity.ok(checklistsService.countChecklistsToday());
    }

    @Operation(summary = "Get last checklist")
    @GetMapping("/last")
    public ResponseEntity<ChecklistsDto> getLastChecklist() {
        return checklistsService.getLastChecklist()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get Top 3 NOK Items")
    @GetMapping("/top-nok")
    public ResponseEntity<Map<String, Long>> getTop3ItemsWithMostNok() {
        return ResponseEntity.ok(checklistsService.getTop3ItemsWithMostNok());
    }

    @Operation(summary = "Get models")
    @GetMapping("/stats/models")
    public ResponseEntity<List<Map<String, Object>>> getTopModels() {
        return ResponseEntity.ok(checklistsService.getTopModels());
    }

    @GetMapping("/stats/items-status")
    public ResponseEntity<List<Map<String, Object>>> getItemStatusStats() {
        return ResponseEntity.ok(checklistsService.getStatusCountPerItem());
    }

    @Operation(summary = "Count all checklists")
    @GetMapping("/count")
    public ResponseEntity<Long> getChecklistsCount() {
        long count = checklistsService.countChecklists();
        return ResponseEntity.ok(count);
    }


    @Operation(summary = "List all checklists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Checklists listados com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<ChecklistsDto>> listAll() {
        return ResponseEntity.ok(checklistsService.getAll());
    }

}
