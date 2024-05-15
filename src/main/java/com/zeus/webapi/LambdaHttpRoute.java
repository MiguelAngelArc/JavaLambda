package com.zeus.webapi;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.zeus.infrastructure.utils.CheckedFunction;

import java.util.List;

public class LambdaHttpRoute {
    private String httpMethod;
    private String pathTemplate;
    private CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception> action;
    private List<CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception>> middlewares;

    public LambdaHttpRoute(
        String httpMethod, String pathTemplate, 
        CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception> action,
        List<CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception>> middlewares
    ) {
        this.httpMethod = httpMethod;
        this.pathTemplate = pathTemplate;
        this.action = action;
        this.middlewares = middlewares;
    }

    public List<CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception>> getMiddlewares() {
        return middlewares;
    }

    public void setMiddlewares(
            List<CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception>> middlewares) {
        this.middlewares = middlewares;
    }
    public String getHttpMethod() {
        return httpMethod;
    }
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }
    public String getPathTemplate() {
        return pathTemplate;
    }
    public void setPathTemplate(String pathTemplate) {
        this.pathTemplate = pathTemplate;
    }

    public CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception> getAction() {
        return action;
    }

    public void setAction(CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception> action) {
        this.action = action;
    }
}
