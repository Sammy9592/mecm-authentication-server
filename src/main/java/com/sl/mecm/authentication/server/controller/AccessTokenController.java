package com.sl.mecm.service.cache.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.service.cache.service.TokenCacheService;
import com.sl.mecm.service.cache.service.UserSessionCacheService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/cache")
@Slf4j
public class CacheController {

    @Autowired
    private UserSessionCacheService userSessionCacheService;

    @Autowired
    private TokenCacheService tokenCacheService;

    @RequestMapping(value = "/session/query", method = RequestMethod.POST)
    public Mono<ResponseEntity<JSONObject>> retrieveSession(@RequestBody String body){
        return Mono.just(body)
                .doOnNext(theBody -> log.info("receive query session request:" + theBody))
                .map(JSON::parseObject)
                .map(jsonObject -> jsonObject.getString("dataKey"))
                .map(dataKey -> {
                    JSONObject sessionObject = userSessionCacheService.queryInCache(dataKey);
                    ResponseEntity<JSONObject> responseEntity;
                    if (sessionObject == null){
                        responseEntity = new ResponseEntity<>(JSONObject.of("code", "400", "message", "session not found"), HttpStatus.OK);
                    }else {
                        responseEntity = new ResponseEntity<>(JSONObject.of("code", "200", "message", "success", "data", sessionObject), HttpStatus.OK);
                    }
                    return responseEntity;
                })
                .onErrorResume(throwable -> {
                    log.error("error to query session from cache: " + throwable.getLocalizedMessage(), throwable);
                    return Mono.just(new ResponseEntity<>(JSONObject.of("code", "503", "message", throwable.getLocalizedMessage()), HttpStatus.SERVICE_UNAVAILABLE));
                });
    }

    @RequestMapping(value = "/session/save", method = RequestMethod.POST)
    public Mono<ResponseEntity<JSONObject>> saveSession(@RequestBody String body){
        return Mono.just(body)
                .doOnNext(theBody -> log.info("receive save session request:" + theBody))
                .map(JSON::parseObject)
                .map(bodyObject -> {
                    String key = bodyObject.getString("dataKey");
                    JSONObject data = bodyObject.getJSONObject("dataObject");
                    userSessionCacheService.saveInCache(key, data);
                    return new ResponseEntity<>(JSONObject.of("code", "200", "message", "success"), HttpStatus.OK);
                })
                .onErrorResume(throwable -> {
                    log.error("error to save session into cache: " + throwable.getLocalizedMessage(), throwable);
                    return Mono.just(new ResponseEntity<>(
                            JSONObject.of("code", "503", "message", throwable.getLocalizedMessage()),
                            HttpStatus.SERVICE_UNAVAILABLE));
                });
    }

    @RequestMapping(value = "/session/token/query", method = RequestMethod.POST)
    public Mono<ResponseEntity<JSONObject>> retrieveSessionToken(@RequestBody String body){
        return Mono.just(body)
                .doOnNext(theBody -> log.info("receive query session token request:" + theBody))
                .map(JSON::parseObject)
                .map(jsonObject -> jsonObject.getString("dataKey"))
                .map(accessToken -> {
                    String sessionToken = tokenCacheService.getSessionToken(accessToken);
                    ResponseEntity<JSONObject> responseEntity;
                    if (StringUtils.hasText(sessionToken)){
                        responseEntity = new ResponseEntity<>(JSONObject.of("code", "200", "message", "success", "data", sessionToken), HttpStatus.OK);
                    }else {
                        responseEntity = new ResponseEntity<>(JSONObject.of("code", "400", "message", "session token not found"), HttpStatus.OK);
                    }
                    return responseEntity;
                })
                .onErrorResume(throwable -> {
                    log.error("error to query session token from cache: " + throwable.getLocalizedMessage(), throwable);
                    return Mono.just(new ResponseEntity<>(JSONObject.of("code", "503", "message", throwable.getLocalizedMessage()), HttpStatus.SERVICE_UNAVAILABLE));
                });
    }

    @RequestMapping(value = "/session/token/save", method = RequestMethod.POST)
    public Mono<ResponseEntity<JSONObject>> saveSessionToken(@RequestBody String body){
        return Mono.just(body)
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String theBody) {
                        log.info("receive save session token request:" + theBody);
                    }
                })
                .map(JSON::parseObject)
                .map(new Function<JSONObject, ResponseEntity<JSONObject>>() {
                    @Override
                    public ResponseEntity<JSONObject> apply(JSONObject bodyObject) {
                        String accessToken = bodyObject.getString("dataKey");
                        String sessionToken = bodyObject.getString("data");
                        tokenCacheService.saveSessionToken(accessToken, sessionToken);
                        return new ResponseEntity<>(JSONObject.of("code", "200", "message", "success"), HttpStatus.OK);
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("error to save session token into cache: " + throwable.getLocalizedMessage(), throwable);
                    return Mono.just(new ResponseEntity<>(
                            JSONObject.of("code", "503", "message", throwable.getLocalizedMessage()),
                            HttpStatus.SERVICE_UNAVAILABLE));
                });
    }
}
