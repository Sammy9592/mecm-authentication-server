package com.sl.mecm.authentication.server.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.authentication.server.service.TokenCacheHandler;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;

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
@RequestMapping("${mecm.user.base-path}")
@Slf4j
public class AccessTokenController {

    @Autowired
    private TokenCacheHandler tokenCacheHandler;

    @RequestMapping(value = "/access-token/retrieve", method = RequestMethod.POST)
    public Mono<ResponseEntity<JSONObject>> retrieveSessionToken(@RequestBody String body){
        return Mono.just(body)
                .doOnNext(theBody -> log.info("receive query session token request:" + theBody))
                .map(JSON::parseObject)
                .map(jsonObject -> jsonObject.getString("dataKey"))
                .map(accessToken -> {
                    String sessionToken = tokenCacheHandler.getSessionToken(accessToken);
                    ResponseEntity<JSONObject> responseEntity;
                    if (StringUtils.hasText(sessionToken)){
                        responseEntity = new ResponseEntity<>(
                                JSONObject.of()
                                        .fluentPut(CommonVariables.CODE, ErrorCode.SUCCESS.getCode())
                                        .fluentPut(CommonVariables.MESSAGE, "success")
                                        .fluentPut(CommonVariables.DATA, sessionToken),
                                HttpStatus.OK);
                    }else {
                        responseEntity = new ResponseEntity<>(
                                JSONObject.of()
                                        .fluentPut(CommonVariables.CODE, ErrorCode.ILLEGAL_PARAM.getCode())
                                        .fluentPut(CommonVariables.MESSAGE, "session token not found")
                                        .fluentPut(CommonVariables.DATA, sessionToken),
                                HttpStatus.OK);
                    }
                    return responseEntity;
                })
                .onErrorResume(throwable -> {
                    log.error("error to query session token from cache: " + throwable.getLocalizedMessage(), throwable);
                    return Mono.just(new ResponseEntity<>(
                            JSONObject.of()
                                    .fluentPut(CommonVariables.CODE, ErrorCode.ERROR.getCode())
                                    .fluentPut(CommonVariables.MESSAGE, throwable.getLocalizedMessage()),
                            HttpStatus.SERVICE_UNAVAILABLE));
                });
    }

    @RequestMapping(value = "/access-token/save", method = RequestMethod.POST)
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
                        String accessToken = bodyObject.getString(CommonVariables.DATA_KEY);
                        String sessionToken = bodyObject.getString(CommonVariables.DATA);
                        tokenCacheHandler.saveSessionToken(accessToken, sessionToken);
                        return new ResponseEntity<>(
                                JSONObject.of()
                                        .fluentPut(CommonVariables.CODE, ErrorCode.SUCCESS.getCode())
                                        .fluentPut(CommonVariables.MESSAGE, "success")
                                        .fluentPut(CommonVariables.DATA, sessionToken),
                                HttpStatus.OK);
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("error to save session token into cache: " + throwable.getLocalizedMessage(), throwable);
                    return Mono.just(new ResponseEntity<>(
                            JSONObject.of()
                                    .fluentPut(CommonVariables.CODE, ErrorCode.ERROR.getCode())
                                    .fluentPut(CommonVariables.MESSAGE, throwable.getLocalizedMessage()),
                            HttpStatus.SERVICE_UNAVAILABLE));
                });
    }
}
