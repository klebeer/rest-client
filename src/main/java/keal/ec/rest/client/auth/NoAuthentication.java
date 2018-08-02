package keal.ec.rest.client.auth;

import feign.RequestTemplate;

public class NoAuthentication implements Authentication {

    @Override
    public void apply(RequestTemplate template) {
        //dummy
    }
}
