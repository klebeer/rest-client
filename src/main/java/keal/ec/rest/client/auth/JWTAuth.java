package keal.ec.rest.client.auth;

import feign.RequestTemplate;

public class JWTAuth implements Authentication {

    private String key;
    private String headerName;
    private String username;

    public static JWTAuth get() {
        return new JWTAuth();
    }

    public JWTAuth headerApiKey(String headerName, String key) {
        this.key = key;
        this.headerName = headerName;
        return this;
    }


    public JWTAuth bodyUserApiKey(String username, String key) {
        this.username = username;
        this.key = key;
        return this;
    }

    @Override
    public void apply(RequestTemplate template) {
        if (username == null) {
            template.header(headerName, key);
        } else {
            //para pruebas
            String body = "{ \n" +
                    "   \"username\": \"" + username + "\", \n" +
                    "   \"api_key\": \"" + key + "\"\n" +
                    " }";

            template.body(body);
        }

    }

}