package se.jennifer.bookingservice.booking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.jennifer.bookingservice.dto.CustomerDto;


@Service
public class CustomerClient {

    private final RestTemplate restTemplate;
    private final String customerServiceUrl;

    public CustomerClient(RestTemplate restTemplate,
                          @Value("${customer.service.base-url}") String customerServiceUrl) {
        this.restTemplate = restTemplate;
        this.customerServiceUrl = customerServiceUrl;
    }

    public CustomerDto getCustomerById(Long customerId, String authHeader) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<CustomerDto> response = restTemplate.exchange(
                    customerServiceUrl + "/customers/" + customerId,
                    HttpMethod.GET,
                    entity,
                    CustomerDto.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new IllegalStateException("Customer service unavailable");
        }
    }
}

