package com.sl.mecm.authentication.server.controller;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.auth.intercptor.constant.AuthType;
import com.sl.mecm.auth.intercptor.service.TokenVerifyService;
import com.sl.mecm.authentication.server.service.TokenCacheService;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;
import com.sl.mecm.core.commons.exception.MECMServiceException;
import com.sl.mecm.core.commons.utils.JsonUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/token")
@Slf4j
public class TrustTokenController {

    @Autowired
    private TokenCacheService tokenCacheService;

    @Autowired
    private TokenVerifyService tokenVerifyService;

    @RequestMapping(value = "/e2e-trust/retrieve", method = RequestMethod.POST)
    public Mono<ResponseEntity<JSONObject>> retrieveSession(@RequestBody String body){
        return Mono.justOrEmpty(body)
                .doOnNext(theBody -> {
                    if (!JsonUtils.isInvalid(theBody)){
                        throw new MECMServiceException(ErrorCode.ERROR.getCode(), "request body invalid:" + theBody, "Parameter Invalid Format!", HttpStatus.BAD_REQUEST);
                    }
                })
                .map(theBody -> {
                    String sessionToken = JsonUtils.toJsonObject(theBody).getString(CommonVariables.SESSION_TOKEN);
                    if (!StringUtils.hasText(sessionToken)){
                        throw new MECMServiceException(ErrorCode.ERROR.getCode(), "lack of session token", "Parameter Missing!", HttpStatus.BAD_REQUEST);
                    }
                    return sessionToken;
                })
                .map(theSessionToken -> JSONObject.of(CommonVariables.SESSION_TOKEN, theSessionToken))
                .map(entitlement -> tokenVerifyService.generateToken(entitlement, AuthType.SESSION_AUTH))
                .map(theTokenStr -> new ResponseEntity<>(
                        JSONObject.of()
                                .fluentPut(CommonVariables.CODE, ErrorCode.SUCCESS.getCode())
                                .fluentPut(CommonVariables.MESSAGE, "success")
                                .fluentPut(CommonVariables.DATA, theTokenStr),
                        HttpStatus.OK))
                .onErrorResume(MECMServiceException.class, e -> {
                    log.error(e.getLocalizedMessage(), e);
                    return Mono.just(new ResponseEntity<>(
                            JSONObject.of()
                                    .fluentPut(CommonVariables.CODE, ErrorCode.ERROR.getCode())
                                    .fluentPut(CommonVariables.MESSAGE, e.getErrorDetail()),
                            e.getOutputStatusCode()));
                })
                .onErrorResume(e -> {
                    log.error(e.getLocalizedMessage(), e);
                    return Mono.just(new ResponseEntity<>(
                            JSONObject.of()
                                    .fluentPut(CommonVariables.CODE, ErrorCode.ERROR.getCode())
                                    .fluentPut(CommonVariables.MESSAGE, "Retrieve Token Error!"),
                            HttpStatus.SERVICE_UNAVAILABLE));
                });
    }
}
