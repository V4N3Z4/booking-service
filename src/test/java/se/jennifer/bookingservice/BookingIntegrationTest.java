package se.jennifer.bookingservice;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import se.jennifer.bookingservice.dto.CustomerDto;
import se.jennifer.bookingservice.room.model.Room;
import se.jennifer.bookingservice.room.model.RoomType;
import se.jennifer.bookingservice.room.repository.RoomRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void shouldReturnRoomsSuccessfully() throws Exception {
        Room room = new Room("101", RoomType.SINGLE, false, 1, 800);
        roomRepository.save(room);

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateBookingSuccessfully() throws Exception {


        Room room = new Room("102", RoomType.DOUBLE, false, 2, 1200);
        room = roomRepository.save(room);

        CustomerDto mockCustomer = new CustomerDto(1L, "Jennifer", "Doe", "jenny@mail.com", "123");
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(CustomerDto.class)))
                .thenReturn(mockCustomer);

        String bookingJson = """
            {
                "customerId": 1,
                "roomId": %d,
                "startDate": "2026-12-01",
                "endDate": "2026-12-05"
            }
        """.formatted(room.getId());

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnBadRequestWhenRoomIsDoubleBooked() throws Exception {


        Room room = new Room("103", RoomType.SINGLE, false, 1, 900);
        room = roomRepository.save(room);


        CustomerDto mockCustomer = new CustomerDto(2L, "John", "Doe", "john@mail.com", "456");
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(CustomerDto.class)))
                .thenReturn(mockCustomer);


        String bookingJson = """
            {
                "customerId": 2,
                "roomId": %d,
                "startDate": "2026-12-01",
                "endDate": "2026-12-05"
            }
        """.formatted(room.getId());


        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isCreated());


        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson))
                .andExpect(status().isBadRequest());
    }
}