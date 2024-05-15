package com.zeus.webapi.middlewares;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity;
import com.zeus.domain.services.JwtService;
import com.zeus.infrastructure.utils.CheckedFunction;
import com.zeus.models.enums.ErrorCodes;
import com.zeus.webapi.errors.HttpErrorInfo;
import com.zeus.webapi.errors.HttpErrors;

import io.jsonwebtoken.Claims;


public class Middlewares {
    public static APIGatewayProxyResponseEvent validateUserId(APIGatewayProxyRequestEvent input)
    {
        if (!input.getHeaders().containsKey("x-user-id"))
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody("{\"error\": \"header x-user-id-not-found\"}");
        return null;
    }

    public static APIGatewayProxyResponseEvent validateUserName(APIGatewayProxyRequestEvent input)
    {
        if (!input.getHeaders().containsKey("x-user-name"))
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody("{\"error\": \"header x-user-name-not-found\"}");
        return null;
    }

    public static CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception> validateJwt(JwtService jwtService) {
        return input -> {
            try {
                String token = input.getHeaders().get("Authorization").split("Bearer ")[1];
                Claims claims = jwtService.readJwt(token);
                RequestIdentity identity = new RequestIdentity();
                identity.setUser(claims.getSubject());
                input.getRequestContext().setIdentity(identity);
                return null;
            }
            catch (Exception e){
                HttpErrorInfo errorInfo = HttpErrors.CommonErrors.get(ErrorCodes.UNAUTHORIZED.toString());
                System.out.println(e.getMessage());
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(errorInfo.getHttpStatusCode())
                    .withBody(new Gson().toJson(errorInfo));
            }
        };
    }
}
