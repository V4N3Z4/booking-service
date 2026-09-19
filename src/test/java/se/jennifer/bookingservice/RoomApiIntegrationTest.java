package se.jennifer.bookingservice;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import se.jennifer.bookingservice.room.model.Room;
import se.jennifer.bookingservice.room.model.RoomType;
import se.jennifer.bookingservice.room.repository.RoomRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoomApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void shouldReturnRooms() throws Exception {

        Room room = new Room("101", RoomType.SINGLE, false, 1, 800);
        roomRepository.save(room);

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk());
    }
}