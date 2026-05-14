package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import static org.junit.jupiter.api.Assertions.*;

public class ItemMapperTest {

    @Test
    public void toItemDto_WithRequestObject_ShouldMapRequestId() {

        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Профессиональная");
        item.setAvailable(true);

        ItemRequest request = new ItemRequest();
        request.setId(5L);
        item.setRequest(request);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(10L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("Профессиональная", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(5L, dto.getRequestId());
        assertNull(dto.getLastBooking());
    }

    @Test
    public void toItemDto_WithRequestIdField_ShouldFallbackToReadOnlyField() {

        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Профессиональная");
        item.setAvailable(true);
        item.setRequestId(5L);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(10L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("Профессиональная", dto.getDescription());
        assertTrue(dto.getAvailable());
        assertEquals(5L, dto.getRequestId());
    }

    @Test
    public void toItemDto_PriorityRequestOverRequestId() {

        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Профессиональная");
        item.setAvailable(true);

        ItemRequest request = new ItemRequest();
        request.setId(5L);
        item.setRequest(request);
        item.setRequestId(999L);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(5L, dto.getRequestId(), "Должен использоваться ID из объекта Request");
    }

    @Test
    public void toItem_FromItemDto_ShouldNotSetRequestId() {

        ItemDto dto = new ItemDto(10L, "Дрель", "Описание", false, null, null, null, 5L);

        Item item = ItemMapper.toItem(dto);

        assertEquals(10L, item.getId());
        assertEquals("Дрель", item.getName());
        assertEquals("Описание", item.getDescription());
        assertFalse(item.getAvailable());

        assertNull(item.getRequestId(), "requestId не должен копироваться из DTO");
        assertNull(item.getRequest(), "Request не должен устанавливаться в маппере");
    }

    @Test
    public void toItem_FromItemDtoWithoutRequestId_ShouldWork() {

        ItemDto dto = new ItemDto();
        dto.setId(10L);
        dto.setName("Дрель");
        dto.setDescription("Описание");
        dto.setAvailable(true);
        dto.setRequestId(null);

        Item item = ItemMapper.toItem(dto);

        assertEquals(10L, item.getId());
        assertEquals("Дрель", item.getName());
        assertEquals("Описание", item.getDescription());
        assertTrue(item.getAvailable());
        assertNull(item.getRequestId());
        assertNull(item.getRequest());
    }

    @Test
    public void toItem_FromItemDto_ShouldIgnoreRequestIdEvenIfPresent() {

        ItemDto dto = new ItemDto();
        dto.setId(10L);
        dto.setName("Дрель");
        dto.setDescription("Описание");
        dto.setAvailable(true);
        dto.setRequestId(99L);

        Item item = ItemMapper.toItem(dto);

        assertNull(item.getRequestId(), "requestId не должен копироваться из DTO");
        assertNull(item.getRequest(), "Связь с Request не должна устанавливаться в маппере");
    }

    @Test
    public void toItemUpdateDto_MapsFields() {
        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);

        ItemUpdateDto dto = ItemMapper.toItemUpdateDto(item);

        assertEquals(10L, dto.getId());
        assertEquals("Дрель", dto.getName());
        assertEquals("Описание", dto.getDescription());
        assertTrue(dto.getAvailable());
    }

    @Test
    public void toItem_FromItemUpdateDto() {
        ItemUpdateDto dto = new ItemUpdateDto(10L, "Новое имя", "Новое описание", false);

        Item item = ItemMapper.toItem(dto);

        assertEquals(10L, item.getId());
        assertEquals("Новое имя", item.getName());
        assertEquals("Новое описание", item.getDescription());
        assertFalse(item.getAvailable());
        assertNull(item.getRequestId());
    }
}
