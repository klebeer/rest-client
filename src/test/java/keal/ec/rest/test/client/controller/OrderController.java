package keal.ec.rest.test.client.controller;



import keal.ec.rest.test.client.model.Order;
import keal.ec.rest.test.client.model.OrderResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {


    @RequestMapping(value = "/order/", method = RequestMethod.POST, produces = "application/json")
    public OrderResponse processOrder(Order order) {
        OrderResponse response = new OrderResponse().returnCode(1).returnCodeDesc("Todo Bien :)");

        return response;
    }

    @RequestMapping(value = "/swloworders/", method = RequestMethod.POST, produces = "application/json")
    public OrderResponse processSlowOrder(Order order) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        OrderResponse response = new OrderResponse().returnCode(1).returnCodeDesc("Todo Bien :)");

        return response;
    }
}
