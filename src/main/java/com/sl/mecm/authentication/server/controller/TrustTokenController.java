package com.sl.mecm.authentication.server.controller;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.auth.intercptor.constant.AuthType;
import com.sl.mecm.authentication.server.service.TrustTokenService;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;
import com.sl.mecm.core.commons.exception.MECMServiceException;
import com.sl.mecm.core.commons.utils.JsonUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${mecm.user.base-path}")
@Slf4j
public class TrustTokenController {

    @Autowired
    private TrustTokenService trustTokenService;

    @RequestMapping(value = "/e2e-trust/session/retrieve", method = RequestMethod.POST)
    public Mono<ResponseEntity<JSONObject>> retrieveSessionToken(@RequestBody String body){
        return preCheckRequest(body)
                .map(JsonUtils::toJsonObject)
                .flatMap(theBodyObject -> trustTokenService.applyTrustToken(theBodyObject))
                .map(theTokenStr -> successResponse(ErrorCode.SUCCESS.getCode(), theTokenStr))
                .onErrorResume(MECMServiceException.class,
                        e -> {
                            log.error(e.getLocalizedMessage(), e);
                            return errorResponse(ErrorCode.ERROR.getCode(), e.getErrorDetail().toString(), e.getOutputStatusCode());
                        })
                .onErrorResume(Throwable.class,
                        e -> {
                            log.error(e.getLocalizedMessage(), e);
                            return errorResponse(ErrorCode.ERROR.getCode(), "Retrieve Session Token Error!", HttpStatus.SERVICE_UNAVAILABLE);
                        });
    }

    @RequestMapping(value = "/e2e-trust/client/retrieve", method = RequestMethod.POST)
    public Mono<ResponseEntity<JSONObject>> retrieveClientToken(@RequestBody String body){
        return preCheckRequest(body)
                .map(JsonUtils::toJsonObject)
                .flatMap(theBodyObject -> trustTokenService.applyTrustToken(theBodyObject))
                .map(theTokenStr -> successResponse(ErrorCode.SUCCESS.getCode(), theTokenStr))
                .onErrorResume(MECMServiceException.class,
                        e -> {
                            log.error(e.getLocalizedMessage(), e);
                            return errorResponse(ErrorCode.ERROR.getCode(), e.getErrorDetail().toString(), e.getOutputStatusCode());
                        })
                .onErrorResume(Throwable.class,
                        e -> {
                            log.error(e.getLocalizedMessage(), e);
                            return errorResponse(ErrorCode.ERROR.getCode(), "Retrieve client Token Error!", HttpStatus.SERVICE_UNAVAILABLE);
                        });
    }



    private ResponseEntity<JSONObject> successResponse(String code, String data){
        return new ResponseEntity<>(
                JSONObject.of()
                        .fluentPut(CommonVariables.CODE, code)
                        .fluentPut(CommonVariables.MESSAGE, "success")
                        .fluentPut(CommonVariables.DATA, data),
                HttpStatus.OK);
    }

    private Mono<ResponseEntity<JSONObject>> errorResponse(String code, String message, HttpStatusCode statusCode){
        return Mono.just(new ResponseEntity<>(
                JSONObject.of()
                        .fluentPut(CommonVariables.CODE, code)
                        .fluentPut(CommonVariables.MESSAGE, message),
                statusCode));
    }

    private Mono<String> preCheckRequest(String body){
        return Mono.justOrEmpty(body)
                .doOnNext(theBody -> {
                    if (!JsonUtils.isInvalid(theBody)){
                        throw new MECMServiceException(ErrorCode.ERROR.getCode(),
                                "request body invalid:" + theBody,
                                "Invalid Message Format!",
                                HttpStatus.BAD_REQUEST);
                    }
                })
                .doOnNext(theBody -> {
                    JSONObject bodyObject = JsonUtils.toJsonObject(theBody);
                    String authTypeStr = bodyObject.getString(CommonVariables.AUTH_TYPE);
                    if (AuthType.UNKNOWN_TYPE.equals(AuthType.typeOf(authTypeStr))){
                        throw new MECMServiceException(ErrorCode.ERROR.getCode(),
                                "request body invalid Param:" + theBody,
                                "Invalid Message With Illegal Parameter!",
                                HttpStatus.BAD_REQUEST);
                    }
                })
                .doOnNext(theBody -> {
                    JSONObject bodyObject = JsonUtils.toJsonObject(theBody);
                    JSONObject authCerts = bodyObject.getJSONObject(CommonVariables.AUTH_CERTS);
                    if (authCerts == null || authCerts.isEmpty()){
                        throw new MECMServiceException(ErrorCode.ERROR.getCode(),
                                "Lack of Parameter with body:" + theBody,
                                "Lack of Parameter!",
                                HttpStatus.BAD_REQUEST);
                    }
                });
    }
}
