package br.com.phguedes.domain.listeners;

import br.com.phguedes.domain.entities.Checklists;
import br.com.phguedes.domain.entities.ChecklistItem;
import br.com.phguedes.domain.entities.Users;
import br.com.phguedes.domain.repositories.ChecklistsRepository;
import br.com.phguedes.domain.repositories.UsersRepository;
import com.google.firebase.database.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChecklistListener {

    private final FirebaseDatabase firebaseDatabase;
    private final UsersRepository usersRepository;
    private final ChecklistsRepository checklistsRepository;

    @PostConstruct
    public void init() {
        DatabaseReference ref = firebaseDatabase.getReference("/checklists");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                log.info("Firebase data changed (checklists).");

                for (DataSnapshot uidSnapshot : snapshot.getChildren()) {
                    String uid = uidSnapshot.getKey();

                    for (DataSnapshot timestampSnapshot : uidSnapshot.getChildren()) {
                        String timestampKey = timestampSnapshot.getKey();

                        try {
                            Checklists checklists = parseChecklist(timestampSnapshot);

                            if (checklists != null) {
                                checklistsRepository.save(checklists);
                                log.info("Checklists saved: UID={} Timestamp={}", uid, timestampKey);
                            }
                        } catch (Exception e) {
                            log.error("Failed to parse checklists for UID={} Timestamp={}: {}", uid, timestampKey, e.getMessage(), e);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                log.error("Firebase listener cancelled: {}", error.getMessage());
            }
        });
    }

    private Checklists parseChecklist(DataSnapshot snapshot) {
        String bench = snapshot.child("bancada").getValue(String.class);
        String shift = snapshot.child("turno").getValue(String.class);
        String userName = snapshot.child("operador").getValue(String.class);
        String dateStr = snapshot.getKey();

        Instant timestamp;
        try {
            timestamp = Instant.parse(dateStr);
        } catch (Exception e) {
            log.warn("Data inválida no formato ISO: {}", dateStr);
            timestamp = Instant.now();
        }

        Users user = usersRepository.findByName(userName).orElse(null);

        boolean exists = checklistsRepository.existsByBenchAndShiftAndDateTime(bench, shift, timestamp);
        if (exists) {
            log.info("Checklists já existe: bancada={}, turno={}, data={}", bench, shift, timestamp);
            return null;
        }

        Checklists checklists = Checklists.builder()
                .bench(bench)
                .shift(shift)
                .dateTime(timestamp)
                .user(user)
                .build();

        List<ChecklistItem> items = new ArrayList<>();
        for (DataSnapshot itemSnapshot : snapshot.child("itens_checklist").getChildren()) {
            String question = itemSnapshot.child("item").getValue(String.class);
            String response = itemSnapshot.child("resposta").getValue(String.class);

            if (question != null && response != null) {
                ChecklistItem item = ChecklistItem.builder()
                        .question(question)
                        .response(response)
                        .checklists(checklists)
                        .build();
                items.add(item);
            }
        }

        checklists.setItems(items);
        return checklists;
    }
}
