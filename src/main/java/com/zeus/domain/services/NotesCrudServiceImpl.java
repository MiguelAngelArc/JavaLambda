package com.zeus.domain.services;

import java.util.concurrent.CompletableFuture;

import com.github.f4b6a3.ulid.UlidCreator;
import com.zeus.domain.dataaccess.NoSqlRepository;
import com.zeus.models.entities.Note;
import com.zeus.models.enums.ErrorCodes;

import io.netty.util.internal.StringUtil;

public class NotesCrudServiceImpl implements NotesCrudService {
    private NoSqlRepository<Note> notesRepository;

    public NotesCrudServiceImpl(NoSqlRepository<Note> notesRepository) {
        this.notesRepository = notesRepository;
    }

    @Override
    public CompletableFuture<Note> getNote(String key) {
        return notesRepository.getItem(key).thenApplyAsync(n -> {
            if (n == null || StringUtil.isNullOrEmpty(n.getId()))
                throw new RuntimeException(ErrorCodes.NOTE_NOT_FOUND.toString());
            return n;
        });
    }

    @Override
    public CompletableFuture<String> createNote(Note note) {
        String id = UlidCreator.getUlid().toString();
        note.setId(id);
        return notesRepository.addOrUpdateItem(note).thenApplyAsync(r -> id);
    } 

    @Override
    public CompletableFuture<Boolean> updateNote(Note note) {
        return notesRepository.addOrUpdateItem(note);
    }

    @Override
    public CompletableFuture<Boolean> deleteNote(String key) {
        CompletableFuture<Note> noteTask = getNote(key);
        return noteTask.thenComposeAsync(n -> { 
            n.setDeleted(true);
            return notesRepository.addOrUpdateItem(n);
        });
    }
}
