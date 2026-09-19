package se.jennifer.bookingservice.booking.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.jennifer.bookingservice.booking.model.Booking;
import se.jennifer.bookingservice.booking.model.BookingStatus;
import se.jennifer.bookingservice.booking.model.CreateBookingRequest;
import se.jennifer.bookingservice.booking.model.UpdateBookingRequest;
import se.jennifer.bookingservice.booking.repository.BookingRepository;
import se.jennifer.bookingservice.dto.CustomerDto;
import se.jennifer.bookingservice.error.BadRequest;
import se.jennifer.bookingservice.error.NotFoundException;
import se.jennifer.bookingservice.room.model.Room;
import se.jennifer.bookingservice.room.service.RoomService;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepo;
    private final RoomService roomService;
    private final RestTemplate restTemplate;

    public BookingService(BookingRepository bookingRepo, RoomService roomService, RestTemplate restTemplate) {
        this.bookingRepo = bookingRepo;
        this.roomService = roomService;
        this.restTemplate = restTemplate;
    }

    public List<Booking> getAllBookings(){
        return bookingRepo.findAll();
    }

    public Booking getBookingById(Long id){
        return bookingRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking with id " + id + " not found"));
    }

    public List<Booking> getBookingsForCustomer(Long customerId) {
        LocalDate today = LocalDate.now();
        return bookingRepo.findByCustomerIdAndStartDateAfterAndStatus(
                customerId,
                today.minusDays(1),
                BookingStatus.ACTIVE
        );
    }

    public List<Booking> getBookingsForRoom(Long roomId) {
        return bookingRepo.findByRoomId(roomId);
    }

    @Transactional
    public Booking createBooking(CreateBookingRequest request) {

        if(request.startDate().isAfter(request.endDate())){
            throw new BadRequest("Start date cannot be after end date");
        }

        Room room = roomService.getRoomById(request.roomId());

        if (isRoomBooked(room.getId(), request.startDate(), request.endDate())) {
            throw new BadRequest("Room is already booked between " + request.startDate() +  " and " + request.endDate());
        }

        //Hämtar kund via HTTP
        String url = "http://customer-service:8081/customers/" + request.customerId();
        CustomerDto customer = restTemplate.getForObject(url, CustomerDto.class);

        Booking booking = new Booking(
                request.customerId(),
                room,
                request.startDate(),
                request.endDate(),
                BookingStatus.ACTIVE
        );
        return bookingRepo.save(booking);
    }

    @Transactional
    public Booking updateBooking(Long id, UpdateBookingRequest request) {

        Booking booking = getBookingById(id);

        if(request.startDate().isAfter(request.endDate())){
            throw new BadRequest("Start date cannot be after end date");
        }

        Room room = booking.getRoom();

        if(request.roomId() != null && !request.roomId().equals(booking.getRoom().getId())) {
            room = roomService.getRoomById(request.roomId());
        }

        boolean overlaps = bookingRepo. existsByRoomIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
                room.getId(),
                BookingStatus.ACTIVE,
                request.endDate(),
                request.startDate(),
                booking.getId()
        );
        if(overlaps) {
            throw new BadRequest("Room is already booked between this period");
        }

        booking.setRoom(room);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());

        return bookingRepo.save(booking);
    }

    public boolean isRoomBooked(Long roomId, LocalDate start, LocalDate end) {
        return bookingRepo.existsByRoomIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                roomId,
                BookingStatus.ACTIVE,
                end,
                start
        );
    }

    public List<Room> getAvailableRoomsByDate(LocalDate date) {
        return roomService.getAllRooms().stream()
                .filter(room -> !isRoomBooked(room.getId(), date, date))
                .toList();
    }

    public List<Room> getAvailableRoomsByInterval(LocalDate start, LocalDate end) {
        return roomService.getAllRooms().stream()
                .filter(room -> !isRoomBooked(room.getId(), start, end))
                .toList();
    }


    public boolean roomHasActiveBookings(Long roomId) {
        return bookingRepo.existsByRoomIdAndStatus(roomId, BookingStatus.ACTIVE);
    }


    @Transactional
    public void cancelBooking(Long id){
        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequest("Booking with id " + id + " is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);
    }

}

