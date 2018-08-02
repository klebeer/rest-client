
package keal.ec.rest.client;


import keal.ec.rest.client.auth.JWTAuth;
import keal.ec.rest.client.auth.JWTClient;
import keal.ec.rest.client.error.RestClientException;

import keal.ec.rest.test.client.JWTRestApplicationDummy;
import keal.ec.rest.test.client.model.Order;
import keal.ec.rest.test.client.model.OrderResponse;
import feign.FeignException;
import feign.hystrix.FallbackFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = JWTRestApplicationDummy.class)
public class RestClientTest {

    @LocalServerPort
    private Integer randomPort;

    private Optional<String> jwtToken;

    @Before
    public void setUp() {
        JWTAuth jwtAuth = JWTAuth.get().
                headerApiKey("api", "601f1889667efaebb33b8c12572835da3f027f78");

        JWTClient jwtClient = RestClient.get().
                auth(jwtAuth).
                apiClass(JWTClient.class).
                url("http://localhost:".concat(randomPort.toString())).
                build();

        jwtToken = jwtClient.getToken();
        Assert.assertNotNull(jwtToken);
    }

    @Test
    public void orderAPIJWTAuthTest() {


        OrderAPIClient orderAPIClient = RestClient.
                get().
                withJWTToken(jwtToken).
                apiClass(OrderAPIClient.class).
                url("http://localhost:".
                        concat(randomPort.toString()).
                        concat("/api/")).
                build();

        OrderResponse orderResponse = orderAPIClient.process(new Order());
        Assert.assertNotNull(orderResponse);
        Assert.assertEquals("Todo Bien :)", orderResponse.getReturnCodeDesc());
    }


    @Test
    public void orderAPIJWTAuthBadAPIKeyTest() {


        JWTAuth jwtAuth = JWTAuth.get().
                headerApiKey("api", "1234567890");

        JWTClient jwtClient = RestClient.get().
                auth(jwtAuth).
                apiClass(JWTClient.class).
                url("http://localhost:".concat(randomPort.toString())).
                build();


        Optional<String> jwtToken = jwtClient.getToken();
        Assert.assertNotNull(jwtToken);
        Assert.assertEquals("ND", jwtToken.get());

        OrderAPIClient orderAPIClient = RestClient.
                get().
                withJWTToken(jwtToken).
                apiClass(OrderAPIClient.class).
                url("http://localhost:".
                        concat(randomPort.toString()).
                        concat("/api/")).
                build();


        try {

            OrderResponse orderResponse = orderAPIClient.process(new Order());
        } catch (RestClientException error) {
            Assert.assertEquals(403, error.status());
            String message = "\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access Denied\",\"path\":\"/api/order/\"}";
            assertThat(error.getMessage(), containsString(message));
        }


    }

    @Test
    public void orderAPIJWTAuthFallBackAPIKeyTest() {


        JWTAuth jwtAuth = JWTAuth.get().
                headerApiKey("api", "1234567890");

        JWTClient jwtClient = RestClient.get().
                auth(jwtAuth).
                apiClass(JWTClient.class).
                url("http://localhost:".concat(randomPort.toString())).
                build();


        OrderAPIClient fallback = (order) -> {

            OrderResponse fallBackResponse = new OrderResponse();
            fallBackResponse.returnCode(-1);
            return fallBackResponse;

        };

        Optional<String> jwtToken = jwtClient.getToken();
        Assert.assertNotNull(jwtToken);
        Assert.assertEquals("ND", jwtToken.get());

        OrderAPIClient orderAPIClient = RestClient.
                get().
                withJWTToken(jwtToken).
                apiClass(OrderAPIClient.class).
                url("http://localhost:".
                        concat(randomPort.toString()).
                        concat("/api/")).
                build(fallback);


        OrderResponse orderResponse = orderAPIClient.process(new Order());

        Assert.assertNotNull(orderResponse);
        Assert.assertEquals(new Integer(-1), orderResponse.getReturnCode());


    }


    @Test
    public void orderAPIJWTAuthFallBackTimeOutAPIKeyTest() {


        OrderSlowAPIClient fallback = (order) -> {
            OrderResponse fallBackResponse = new OrderResponse();
            fallBackResponse.returnCode(-1);
            return fallBackResponse;

        };

        OrderSlowAPIClient orderAPIClient = RestClient.
                get().
                withJWTToken(jwtToken).
                apiClass(OrderSlowAPIClient.class).
                url("http://localhost:".
                        concat(randomPort.toString()).
                        concat("/api/")).
                fallBackTimeout(5000).
                build(fallback);

        OrderResponse orderResponse = orderAPIClient.process(new Order());
        Assert.assertNotNull(orderResponse);
        Assert.assertEquals("Todo Bien :)", orderResponse.getReturnCodeDesc());

    }

    @Test
    public void orderAPIJWTAuthFallBackFactoryAPIKeyTest() {

        String message = "\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access Denied\",\"path\":\"/api/order/\"}";

        JWTAuth jwtAuth = JWTAuth.get().
                headerApiKey("api", "1234567890");

        JWTClient jwtClient = RestClient.get().
                auth(jwtAuth).
                apiClass(JWTClient.class).
                url("http://localhost:".concat(randomPort.toString())).
                build();


        FallbackFactory<OrderAPIClient> fallbackFactory = cause -> (order) -> {
            if (cause instanceof RestClientException && ((RestClientException) cause).status() == 403) {
                OrderResponse fallBackResponse = new OrderResponse();
                fallBackResponse.returnCodeDesc(cause.getMessage());
                return fallBackResponse;
            } else {
                return new OrderResponse();
            }
        };



        Optional<String> jwtToken = jwtClient.getToken();
        Assert.assertNotNull(jwtToken);
        Assert.assertEquals("ND", jwtToken.get());

        OrderAPIClient orderAPIClient = RestClient.
                get().
                withJWTToken(jwtToken).
                apiClass(OrderAPIClient.class).
                url("http://localhost:".
                        concat(randomPort.toString()).
                        concat("/api/")).
                build(fallbackFactory);

        OrderResponse orderResponse = orderAPIClient.process(new Order());

        Assert.assertNotNull(orderResponse);
        assertThat(orderResponse.getReturnCodeDesc(), containsString(message));


    }
}
