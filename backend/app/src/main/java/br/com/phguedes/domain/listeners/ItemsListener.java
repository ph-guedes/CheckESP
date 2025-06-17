package br.com.phguedes.domain.listeners;

import br.com.phguedes.domain.entities.Items;
import br.com.phguedes.domain.repositories.ItemsRepository;
import com.google.firebase.database.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItemsListener {

    private final FirebaseDatabase firebaseDatabase;
    private final ItemsRepository itemsRepository;

    @PostConstruct
    public void init() {
        DatabaseReference ref = firebaseDatabase.getReference("itens");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                saveOrUpdate(snapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                saveOrUpdate(snapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                String key = snapshot.child("chave_envio").getValue(String.class);
                if (key != null) {
                    itemsRepository.deleteByItem(key);
                    log.info("Item removido: {}", key);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
                log.error("Erro no listener de itens: {}", error.getMessage());
            }
        });

        log.info("ItemsListener registrado para escutar /itens");
    }

    private void saveOrUpdate(DataSnapshot snapshot) {
        try {
            String question = snapshot.child("pergunta").getValue(String.class);
            String itemName = snapshot.child("chave_envio").getValue(String.class);
            Integer index = snapshot.child("indice").getValue(Integer.class);

            if (question == null || itemName == null || index == null) {
                log.warn("Item inválido ou incompleto: {}", snapshot.getKey());
                return;
            }

            List<String> options = new ArrayList<>();
            for (DataSnapshot opt : snapshot.child("opcoes").getChildren()) {
                String value = opt.getValue(String.class);
                if (value != null) options.add(value);
            }

            String key = snapshot.getKey();

            Items existing = itemsRepository.findByItem(itemName).orElse(null);

            Items itemEntity = (existing != null)
                    ? existing.toBuilder()
                    .index(index)
                    .key(key)
                    .question(question)
                    .options(options)
                    .build()
                    : Items.builder()
                    .index(index)
                    .key(key)
                    .item(itemName)
                    .question(question)
                    .options(options)
                    .build();

            itemsRepository.save(itemEntity);
            log.info("Item salvo/atualizado: {}", key);

        } catch (Exception e) {
            log.error("Erro ao salvar item do Firebase:", e);
        }
    }
}
