package br.com.phguedes.controllers;

import br.com.phguedes.domain.entities.UidLog;
import br.com.phguedes.domain.entities.UidUser;
import br.com.phguedes.domain.services.UidUserService;
import br.com.phguedes.dto.UidUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/uid-users")
@RequiredArgsConstructor
@Tag(name = "UID Users", description = "UID Users API")
public class UidUserController {

    private final UidUserService uidUserService;

    @Operation(summary = "Save UID User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "UID User criado ou atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou UID/User não encontrado")
    })
    @PostMapping
    public ResponseEntity<UidUserDto> createUidUser(@RequestParam String uid, @RequestParam Long userId) {
        try {
            UidUser uidUser = uidUserService.createOrUpdateUidUser(uid, userId);
            UidUserDto dto = uidUserService.toDto(uidUser);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "List all UidUsers")
    @GetMapping
    public ResponseEntity<?> listAll() {
        var all = uidUserService.getAllUidUsers();
        return ResponseEntity.ok(all);
    }

    @Operation(summary = "Delete UidUser by UID")
    @DeleteMapping("/{uid}")
    public ResponseEntity<Void> delete(@PathVariable String uid) {
        uidUserService.deleteUidUser(uid);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "List all logs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logs listados com sucesso")
    })
    @GetMapping("/logs")
    public ResponseEntity<List<UidLog>> listAllLogs() {
        return ResponseEntity.ok(uidUserService.getAll());
    }
}
