package keal.ec.rest.client;



import keal.ec.rest.test.client.model.Order;
import keal.ec.rest.test.client.model.OrderResponse;
import feign.Headers;
import feign.RequestLine;


public interface OrderAPIClient {

    @RequestLine("POST /order/")
    @Headers("Content-Type: application/json")
    OrderResponse process(Order order);


}
