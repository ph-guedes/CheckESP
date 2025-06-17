package br.com.phguedes.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ItemsDto {
    private int index;
    private String key;
    private String item;
    private String question;
    private List<String> options;
}
