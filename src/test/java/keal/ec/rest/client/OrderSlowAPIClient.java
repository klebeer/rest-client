package keal.ec.rest.client;


import keal.ec.rest.test.client.model.Order;
import keal.ec.rest.test.client.model.OrderResponse;
import feign.Headers;
import feign.RequestLine;

@FunctionalInterface
public interface OrderSlowAPIClient {

    @RequestLine("POST /swloworders/")
    @Headers("Content-Type: application/json")
    OrderResponse process(Order order);
}
