package com.ie.naukri.search.service;

import com.ie.naukri.rest.client.delegates.RESTServiceDelegate;
import com.ie.naukri.search.constants.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RestClient {

    @Autowired
    private RESTServiceDelegate restServiceDelegate;

    private static int timeout = 5000;
    private static int connectTimeout = 500;

    public <T> T execute(String endpoint, HttpMethod httpMethod, Object requestDTO,
                         Class<T> responseType) {
        T response;
        response = restServiceDelegate.createRequest(endpoint, httpMethod, responseType)
                .requestEntity(requestDTO).header("Content-type", "application/json")
                .appId(AppConstants.APP_ID).systemId(AppConstants.SYSTEM_ID)
                .timeout(timeout).connectTimeout(connectTimeout).submit();
        return response;
    }

    //Overloaded for systemId constrain in access URL
    public <T> T execute(String endpoint, HttpMethod httpMethod, Object requestDTO, String systemId,
                         Class<T> responseType) {
        T response;
        response = restServiceDelegate.createRequest(endpoint, httpMethod, responseType)
                .requestEntity(requestDTO).header("Content-type", "application/json")
                .appId(AppConstants.APP_ID).systemId(systemId)
                .timeout(timeout).connectTimeout(connectTimeout).submit();
        return response;
    }

    public <T> T execute(String endpoint, HttpMethod httpMethod, Object requestDTO,
                         Map<String, String> headers,
                         Class<T> responseType) {
        T response;
        response = restServiceDelegate.createRequest(endpoint, httpMethod, responseType)
                .requestEntity(requestDTO).headers(headers)
                .appId(AppConstants.APP_ID).systemId(AppConstants.SYSTEM_ID)
                .timeout(timeout).connectTimeout(connectTimeout).submit();
        return response;
    }

}
