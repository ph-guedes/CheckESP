package br.com.phguedes.controllers;

import br.com.phguedes.domain.entities.Items;
import br.com.phguedes.domain.services.ItemsService;
import br.com.phguedes.dto.ItemsDto;
import br.com.phguedes.dto.ItemsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
@Tag(name = "Items", description = "Items API")
public class ItemsController {

    private final ItemsService itemsService;

    @Operation(summary = "List items with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de items retornada com sucesso")
    })
    @GetMapping("/filters")
    public ResponseEntity<List<ItemsDto>> list(
            @RequestParam(defaultValue = "") String item,
            @RequestParam(defaultValue = "") String key,
            @RequestParam(defaultValue = "-1") int index
    ) {
        List<ItemsDto> items = itemsService.findByFilters(item, key, index);
        return ResponseEntity.ok(items);
    }

    @Operation(summary = "Save item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PostMapping
    public ResponseEntity<ItemsDto> create(@RequestBody ItemsDto item) {
        try {
            ItemsDto created = itemsService.createItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Update item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ItemsDto> update(@PathVariable Long id, @RequestBody ItemsDto item) {
        try {
            ItemsDto updated = itemsService.updateItem(id, item);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "List all items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Itens listados com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<Items>> listAll() {
        return ResponseEntity.ok(itemsService.getAllItems());
    }

    @Operation(summary = "Find item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Items> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(itemsService.getItemById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Delete item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        try {
            itemsService.deleteItem(key);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
