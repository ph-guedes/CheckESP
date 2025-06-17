package br.com.phguedes.controllers;

import br.com.phguedes.domain.services.UsersService;
import br.com.phguedes.dto.AuthDto;
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
@RequestMapping("auth")
@Tag(name="Auth", description = "Auth API")
public class AuthController {

    private final UsersService usersService;

    //POST
    @Operation(summary = "Save user")
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="User created successfully"),
            @ApiResponse(responseCode="400", description="Invalid input data"),
            @ApiResponse(responseCode="500", description="Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<String> createUser(@RequestBody UsersDto user){
        try {
            this.usersService.createUser(user);
            return ResponseEntity.ok("User created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ooh something went wrong :(");
        }
    }

    //AUTH
    @Operation(summary = "Auth user")
    @ApiResponses(value = {
            @ApiResponse(responseCode="200", description="User authed successfully"),
            @ApiResponse(responseCode="400", description="Invalid input data"),
            @ApiResponse(responseCode="500", description="Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<?> auth(@RequestBody AuthDto user) {

        try {
            return ResponseEntity.ok(this.usersService.auth(user));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ooh something went wrong :(");
        }
    }
}
