package org.example.services;

import org.example.requests.ProductItemRequest;
import org.example.responses.ProductItemResponse;

import retrofit2.Call;
import retrofit2.http.*;

public interface ProductService {
    @GET("api/v1/products/{id}")
    Call<ProductItemResponse> getProduct(@Path("id") long id);

    @POST("api/v1/products")
    Call<ProductItemResponse> createProduct(@Body ProductItemRequest item);
}
