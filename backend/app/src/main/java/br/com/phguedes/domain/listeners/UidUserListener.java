package br.com.phguedes.domain.listeners;

import br.com.phguedes.domain.entities.UidUser;
import br.com.phguedes.domain.entities.Users;
import br.com.phguedes.domain.repositories.UidUserRepository;
import br.com.phguedes.domain.repositories.UsersRepository;
import com.google.firebase.database.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UidUserListener {

    private final UsersRepository usersRepository;
    private final UidUserRepository uidUserRepository;
    private final FirebaseDatabase firebaseDatabase;

    @PostConstruct
    public void init() {
        DatabaseReference ref = firebaseDatabase.getReference("uid_usuarios");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                processSnapshot(snapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                processSnapshot(snapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                String uid = snapshot.getKey();
                if (uidUserRepository.existsById(uid)) {
                    uidUserRepository.deleteById(uid);
                    log.info("UID removido do Firebase e deletado localmente: {}", uid);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                log.error("Erro ao escutar uid_usuarios: {}", error.getMessage());
            }

            private void processSnapshot(DataSnapshot snapshot) {
                String uid = snapshot.getKey();
                String nome = snapshot.child("nome").getValue(String.class);

                if (uid == null || nome == null || nome.isBlank()) {
                    log.warn("Dados inválidos recebidos: UID={} Nome={}", uid, nome);
                    return;
                }

                usersRepository.findByName(nome).ifPresentOrElse(user -> {
                    UidUser uidUser = uidUserRepository.findById(uid)
                            .map(existing -> existing.toBuilder()
                                    .name(nome)
                                    .user(user)
                                    .build())
                            .orElse(UidUser.builder()
                                    .uid(uid)
                                    .name(nome)
                                    .user(user)
                                    .build());

                    uidUserRepository.save(uidUser);
                    log.info("UID '{}' vinculado ao usuário '{}'", uid, nome);

                }, () -> log.warn("Usuário com nome '{}' não encontrado para UID {}", nome, uid));
            }
        });

        log.info("Listener de UID User inicializado.");
    }
}
