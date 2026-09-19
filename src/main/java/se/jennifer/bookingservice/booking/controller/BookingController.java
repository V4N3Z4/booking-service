package se.jennifer.bookingservice.booking.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import se.jennifer.bookingservice.booking.model.Booking;
import se.jennifer.bookingservice.booking.model.CreateBookingRequest;
import se.jennifer.bookingservice.booking.model.UpdateBookingRequest;
import se.jennifer.bookingservice.booking.service.BookingService;
import se.jennifer.bookingservice.room.model.Room;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getAllBookings(){
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id){
        return bookingService.getBookingById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<Booking> getBookingsForCustomer(@PathVariable Long customerId){
        return bookingService.getBookingsForCustomer(customerId);
    }

    @GetMapping("/customer/{customerId}/active")
    public boolean hasActiveBookings(@PathVariable Long customerId) {
        return !bookingService.getBookingsForCustomer(customerId).isEmpty();
    }


    @GetMapping("/room/{roomId}")
    public List<Booking>getBookingsForRoom(@PathVariable Long roomId){
        return bookingService.getBookingsForRoom(roomId);
    }

    @GetMapping("/available")
    public List<Room> getAvailableRoomsByDate(@RequestParam LocalDate date) {
        return bookingService.getAvailableRoomsByDate(date);
    }

    @GetMapping("/available-range")
    public List<Room> getAvailableRoomsByInterval(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return bookingService.getAvailableRoomsByInterval(start, end);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBooking(@PathVariable Long id){
        bookingService.cancelBooking(id);
    }


    @PutMapping("/{id}")
    public Booking updateBooking(@PathVariable Long id,
                                 @RequestBody UpdateBookingRequest request) {
        return bookingService.updateBooking(id, request);
    }
}

