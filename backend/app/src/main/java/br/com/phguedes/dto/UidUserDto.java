package br.com.phguedes.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UidUserDto {
    private String uid;
    private String name;
    private UsersDto user;
}
