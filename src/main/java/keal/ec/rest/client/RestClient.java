package keal.ec.rest.client;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.netflix.config.ConfigurationManager;
import feign.Feign;
import feign.Logger;
import feign.hystrix.FallbackFactory;
import feign.hystrix.HystrixFeign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import keal.ec.rest.client.auth.Authentication;
import keal.ec.rest.client.auth.JWTToken;
import keal.ec.rest.client.auth.NoAuthentication;
import keal.ec.rest.client.error.RestClientErrorDecoder;

import java.util.Objects;
import java.util.Optional;


/**
 * Feign Rest Service client implementation, used to create easy and lightweight rest clients, including Authentication
 *
 * @author Kleber Ayala
 */
public class RestClient {

    private static final String HYSTRIX_TIME_OUT = "hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds";
    private Authentication authentication;
    private String serviceUrl;
    private Class apiClass;
    private Integer fallBackTimeout = null;


    public static RestClient get() {
        return new RestClient();
    }

    public RestClient auth(Authentication authentication) {
        this.authentication = authentication;
        return this;
    }

    /**
     * Crea un cliente de feign sin soporte para FallBack
     *
     * @param <T>
     * @return cliente de feign
     */
    public <T> T build() {
        return (T) buildFeingClient(null);
    }

    /**
     * Crea un cliente de un servicio Rest utilizando netflix hystrix y feign con soporte para un FallbackFactory
     * que se ejecutar&#x00E1; cuando se produzcan errores o  cuando la petici&#x00F3;n
     * supere el timeout definido, por defecto es 1 segundo.
     *
     * @param fallbackFactory
     * @param <T>
     * @return cliente  de feign
     */
    public <T> T build(FallbackFactory<T> fallbackFactory) {
        return (T) buildFeingClient(fallbackFactory);
    }

    /**
     * Crea un cliente de un servicio Rest utilizando netflix hystrix y feign con soporte para una funci&#x00F3;n
     * de java  que se ejecutar&#x00E1; cuando se produzcan errores o  cuando la petici&#x00F3;n
     * supere el timeout definido, por defecto es 1 segundo.
     *
     * @param fallback
     * @param <T>
     * @return cliente  de feign
     */
    public <T> T build(T fallback) {
        return buildFeingClient(fallback);
    }


    private <T> T buildFeingClient(T fallback) {

        if (Objects.isNull(authentication)) {
            authentication = new NoAuthentication();
        }
        if (fallback == null) {
            return simpleBuilder();
        } else if (fallback instanceof FallbackFactory) {
            return fallbackFactoryBuilder((FallbackFactory<T>) fallback);
        } else {
            return fallbackBuilder(fallback);
        }
    }

    private <T> T fallbackBuilder(T fallback) {
        ObjectMapper mapper = getObjectMapper();
        hystrixSetup();
        return HystrixFeign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder(mapper))
                .decoder(new JacksonDecoder(mapper))
                .logger(new Slf4jLogger(apiClass))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(authentication)
                .errorDecoder(new RestClientErrorDecoder())
                .target((Class<T>) apiClass, serviceUrl, fallback);
    }


    private <T> T fallbackFactoryBuilder(FallbackFactory<T> fallbackFactory) {
        ObjectMapper mapper = getObjectMapper();
        hystrixSetup();
        return HystrixFeign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder(mapper))
                .decoder(new JacksonDecoder(mapper))
                .logger(new Slf4jLogger(apiClass))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(authentication)
                .errorDecoder(new RestClientErrorDecoder())
                .target((Class<T>) apiClass, serviceUrl, fallbackFactory);
    }

    private <T> T simpleBuilder() {
        ObjectMapper mapper = getObjectMapper();
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder(mapper))
                .decoder(new JacksonDecoder(mapper))
                .logger(new Slf4jLogger(apiClass))
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(authentication)
                .errorDecoder(new RestClientErrorDecoder())
                .target((Class<T>) apiClass, serviceUrl);
    }

    private void hystrixSetup() {
        if (fallBackTimeout != null) {
            ConfigurationManager.getConfigInstance().setProperty(HYSTRIX_TIME_OUT, fallBackTimeout);
        }
    }

    private ObjectMapper getObjectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    private RestClient withJWTToken(JWTToken jwtToken) {
        this.authentication = jwtToken;
        return this;
    }

    public RestClient withJWTToken(Optional<String> jwtTokenOptional) {
        JWTToken jwtToken = JWTToken.build(jwtTokenOptional);
        return withJWTToken(jwtToken);
    }

    public RestClient apiClass(Class apiClass) {
        this.apiClass = apiClass;
        return this;
    }

    public RestClient url(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        return this;
    }


    public RestClient fallBackTimeout(Integer fallBackTimeout) {
        this.fallBackTimeout = fallBackTimeout;
        return this;
    }

}
