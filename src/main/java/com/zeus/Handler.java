package com.zeus;

import java.net.URI;
import java.util.List;

import java.util.ArrayList;
import java.util.Optional;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.zeus.domain.dataaccess.NoSqlRepository;
import com.zeus.domain.services.AuthService;
import com.zeus.domain.services.AuthServiceImpl;
import com.zeus.domain.services.JwtService;
import com.zeus.domain.services.JwtServiceImpl;
import com.zeus.domain.services.NotesCrudService;
import com.zeus.domain.services.NotesCrudServiceImpl;
import com.zeus.domain.services.PasswordHasher;
import com.zeus.infrastructure.dataaccess.DynamoDBTableRepository;
import com.zeus.infrastructure.services.PBKDF2PasswordHasher;
import com.zeus.infrastructure.utils.CheckedFunction;
import com.zeus.models.config.JwtConfig;
import com.zeus.models.entities.Note;
import com.zeus.models.entities.User;
import com.zeus.models.enums.ErrorCodes;
import com.zeus.webapi.LambdaHttpRoute;
import com.zeus.webapi.controllers.NotesController;
import com.zeus.webapi.controllers.UsersController;
import com.zeus.webapi.errors.HttpErrorInfo;
import com.zeus.webapi.errors.HttpErrors;
import com.zeus.webapi.middlewares.Middlewares;

import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private List<LambdaHttpRoute> routes;
    private Gson gson;

    public Handler() {
        gson = new Gson();
        routes = new ArrayList<>();
        // TODO: Dependency injection would be better in this case
        String env = System.getenv().getOrDefault("ENV", "local");
        String jwtHmacSecret = System.getenv().getOrDefault("JWT_HMAC_SECRET", "ThisIsASuperSecretSecurityKeyUsedForTheAuthenticationServer");
        String jwtIssuer = System.getenv().getOrDefault("JWT_ISSUER", "java-notes-api");
        String notesTableName = System.getenv().getOrDefault("NOTES_TABLE_NAME", "notes");
        String usersTableName = System.getenv().getOrDefault("USERS_TABLE_NAME", "users");
        int jwtTtlSeconds = Integer.parseInt(System.getenv().getOrDefault("JWT_TTL_SECONDS", "3600"));
        DynamoDbAsyncClient dynamoDbAsyncClient = null;
        if (env.equals("local")){
            dynamoDbAsyncClient = DynamoDbAsyncClient.builder()
                .region(Region.US_EAST_1)
                .httpClient(NettyNioAsyncHttpClient.create())
                .endpointOverride(URI.create("http://localstack:4566"))
                .build();
        }
        else {
            dynamoDbAsyncClient = DynamoDbAsyncClient.builder()
                .region(Region.US_EAST_1)
                .httpClient(NettyNioAsyncHttpClient.create())
                .build();
        }
        NoSqlRepository<Note> dynamoNotesRepository = new DynamoDBTableRepository<>(
            dynamoDbAsyncClient, notesTableName, Note.class
        );
        NoSqlRepository<User> dynamoUsersRepository = new DynamoDBTableRepository<>(
            dynamoDbAsyncClient, usersTableName, User.class
        );
        JwtConfig jwtConfig = new JwtConfig(jwtHmacSecret, jwtIssuer, jwtTtlSeconds);
        JwtService jwtService = new JwtServiceImpl(jwtConfig);
        PasswordHasher passwordHasher = new PBKDF2PasswordHasher();

        AuthService authService = new AuthServiceImpl(jwtService, dynamoUsersRepository, passwordHasher);
        UsersController usersController = new UsersController(authService);
 
        NotesCrudService notesService = new NotesCrudServiceImpl(dynamoNotesRepository);
        NotesController notesController = new NotesController(notesService, jwtService);
        
        routes.add(
            new LambdaHttpRoute("POST", "/notes", notesController::createNote, List.of(Middlewares.validateJwt(jwtService)))
        );
        routes.add(
            new LambdaHttpRoute("PUT", "/notes", notesController::updateNote, List.of(Middlewares.validateJwt(jwtService)))
        );
        routes.add(
            new LambdaHttpRoute("DELETE", "/notes/([\\w|\\d]+)", notesController::deleteNote, List.of(Middlewares::validateUserId, Middlewares::validateUserName))
        );
        routes.add(
            new LambdaHttpRoute("GET", "/notes/([\\w|\\d]+)", notesController::getNote, List.of(Middlewares.validateJwt(jwtService)))
        );
        routes.add(
            new LambdaHttpRoute("POST", "/users/sign-in", usersController::signIn, null)
        );
        routes.add(
            new LambdaHttpRoute("POST", "/users/sign-up", usersController::signUp, null)
        );
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // context.getLogger().log(gson.toJson(input));
            String httpMethod = input.getHttpMethod();
            String path = input.getPath();
            Optional<LambdaHttpRoute> route = getRoute(httpMethod, path);
            if (route.isEmpty()){
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(404);
            }

            APIGatewayProxyResponseEvent middlewaresResponse = runMiddlewares(route.get(), input);
            if (middlewaresResponse != null) 
                return middlewaresResponse;

            return route.get().getAction().apply(input);
        } catch (Exception e){
            HttpErrorInfo errorInfo = HttpErrors.CommonErrors.get(ErrorCodes.UNKNOWN_ERROR.toString());
            if (HttpErrors.CommonErrors.containsKey(e.getMessage())) // is a knownn error
                errorInfo = HttpErrors.CommonErrors.get(e.getMessage());
            else // is an unknown error
                context.getLogger().log(e.getMessage());
            
            return new APIGatewayProxyResponseEvent()
                .withBody(gson.toJson(errorInfo))
                .withStatusCode(errorInfo.getHttpStatusCode());
        }
    }

    private Optional<LambdaHttpRoute> getRoute(String httpMethod, String path) {
        return routes
            .stream()
            .filter(i -> i.getHttpMethod().equals(httpMethod) && path.matches(i.getPathTemplate()))
            .findFirst();
    }

    private APIGatewayProxyResponseEvent runMiddlewares(LambdaHttpRoute httpRoute, APIGatewayProxyRequestEvent input) throws Exception {
        List<CheckedFunction<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Exception>> middlewares = httpRoute.getMiddlewares();
        if (middlewares == null || middlewares.size() == 0)
            return null;
        
        for (int i = 0; i < middlewares.size(); i++) {
            APIGatewayProxyResponseEvent middlewareResponse = middlewares.get(i).apply(input);
            if (middlewareResponse != null)
                return middlewareResponse;
        }

        return null;
    }
}
