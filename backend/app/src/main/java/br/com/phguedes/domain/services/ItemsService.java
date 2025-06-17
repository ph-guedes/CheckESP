package br.com.phguedes.domain.services;

import br.com.phguedes.domain.entities.Items;
import br.com.phguedes.domain.repositories.ItemsRepository;
import br.com.phguedes.dto.ItemsDto;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class ItemsService {

    private final ItemsRepository itemsRepository;
    private final FirebaseDatabase firebaseDatabase;

    @Transactional(readOnly = true)
    public List<ItemsDto> findByFilters(String item, String key, int index) {
        String itemFilter = item == null ? "" : item;
        String keyFilter = key == null ? "" : key;

        List<Items> items;

        if (index <= -1) {
            items = itemsRepository.findByItemContainingIgnoreCaseAndKeyContainingIgnoreCase(itemFilter, keyFilter);
        } else {
            items = itemsRepository.findByItemContainingIgnoreCaseAndKeyContainingIgnoreCaseAndIndex(itemFilter, keyFilter, index);
        }

        return items.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItemsDto createItem(ItemsDto dto) {
        Items item = Items.builder()
                .index(dto.getIndex())
                .key(dto.getKey())
                .item(dto.getItem())
                .question(dto.getQuestion())
                .options(dto.getOptions())
                .build();

        syncWithFirebase(item);
        log.info("Criando novo item: {}", item.getItem());
        return toDto(itemsRepository.save(item));
    }

    @Transactional
    public ItemsDto updateItem(Long id, ItemsDto dto) {
        Items existing = this.itemsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado com id: " + id));

        deleteFromFirebase(existing.getItem());
        Items updatedItem = existing.toBuilder()
                .index(dto.getIndex() <= -1 ? dto.getIndex() : existing.getIndex())
                .key(dto.getKey() != null ? dto.getKey() : existing.getKey())
                .item(dto.getItem() != null ? dto.getItem() : existing.getItem())
                .question(dto.getQuestion() != null ? dto.getQuestion() : existing.getQuestion())
                .options(dto.getOptions() != null ? dto.getOptions() : existing.getOptions())
                .build();

        syncWithFirebase(updatedItem);
        log.info("Atualizando item: {} (id={})", updatedItem.getItem(), id);
        return toDto(itemsRepository.save(updatedItem));
    }

    @Transactional(readOnly = true)
    public Items getItemById(Long id) {
        return itemsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado com id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Items> getAllItems() {
        return itemsRepository.findAll();
    }

    @Transactional
    public void deleteItem(String key) {
        Items existing = itemsRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado com a chave: " + key));

        itemsRepository.deleteByKey(key);
        deleteFromFirebase(existing.getKey());

        log.info("Item removido: {}", existing.getId());
    }

    private void syncWithFirebase(Items item) {
        DatabaseReference ref = firebaseDatabase.getReference("itens").child(item.getKey());

        ref.child("chave_envio").setValueAsync(item.getItem());
        ref.child("indice").setValueAsync(item.getIndex());
        ref.child("pergunta").setValueAsync(item.getQuestion());

        if (item.getOptions() != null) {
            ref.child("opcoes").setValueAsync(item.getOptions());
        }
    }



    private void deleteFromFirebase(String key) {
        DatabaseReference ref = firebaseDatabase.getReference("itens/" + key);
        ref.removeValueAsync();
    }

    private ItemsDto toDto(Items item) {
        return ItemsDto.builder()
                .index(item.getIndex())
                .key(item.getKey())
                .item(item.getItem())
                .question(item.getQuestion())
                .options(item.getOptions())
                .build();
    }
}
