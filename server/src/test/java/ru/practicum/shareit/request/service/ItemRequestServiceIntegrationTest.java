package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Test
    public void createRequest_Success() {
        UserDto user = userService.addUser(new UserDto(null, "Test User", "test@test.com"));
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");

        ItemRequestResponseDto response = requestService.createRequest(user.getId(), createDto);

        assertNotNull(response.getId());
        assertEquals("Нужна дрель", response.getDescription());
        assertNotNull(response.getCreated());
        assertNotNull(response.getItems());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    public void getUserRequests_WithAndWithoutRequests() {
        UserDto user1 = userService.addUser(new UserDto(null, "User1", "user1@test.com"));
        UserDto user2 = userService.addUser(new UserDto(null, "User2", "user2@test.com"));

        requestService.createRequest(user1.getId(), new ItemRequestCreateDto("Request 1"));
        requestService.createRequest(user1.getId(), new ItemRequestCreateDto("Request 2"));

        var user1Requests = requestService.getUserRequests(user1.getId());
        var user2Requests = requestService.getUserRequests(user2.getId());

        assertEquals(2, user1Requests.size());
        assertEquals(0, user2Requests.size());
    }

    @Test
    public void getUserRequests_WithItems_ShouldIncludeItems() {

        UserDto requester = userService.addUser(new UserDto(null, "Requester", "requester@test.com"));
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));

        var request = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Нужна профессиональная дрель"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Дрель Makita", "Профессиональная", true, null, null, List.of(), request.getId()));

        var userRequests = requestService.getUserRequests(requester.getId());

        assertEquals(1, userRequests.size());
        ItemRequestResponseDto response = userRequests.get(0);
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
        assertEquals(item.getId(), response.getItems().get(0).getId());
        assertEquals(item.getName(), response.getItems().get(0).getName());
        assertEquals(owner.getId(), response.getItems().get(0).getOwnerId());
    }

    @Test
    public void getUserRequests_MultipleItemsForOneRequest_ShouldReturnAllItems() {

        UserDto requester = userService.addUser(new UserDto(null, "Requester", "requester@test.com"));
        UserDto owner1 = userService.addUser(new UserDto(null, "Owner1", "owner1@test.com"));
        UserDto owner2 = userService.addUser(new UserDto(null, "Owner2", "owner2@test.com"));

        var request = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Нужны инструменты"));

        ItemDto item1 = itemService.addItem(owner1.getId(),
                new ItemDto(null, "Дрель", "Электрическая", true, null, null, List.of(), request.getId()));
        ItemDto item2 = itemService.addItem(owner2.getId(),
                new ItemDto(null, "Шуруповерт", "Аккумуляторный", true, null, null, List.of(), request.getId()));

        var userRequests = requestService.getUserRequests(requester.getId());

        assertEquals(1, userRequests.size());
        ItemRequestResponseDto response = userRequests.get(0);
        assertEquals(2, response.getItems().size());

        boolean hasDrill = response.getItems().stream().anyMatch(i -> i.getName().equals("Дрель"));
        boolean hasScrewdriver = response.getItems().stream().anyMatch(i -> i.getName().equals("Шуруповерт"));
        assertTrue(hasDrill && hasScrewdriver);
    }

    @Test
    public void getAllRequests_ExcludesOwnRequests() {
        UserDto user1 = userService.addUser(new UserDto(null, "User1", "user1@test.com"));
        UserDto user2 = userService.addUser(new UserDto(null, "User2", "user2@test.com"));

        requestService.createRequest(user1.getId(), new ItemRequestCreateDto("Req from 1"));
        requestService.createRequest(user2.getId(), new ItemRequestCreateDto("Req from 2"));

        var allForUser1 = requestService.getAllRequests(user1.getId());
        assertEquals(1, allForUser1.size());
        assertEquals("Req from 2", allForUser1.get(0).getDescription());
    }

    @Test
    public void getAllRequests_WithItems_ShouldIncludeItems() {

        UserDto requester1 = userService.addUser(new UserDto(null, "Requester1", "req1@test.com"));
        UserDto requester2 = userService.addUser(new UserDto(null, "Requester2", "req2@test.com"));
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));

        var request1 = requestService.createRequest(requester1.getId(), 
                new ItemRequestCreateDto("Нужна дрель"));

        var request2 = requestService.createRequest(requester2.getId(), 
                new ItemRequestCreateDto("Нужен шуруповерт"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Шуруповерт Bosch", "Профессиональный", true, null, null, List.of(), request2.getId()));

        var allRequests = requestService.getAllRequests(requester1.getId());

        assertEquals(1, allRequests.size());
        ItemRequestResponseDto response = allRequests.get(0);
        assertEquals("Нужен шуруповерт", response.getDescription());
        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
        assertEquals("Шуруповерт Bosch", response.getItems().get(0).getName());
    }

    @Test
    public void getRequestById_WithItems_ShouldReturnItems() {

        UserDto requester = userService.addUser(new UserDto(null, "Requester", "requester@test.com"));
        UserDto owner = userService.addUser(new UserDto(null, "Owner", "owner@test.com"));

        var request = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Нужна качественная дрель"));

        ItemDto item = itemService.addItem(owner.getId(),
                new ItemDto(null, "Dewalt", "Профессиональная дрель", true, null, null, List.of(), request.getId()));

        var foundRequest = requestService.getRequestById(requester.getId(), request.getId());

        assertNotNull(foundRequest);
        assertEquals(request.getId(), foundRequest.getId());
        assertEquals("Нужна качественная дрель", foundRequest.getDescription());
        assertNotNull(foundRequest.getItems());
        assertEquals(1, foundRequest.getItems().size());
        assertEquals(item.getId(), foundRequest.getItems().get(0).getId());
        assertEquals(item.getName(), foundRequest.getItems().get(0).getName());
        assertEquals(owner.getId(), foundRequest.getItems().get(0).getOwnerId());
    }

    @Test
    public void getRequestById_WithoutItems_ShouldReturnEmptyList() {

        UserDto requester = userService.addUser(new UserDto(null, "Requester", "requester@test.com"));

        var request = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Запрос без ответов"));

        var foundRequest = requestService.getRequestById(requester.getId(), request.getId());

        assertNotNull(foundRequest);
        assertNotNull(foundRequest.getItems());
        assertTrue(foundRequest.getItems().isEmpty());
    }

    @Test
    public void getUserRequests_OrderedByCreatedDesc() {

        UserDto requester = userService.addUser(new UserDto(null, "Requester", "requester@test.com"));

        var request1 = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Первый запрос"));

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        var request2 = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Второй запрос"));

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        var request3 = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Третий запрос"));

        var userRequests = requestService.getUserRequests(requester.getId());

        assertEquals(3, userRequests.size());
        assertEquals("Третий запрос", userRequests.get(0).getDescription());
        assertEquals("Второй запрос", userRequests.get(1).getDescription());
        assertEquals("Первый запрос", userRequests.get(2).getDescription());
    }

    @Test
    public void createRequest_WithSameRequester_MultipleRequests() {

        UserDto requester = userService.addUser(new UserDto(null, "Requester", "requester@test.com"));

        var request1 = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Первый запрос"));
        var request2 = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Второй запрос"));
        var request3 = requestService.createRequest(requester.getId(), 
                new ItemRequestCreateDto("Третий запрос"));

        assertNotNull(request1.getId());
        assertNotNull(request2.getId());
        assertNotNull(request3.getId());

        var allRequests = requestService.getUserRequests(requester.getId());
        assertEquals(3, allRequests.size());
    }
}
