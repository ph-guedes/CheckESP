package br.com.phguedes.domain.listeners;

import br.com.phguedes.domain.entities.UidLog;
import br.com.phguedes.domain.repositories.UidLogRepository;
import com.google.firebase.database.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UidLogListener {

    private final FirebaseDatabase firebaseDatabase;
    private final UidLogRepository uidLogRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @PostConstruct
    public void init() {
        DatabaseReference ref = firebaseDatabase.getReference("uid_logs");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                saveLog(snapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                saveLog(snapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                log.error("Erro ao escutar uid_logs: {}", error.getMessage());
            }
        });

        log.info("UidLogListener registrado para escutar /uid_logs");
    }

    private void saveLog(DataSnapshot snapshot) {
        try {
            String key = snapshot.getKey();
            if (key == null || !key.contains("_")) return;

            String[] parts = key.split("_", 2);
            String uid = parts[0];
            String timestampStr = parts[1];

            LocalDateTime timestamp;
            try {
                timestamp = LocalDateTime.parse(timestampStr, FORMATTER);
            } catch (Exception e) {
                log.warn("Timestamp inválido: {}", timestampStr);
                return;
            }

            Optional<UidLog> existing = uidLogRepository.findByTimestamp(timestamp);
            if (existing.isPresent()) {
                log.info("UID log já registrado: {}", timestampStr);
                return;
            }

            String bench = snapshot.child("bancada").getValue(String.class);
            String status = snapshot.child("status").getValue(String.class);

            if (uid == null || bench == null || status == null) {
                log.warn("Dados incompletos no UID log: {}", key);
                return;
            }

            UidLog logEntry = UidLog.builder()
                    .uid(uid)
                    .bench(bench)
                    .status(status)
                    .timestamp(timestamp)
                    .build();

            uidLogRepository.save(logEntry);
            log.info("Log de UID salvo: {} - {} ({})", uid, status, timestampStr);

        } catch (Exception e) {
            log.error("Erro ao salvar UID log:", e);
        }
    }
}