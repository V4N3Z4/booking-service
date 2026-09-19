package se.jennifer.bookingservice.error;

public class BadRequest extends RuntimeException{
    public BadRequest(String message){
        super(message);
    }
}
