package com.zeus.domain.dataaccess;
import java.util.concurrent.CompletableFuture;

public interface NoSqlRepository<T> {
    public CompletableFuture<Boolean> addOrUpdateItem(T input);
  
    public CompletableFuture<T> getItem(String key);
}
