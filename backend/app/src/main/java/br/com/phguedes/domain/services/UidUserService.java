package br.com.phguedes.domain.services;

import br.com.phguedes.dto.UidUserDto;
import br.com.phguedes.dto.UsersDto;
import br.com.phguedes.domain.entities.UidLog;
import br.com.phguedes.domain.entities.UidUser;
import br.com.phguedes.domain.entities.Users;
import br.com.phguedes.domain.repositories.UidLogRepository;
import br.com.phguedes.domain.repositories.UidUserRepository;
import br.com.phguedes.domain.repositories.UsersRepository;
import com.google.firebase.database.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UidUserService {

    private final UidUserRepository uidUserRepository;
    private final UsersRepository usersRepository;
    private final UidLogRepository uidLogRepository;
    private final FirebaseDatabase firebaseDatabase;

    @Transactional(readOnly = true)
    public List<UidLog> getAll() {
        return uidLogRepository.findAll();
    }

    @Transactional
    public UidUser createOrUpdateUidUser(String uid, Long userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com id: " + userId));

        Optional<UidLog> unknownLogs = uidLogRepository.findFirstByUidAndStatus(uid, "Desconhecido");

        if (unknownLogs.isEmpty()) {
            throw new IllegalArgumentException("Nenhum log desconhecido encontrado para UID: " + uid);
        }

        UidUser uidUser = uidUserRepository.findById(uid).orElse(
                UidUser.builder()
                        .uid(uid)
                        .name(user.getName())
                        .user(user)
                        .build()
        );

        uidUser = uidUser.toBuilder()
                .name(user.getName())
                .user(user)
                .build();

        uidUser = uidUserRepository.save(uidUser);
        updateUidUserInFirebase(uidUser);
        deleteUidLogs(uid);

        return uidUser;
    }

    @Transactional(readOnly = true)
    public List<UidUserDto> getAllUidUsers() {
        return uidUserRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UidUserDto toDto(UidUser uidUser) {
        if (uidUser == null) return null;

        return UidUserDto.builder()
                .uid(uidUser.getUid())
                .name(uidUser.getName())
                .user(toDto(uidUser.getUser()))
                .build();
    }

    public UsersDto toDto(Users user) {
        if (user == null) return null;

        return UsersDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public void deleteUidUser(String uid) {
        uidUserRepository.deleteById(uid);
        DatabaseReference uidUsersRef = firebaseDatabase.getReference("uid_usuarios");
        uidUsersRef.child(uid).removeValueAsync()
                .addListener(() -> log.info("UID {} removido do Firebase.", uid),
                        error -> log.error("Erro ao remover UID {} do Firebase: {}", uid, error));
    }

    public void deleteUidLogs(String uid) {
        uidLogRepository.deleteByUid(uid);
        DatabaseReference logsRef = firebaseDatabase.getReference("uid_logs");

        logsRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key != null && key.startsWith(uid)) {
                        logsRef.child(key).removeValueAsync()
                                .addListener(() -> log.info("Log {} removido do Firebase.", key),
                                        error -> log.error("Erro ao remover log {}: {}", key, error));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                log.error("Erro ao acessar uid_logs no Firebase: {}", error.getMessage());
            }
        });
    }

    private void updateUidUserInFirebase(UidUser uidUser) {
        DatabaseReference ref = firebaseDatabase.getReference("uid_usuarios").child(uidUser.getUid());
        ref.child("nome").setValueAsync(uidUser.getName());
    }
}
