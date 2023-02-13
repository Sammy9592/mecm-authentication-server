package com.sl.mecm.authentication.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.auth.intercptor.constant.AuthType;
import com.sl.mecm.auth.intercptor.service.TokenCreationService;
import com.sl.mecm.auth.intercptor.service.TokenVerifyService;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;
import com.sl.mecm.core.commons.exception.MECMServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.function.Consumer;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@Slf4j
public class TrustTokenService {

    @Autowired
    private TokenCreationService tokenCreationService;

    @Autowired
    private ClientAuthenticationService clientAuthenticationService;

    public Mono<String> applyTrustToken(JSONObject requestBody){
        return Mono.just(requestBody)
                .map(theRequestBody -> {
                    AuthType authType = AuthType.typeOf(theRequestBody.getString(CommonVariables.AUTH_TYPE));
                    if (AuthType.UNKNOWN_TYPE.equals(authType)){
                        throw new MECMServiceException(ErrorCode.ILLEGAL_PARAM.getCode(),
                                "invalid auth type:" + theRequestBody.getString(CommonVariables.AUTH_TYPE),
                                "Invalid Message!", HttpStatus.BAD_REQUEST);
                    }
                    return theRequestBody;
                })
                .flatMap((Function<JSONObject, Mono<String>>) theRequestBody -> {
                    AuthType authType = AuthType.typeOf(theRequestBody.getString(CommonVariables.AUTH_TYPE));
                    JSONObject authCerts = theRequestBody.getJSONObject(CommonVariables.AUTH_CERTS);
                    switch (authType) {
                        case SESSION_AUTH -> {
                            return genSessionToken(authCerts);
                        }
                        case CLIENT_AUTH -> {
                            return genClientToken(authCerts);
                        }
                        default -> throw new MECMServiceException(ErrorCode.ILLEGAL_PARAM.getCode(),
                                "invalid auth type:" + theRequestBody.getString(CommonVariables.AUTH_TYPE),
                                "Invalid Message!", HttpStatus.BAD_REQUEST);
                    }
                });
    }

    private Mono<String> genSessionToken(JSONObject authCerts){
        return Mono.just(authCerts)
                .doOnNext(theAuthCerts -> {
                    String sessionToken = theAuthCerts.getString(CommonVariables.SESSION_TOKEN);
                    String source = theAuthCerts.getString(CommonVariables.SOURCE);
                    if (!StringUtils.hasText(sessionToken) || !StringUtils.hasText(source)){
                        throw new MECMServiceException(ErrorCode.ILLEGAL_PARAM.getCode(),
                                "lack of parameters, sessionToken:" + sessionToken + ", source:" + source,
                                "Invalid Message!", HttpStatus.BAD_REQUEST);
                    }
                })
                .map(theAuthCerts -> theAuthCerts.fluentPut(CommonVariables.AUTH_TYPE, AuthType.SESSION_AUTH.getType()))
                .map(theAuthCerts -> tokenCreationService.generateToken(theAuthCerts, AuthType.SESSION_AUTH));
    }

    private Mono<String> genClientToken(JSONObject authCerts){
        String source = authCerts.getString(CommonVariables.SOURCE);
        String secret = authCerts.getString(CommonVariables.SECRET);
        if (!StringUtils.hasText(source) || !StringUtils.hasText(secret)) {
            throw new MECMServiceException(ErrorCode.ILLEGAL_PARAM.getCode(),
                    "lack of parameters, source:" + source+ ", secret:" + secret,
                    "Invalid Message!", HttpStatus.BAD_REQUEST);
        }
        return Mono.zip(Mono.just(source), Mono.just(secret))
                .flatMap(tuple2 -> clientAuthenticationService.checkoutClient(tuple2.getT1(), tuple2.getT2())
                        .map(resultCerts -> resultCerts
                                        .fluentPut(CommonVariables.SOURCE, tuple2.getT1())
                                        .fluentPut(CommonVariables.AUTH_TYPE, AuthType.CLIENT_AUTH.getType())
                        ))
                .map(theResultCerts -> tokenCreationService.generateToken(theResultCerts, AuthType.CLIENT_AUTH));
    }
}
