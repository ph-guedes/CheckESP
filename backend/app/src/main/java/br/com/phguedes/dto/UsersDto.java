package br.com.phguedes.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UsersDto {
    private Long id;
    private String name;
    private String email;
    private String password;
}
