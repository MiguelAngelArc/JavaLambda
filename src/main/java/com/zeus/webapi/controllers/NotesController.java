package com.zeus.webapi.controllers;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zeus.domain.services.JwtService;
import com.zeus.domain.services.NotesCrudService;
import com.zeus.models.entities.Note;
import com.zeus.models.enums.ErrorCodes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.Map;

public class NotesController {
    private NotesCrudService notesService;
    private Gson gson;

    public NotesController(NotesCrudService notesService, JwtService jwtService) {
        this.notesService = notesService;
        gson = new GsonBuilder().enableComplexMapKeySerialization().create();
    }

    public APIGatewayProxyResponseEvent createNote(APIGatewayProxyRequestEvent input)
        throws InterruptedException, ExecutionException 
    {
        Note note = gson.fromJson(input.getBody(), Note.class);
        note.setUserId(input.getRequestContext().getIdentity().getUser());
        CompletableFuture<String> createTask = notesService.createNote(note);
        String id = createTask.get();
        note.setId(id);
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withBody(gson.toJson(note));
    }

    public APIGatewayProxyResponseEvent updateNote(APIGatewayProxyRequestEvent input)
        throws InterruptedException, ExecutionException 
    {
        Note note = gson.fromJson(input.getBody(), Note.class);
        Boolean updateResult = notesService.updateNote(note).get();
        Map<String, Boolean> anonymousObjectUpdate = new HashMap<String, Boolean>() {{
            put("successful", updateResult);
        }};
        return new APIGatewayProxyResponseEvent()
            .withStatusCode(200)
            .withBody(gson.toJson(anonymousObjectUpdate));
    }

    public APIGatewayProxyResponseEvent deleteNote(APIGatewayProxyRequestEvent input)
        throws InterruptedException, ExecutionException 
    {
        String[] pathParts =  input.getPath().split("/");
        String idToDelete = pathParts[pathParts.length - 1];
        Boolean deleteResult = notesService.deleteNote(idToDelete).get();
        Map<String, Object> anonymousObjectDelete = new HashMap<String, Object>() {{
            put("successful", deleteResult);
        }};
        return new APIGatewayProxyResponseEvent()
            .withStatusCode(200)
            .withBody(gson.toJson(anonymousObjectDelete));
    }

    public APIGatewayProxyResponseEvent getNote(APIGatewayProxyRequestEvent input)
        throws InterruptedException, ExecutionException 
    {
        String[] pathParts =  input.getPath().split("/");
        String id = pathParts[pathParts.length - 1];
        Note note = notesService.getNote(id).get();

        if (note.getUserId() != input.getRequestContext().getIdentity().getUser())
            throw new RuntimeException(ErrorCodes.NOTE_DOES_NOT_BELONG_TO_USER.toString());

        return new APIGatewayProxyResponseEvent()
            .withStatusCode(200)
            .withBody(gson.toJson(note));
    }
}
