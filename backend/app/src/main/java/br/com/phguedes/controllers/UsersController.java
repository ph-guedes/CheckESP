package br.com.phguedes.controllers;

import br.com.phguedes.domain.services.UsersService;
import br.com.phguedes.dto.UsersDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("users")
@Tag(name="Users", description = "Users API")
public class UsersController {

    private final UsersService usersService;

    @Operation(summary = "Update user")
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="User updated successfully"),
            @ApiResponse(responseCode="400", description="Invalid input data"),
            @ApiResponse(responseCode="401", description="Unauthorized"),
            @ApiResponse(responseCode="500", description="Internal server error")
    })
    @PutMapping("/update?userId={id}")
    public ResponseEntity<String> updateUser(@PathVariable String id, @RequestBody UsersDto user) {
        try {
            ResponseEntity.ok(this.usersService.updateUser(Long.parseLong(id), user));
            return ResponseEntity.ok("User updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ooh something went wrong :(");
        }
    }

    //GET ALL USERS
    @Operation(summary = "List users")
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="Users listed successfully"),
            @ApiResponse(responseCode="400", description="Invalid input data"),
            @ApiResponse(responseCode="401", description="Unauthorized"),
            @ApiResponse(responseCode="500", description="Internal server error")
    })
    @GetMapping()
    public ResponseEntity<?> getAllUser(){
        try {
            return ResponseEntity.ok(this.usersService.getAllUser());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No users found");
        }
    }

    //GET SPECIFIC USER
    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="User listed successfully"),
            @ApiResponse(responseCode="400", description="Invalid input data"),
            @ApiResponse(responseCode="401", description="Unauthorized"),
            @ApiResponse(responseCode="500", description="Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(this.usersService.getUserById(Long.parseLong(id)));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUsersCount() {
        long count = usersService.countUsers();
        return ResponseEntity.ok(count);
    }

}
