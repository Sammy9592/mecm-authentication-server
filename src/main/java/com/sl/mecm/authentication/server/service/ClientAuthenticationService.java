package com.sl.mecm.authentication.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.sl.mecm.authentication.server.config.ClientCertsConfig;
import com.sl.mecm.authentication.server.config.ClientCertsConfig.ClientCert;
import com.sl.mecm.core.commons.constants.CommonVariables;
import com.sl.mecm.core.commons.exception.ErrorCode;
import com.sl.mecm.core.commons.exception.MECMServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import reactor.core.publisher.Mono;

@Service
public class ClientAuthenticationService {

    @Autowired
    private ClientCertsConfig clientCertsConfig;

    public Mono<JSONObject> checkoutClient(final String clientName, final String credential){
        ClientCert clientCert = clientCertsConfig.getCertsMap().get(clientName);
        if (clientCert == null){
            throw new MECMServiceException(ErrorCode.ERROR.getCode(),
                    "client certs info not found:" + clientName, "Client Authentication Failed!");
        }
        return Mono.just(clientCert)
                .doOnNext(theClientCert -> {
                    String inputCred = new String(Base64.getDecoder().decode(credential.getBytes(StandardCharsets.UTF_8)));
                    if (!inputCred.equals(theClientCert.getCredential())){
                        throw new MECMServiceException(ErrorCode.ERROR.getCode(),
                                "credential not match, expected:" + theClientCert.getCredential() + ", but found:" + inputCred,
                                "Client Authentication Failed!");
                    }
                })
                .map(theClientCert -> JSONObject.of()
                        .fluentPut(CommonVariables.CLIENT_ID, theClientCert.getClientId())
                        .fluentPut(CommonVariables.CLIENT_SECRET, theClientCert.getClientSecret()));
    }
}
