package com.sl.mecm.authentication.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.auth.intercptor.constant.AuthType;
import com.sl.mecm.auth.intercptor.service.TokenVerifyService;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;
import com.sl.mecm.core.commons.exception.MECMServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class TokenService {

    @Autowired
    private TokenVerifyService tokenVerifyService;

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
                .map(theRequestBody -> {
                    AuthType authType = AuthType.typeOf(theRequestBody.getString(CommonVariables.AUTH_TYPE));
                    JSONObject authCerts = theRequestBody
                            .getJSONObject(CommonVariables.AUTH_CERTS)
                            .fluentPut(CommonVariables.AUTH_TYPE, authType.getType());
                    return tokenVerifyService.generateToken(authCerts, authType);
                });
    }
}
