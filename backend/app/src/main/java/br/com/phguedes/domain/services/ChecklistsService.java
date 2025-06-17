package br.com.phguedes.domain.services;

import br.com.phguedes.domain.entities.ChecklistItem;
import br.com.phguedes.domain.entities.Checklists;
import br.com.phguedes.domain.repositories.ChecklistsRepository;
import br.com.phguedes.dto.ChecklistsDto;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChecklistsService {

    private final ChecklistsRepository checklistsRepository;
    private final FirebaseDatabase firebaseDatabase;

    @Transactional(readOnly = true)
    public List<ChecklistsDto> findAllByFilters(String bench, String shift, Instant dateFrom, Instant dateTo, String userName) {

        String benchFilter = bench == null ? "" : bench;
        String shiftFilter = shift == null ? "" : shift;
        Instant from = dateFrom == null ? Instant.MIN : dateFrom;
        Instant to = dateTo == null ? Instant.MAX : dateTo;
        String userNameFilter = userName == null ? "" : userName;

        List<Checklists> checklists = checklistsRepository.findByBenchContainingIgnoreCaseAndShiftContainingIgnoreCaseAndDateTimeBetweenAndUser_NameContainingIgnoreCase(
                benchFilter, shiftFilter, from, to, userNameFilter
        );

        return checklists.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChecklistsDto> getAll() {
        List<Checklists> checklists = checklistsRepository.findAll();
        return checklists.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ChecklistsDto getById(Long id) {
        Checklists checklist = checklistsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Checklist not found with id: " + id));
        return toDto(checklist);
    }

    @Transactional
    public void deleteById(Long id) {
        Checklists existing = checklistsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Checklist not found with id: " + id));
        checklistsRepository.delete(existing);

        if (existing.getUser() != null && existing.getUser().getUidUser() != null) {
            String uid = existing.getUser().getUidUser().getUid();
            String dateTimeStr = existing.getDateTime().toString();

            DatabaseReference ref = firebaseDatabase.getReference("/checklists")
                    .child(uid)
                    .child(dateTimeStr);

            ref.removeValueAsync().addListener(() -> {
                System.out.println("Checklist removido do Firebase: UID=" + uid + " Timestamp=" + dateTimeStr);
            }, Runnable::run);
        }
    }

    @Transactional(readOnly = true)
    public Optional<ChecklistsDto> getLastChecklist() {
        return checklistsRepository.findTopByOrderByDateTimeDesc()
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public long countChecklistsToday() {
        Instant startOfDay = Instant.now().atZone(TimeZone.getDefault().toZoneId()).toLocalDate()
                .atStartOfDay(TimeZone.getDefault().toZoneId()).toInstant();
        Instant now = Instant.now();

        return checklistsRepository.countByDateTimeBetween(startOfDay, now);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStatusCountPerItem() {
        List<Checklists> checklists = checklistsRepository.findAll();
        Set<String> ignoreItems = Set.of("modelo", "voltagem");

        Map<String, Map<String, Long>> statusCountMap = checklists.stream()
                .flatMap(c -> c.getItems().stream())
                .filter(item ->
                        item.getQuestion() != null &&
                        item.getResponse() != null &&
                        !ignoreItems.contains(item.getQuestion().toLowerCase()))
                .collect(Collectors.groupingBy(
                        ChecklistItem::getQuestion,
                        Collectors.groupingBy(ChecklistItem::getResponse, Collectors.counting())
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> entry : statusCountMap.entrySet()) {
            String item = entry.getKey();
            Map<String, Long> counts = entry.getValue();

            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("item", item);
            itemMap.put("OK", counts.getOrDefault("OK", 0L));
            itemMap.put("NOK", counts.getOrDefault("NOK", 0L));
            result.add(itemMap);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopModels() {
        List<Checklists> checklists = checklistsRepository.findAll();

        Map<String, Long> modelCount = checklists.stream()
                .flatMap(c -> c.getItems().stream())
                .filter(item -> item.getQuestion().equalsIgnoreCase("MODELO"))
                .map(ChecklistItem::getResponse)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(response -> response, Collectors.counting()));

        return modelCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("modelo", entry.getKey());
                    map.put("quantidade", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getTop3ItemsWithMostNok() {
        List<Checklists> checklists = checklistsRepository.findAll();

        Map<String, Long> nokCountMap = new HashMap<>();

        for (Checklists checklist : checklists) {
            if (checklist.getItems() != null) {
                checklist.getItems().forEach(item -> {
                    if ("NOK".equalsIgnoreCase(item.getResponse())) {
                        nokCountMap.merge(item.getQuestion(), 1L, Long::sum);
                    }
                });
            }
        }

        return nokCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new
                ));
    }

    @Transactional(readOnly = true)
    public long countChecklists() {
        return checklistsRepository.count();
    }

    private ChecklistsDto toDto(Checklists checklist) {
        Map<String, ChecklistsDto.ChecklistItemDto> itemsMap = new LinkedHashMap<>();
        if (checklist.getItems() != null) {
            for (ChecklistItem item : checklist.getItems()) {
                ChecklistsDto.ChecklistItemDto dtoItem = ChecklistsDto.ChecklistItemDto.builder()
                        .response(item.getResponse())
                        .build();
                itemsMap.put(item.getQuestion(), dtoItem);
            }
        }

        return ChecklistsDto.builder()
                .id(checklist.getId())
                .bench(checklist.getBench())
                .shift(checklist.getShift())
                .dateTime(checklist.getDateTime())
                .user(checklist.getUser() != null ? checklist.getUser().getName() : null)
                .checklistItems(itemsMap)
                .build();
    }
}
