package com.zeus.infrastructure.dataaccess;

import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;
import com.zeus.domain.dataaccess.NoSqlRepository;

import software.amazon.awssdk.enhanced.dynamodb.document.EnhancedDocument;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import java.util.concurrent.CompletableFuture;

public class DynamoDBTableRepository<T> implements NoSqlRepository<T> {
  private DynamoDbAsyncClient dynamoDbAsyncClient;
  private Class<T> innerType;
  // private DynamoDbEnhancedAsyncClient dynamoEnhancedClient;
  private String tableName;

  public DynamoDBTableRepository(DynamoDbAsyncClient dynamoDbAsyncClient, String tableName, Class<T> type) {
    this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    this.tableName = tableName;
    this.innerType = type;
    // this.dynamoEnhancedClient = DynamoDbEnhancedAsyncClient.builder()
    //     .dynamoDbClient(dynamoDbAsyncClient)
    //     .build();
  }

  @Override
  public CompletableFuture<Boolean> addOrUpdateItem(T input) {
    PutItemRequest request = PutItemRequest.builder().tableName(tableName).item(toAttributeMap(input)).build();
    CompletableFuture<PutItemResponse> addTask = dynamoDbAsyncClient.putItem(request);
    return addTask.thenApplyAsync(r -> true);
  }

  @Override
  public CompletableFuture<T> getItem(String key) {
    Map<String, AttributeValue> dynamoKey = new HashMap<String, AttributeValue>() {{
        put("Id", AttributeValue.builder().s(key).build());
    }};
    GetItemRequest request = GetItemRequest.builder().key(dynamoKey).tableName(tableName).build();
    CompletableFuture<GetItemResponse> getTask = dynamoDbAsyncClient.getItem(request);
    // GetItemResponse dynamoResponse = getTask.get();
    // T result = toModel(dynamoResponse.item());
    return getTask.thenApplyAsync(r -> toModel(r.item()));
  }

  private T toModel(Map<String, AttributeValue> rawItem) {
    // System.out.println("Tomodel1");
    EnhancedDocument document = EnhancedDocument.fromAttributeValueMap(rawItem);
    // Type mySuperclass = getClass().getGenericSuperclass();
    // Type tType = ((ParameterizedType)mySuperclass).getActualTypeArguments()[0];
    //System.out.println("Tomodel2");
    // Type type = new TypeToken<T>() {}.getType();
    //System.out.println("Tomodel3");
    //System.out.println(document.toJson());
    return new Gson().fromJson(document.toJson(), innerType);
  }

  private Map<String, AttributeValue> toAttributeMap(T item) {
    String documentJson = new Gson().toJson(item);
    EnhancedDocument document = EnhancedDocument.fromJson(documentJson);
    return document.toMap();
  }
}
