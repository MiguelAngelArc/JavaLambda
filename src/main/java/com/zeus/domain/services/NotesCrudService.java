package com.zeus.domain.services;

import java.util.concurrent.CompletableFuture;

import com.zeus.models.entities.Note;


public interface NotesCrudService {
    CompletableFuture<Note> getNote(String key);

    CompletableFuture<String> createNote(Note note);

    CompletableFuture<Boolean> updateNote(Note note);

    CompletableFuture<Boolean> deleteNote(String key);
} 
