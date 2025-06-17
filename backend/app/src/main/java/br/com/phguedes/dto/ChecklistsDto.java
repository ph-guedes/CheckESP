package br.com.phguedes.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ChecklistsDto {
    private Long id;
    private String bench;
    private String shift;
    private Instant dateTime;
    private String user;
    private Map<String, ChecklistItemDto> checklistItems;

    @Data
    @Builder
    public static class ChecklistItemDto {
        private String response;
    }
}
