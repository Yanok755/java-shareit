package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemRequestMapperTest {

    @Test
    public void toResponseDto_WithItems() {

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Нужен инструмент");
        request.setCreated(LocalDateTime.of(2024, 6, 1, 12, 0));

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Дрель");
        item1.setDescription("Профессиональная");
        item1.setAvailable(true);
        item1.setRequest(request);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Шуруповерт");
        item2.setDescription("Аккумуляторный");
        item2.setAvailable(true);
        item2.setRequest(request);

        request.setItems(List.of(item1, item2));

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request);

        assertEquals(10L, dto.getId());
        assertEquals("Нужен инструмент", dto.getDescription());
        assertEquals(LocalDateTime.of(2024, 6, 1, 12, 0), dto.getCreated());
        assertEquals(2, dto.getItems().size());

        ItemForRequestDto firstItem = dto.getItems().get(0);
        assertEquals(1L, firstItem.getId());
        assertEquals("Дрель", firstItem.getName());
        assertNotNull(firstItem.getOwnerId());

        ItemForRequestDto secondItem = dto.getItems().get(1);
        assertEquals(2L, secondItem.getId());
        assertEquals("Шуруповерт", secondItem.getName());
    }

    @Test
    public void toResponseDto_WithItemsAndOwners() {

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Нужен инструмент");
        request.setCreated(LocalDateTime.of(2024, 6, 1, 12, 0));

        User owner1 = new User();
        owner1.setId(100L);
        owner1.setName("Владелец 1");

        User owner2 = new User();
        owner2.setId(200L);
        owner2.setName("Владелец 2");

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Дрель");
        item1.setDescription("Профессиональная");
        item1.setAvailable(true);
        item1.setOwner(owner1);
        item1.setRequest(request);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Шуруповерт");
        item2.setDescription("Аккумуляторный");
        item2.setAvailable(true);
        item2.setOwner(owner2);
        item2.setRequest(request);

        request.setItems(List.of(item1, item2));

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request);

        assertEquals(2, dto.getItems().size());

        ItemForRequestDto firstItem = dto.getItems().get(0);
        assertEquals(100L, firstItem.getOwnerId(), "OwnerId должен быть установлен");

        ItemForRequestDto secondItem = dto.getItems().get(1);
        assertEquals(200L, secondItem.getOwnerId(), "OwnerId должен быть установлен");
    }

    @Test
    public void toResponseDto_WithoutItems_ReturnsEmptyList() {

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Запрос");
        request.setCreated(LocalDateTime.now());
        request.setItems(null);

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request);

        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }

    @Test
    public void toResponseDto_WithEmptyItemsList() {

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Запрос");
        request.setCreated(LocalDateTime.now());
        request.setItems(List.of());

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request);

        assertTrue(dto.getItems().isEmpty());
    }

    @Test
    public void toResponseDto_WithSingleItem_ShouldMapCorrectly() {

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Нужна дрель");
        request.setCreated(LocalDateTime.of(2024, 6, 1, 12, 0));

        User owner = new User();
        owner.setId(50L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Makita");
        item.setDescription("Профессиональная дрель");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        request.setItems(List.of(item));

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request);

        assertEquals(1, dto.getItems().size());
        ItemForRequestDto itemDto = dto.getItems().get(0);
        assertEquals(1L, itemDto.getId());
        assertEquals("Makita", itemDto.getName());
        assertEquals(50L, itemDto.getOwnerId());
    }

    @Test
    public void toResponseDto_PreservesOrder() {

        ItemRequest request = new ItemRequest();
        request.setId(10L);
        request.setDescription("Запрос");
        request.setCreated(LocalDateTime.now());

        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Первый");
        item1.setRequest(request);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Второй");
        item2.setRequest(request);

        Item item3 = new Item();
        item3.setId(3L);
        item3.setName("Третий");
        item3.setRequest(request);

        request.setItems(List.of(item1, item2, item3));

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request);

        assertEquals(3, dto.getItems().size());
        assertEquals("Первый", dto.getItems().get(0).getName());
        assertEquals("Второй", dto.getItems().get(1).getName());
        assertEquals("Третий", dto.getItems().get(2).getName());
    }
}
