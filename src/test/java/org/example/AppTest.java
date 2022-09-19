package org.example;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.example.db.dao.ProductsMapper;
import org.example.db.model.ProductsExample;
import org.example.requests.ProductItemRequest;
import org.example.responses.ProductItemResponse;
import org.example.services.ProductService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import org.junit.jupiter.api.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;

import static org.hamcrest.MatcherAssert.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppTest {
    static final String BASE_URL = "https://minimarket1.herokuapp.com/market/";

    static final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

    static Retrofit CreateRetrofit() {
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .build())
                .build();
    }

    @BeforeAll
    static void beforeAll() {
        addedItems = new ArrayList<>();
    }

    static List<ProductItemResponse> addedItems;

    @Order(1)
    @ParameterizedTest
    @MethodSource("createProductSource")
    @DisplayName("Add product item using REST API")
    void createProduct(ProductItemRequest item) throws IOException {
        var retrofit = CreateRetrofit();
        var response = retrofit.create(ProductService.class)
                .createProduct(item).execute();
        assertThat(response.isSuccessful(), CoreMatchers.is(true));
        addedItems.add(response.body());
    }

    static Stream<ProductItemRequest> createProductSource() {
        return Stream.of(
                new ProductItemRequest("3fb43f67-2070-41e4-9dbb-558a58475c43", 101, "Food"),
                new ProductItemRequest("4002ed98-d7a0-48c3-8380-c161bb79245b", 102, "Food"),
                new ProductItemRequest("03a5fb29-9150-4c66-bcb7-f985380c74d1", 103, "Food"),
                new ProductItemRequest("a27a2585-21bd-4d46-aada-2c2b0124b72d", 104, "Food"),
                new ProductItemRequest("444b0b07-c378-4840-bf95-f497fafe8866", 105, "Food")
        );
    }

    @Order(2)
    @ParameterizedTest
    @MethodSource("findProductSource")
    @DisplayName("Find product item using ORM")
    void findProduct(ProductItemResponse item) throws IOException {
        var factory = new SqlSessionFactoryBuilder()
                .build(Resources.getResourceAsStream("myBatisConfig.xml"));
        try (var session = factory.openSession()) {
            var prod = session
                    .getMapper(ProductsMapper.class)
                    .selectByPrimaryKey(item.getId())
                    ;
            assertThat(prod.getTitle(), CoreMatchers.is(item.getTitle()));
            assertThat(prod.getPrice(), CoreMatchers.is(item.getPrice()));
        }
    }

    static Stream<ProductItemResponse> findProductSource() {
        return addedItems.stream();
    }

    @Order(3)
    @ParameterizedTest
    @MethodSource("deleteProductSource")
    @DisplayName("Delete products using ORM")
    void deleteProduct(String title) throws IOException {
        var factory = new SqlSessionFactoryBuilder()
                .build(Resources.getResourceAsStream("myBatisConfig.xml"));
        try (var session = factory.openSession()) {
            var example = new ProductsExample();
            example.createCriteria().andTitleEqualTo(title);
            var count = session
                    .getMapper(ProductsMapper.class)
                    .deleteByExample(example)
                    ;
            session.commit();
            assertThat(count > 0, CoreMatchers.is(true));
        }
    }

    static Stream<String> deleteProductSource() {
        return createProductSource().map(ProductItemRequest::getTitle);
    }

    @Order(4)
    @ParameterizedTest
    @MethodSource("getProductSource")
    @DisplayName("Get product using REST API")
    void getProduct(Long id) throws IOException {
        var retrofit = CreateRetrofit();
        var response = retrofit.create(ProductService.class)
                .getProduct(id).execute();
        assertThat(response.code(), CoreMatchers.is(404));
    }

    static Stream<Long> getProductSource() {
        return addedItems.stream().map(ProductItemResponse::getId);
    }
}