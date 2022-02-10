package com.eyo.reactivemongo.controller;

import com.eyo.reactivemongo.dto.ProductDto;
import com.eyo.reactivemongo.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@WebFluxTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private ProductService productService;

    private ProductDto productDto;
    private ProductDto productDto2;

    @BeforeEach
    void setUp() {
        productDto = new ProductDto("102", "Sedan", 1, 1700.0);
        productDto2 = new ProductDto("103", "SUV", 4, 20000.0);
    }

    @Test
    void testGetProducts() {
        Flux<ProductDto> productDtoFlux = Flux.just(productDto, productDto2);
        when(productService.getProducts()).thenReturn(productDtoFlux);

        Flux<ProductDto> responseBody = webTestClient.get().uri("/products")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductDto.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectSubscription()
                .expectNext(productDto)
                .expectNext(productDto2)
                .verifyComplete();

    }

    @Test
    void testGetProductById() {
        Mono<ProductDto> productDtoMono = Mono.just(productDto);
        when(productService.getProduct(any())).thenReturn(productDtoMono);

        Flux<ProductDto> responseBody = webTestClient.get().uri("/products/102")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductDto.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectSubscription()
                .expectNextMatches(productDto1 -> productDto1.getName().equals(productDto.getName()))
                .verifyComplete();

    }

    @Test
    void getProductBetweenRange() {
        Flux<ProductDto> productDtoFlux = Flux.just(productDto);
        given(productService.getProductInRange(anyDouble(), anyDouble())).willReturn(productDtoFlux);

        Flux<ProductDto> responseBody = webTestClient.get().uri("/products/price-range?min=1500&max=1700")
                .exchange()
                .expectStatus().isOk()
                .returnResult(ProductDto.class)
                .getResponseBody();

        StepVerifier.create(responseBody)
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void testAddProduct() {
        Mono<ProductDto> productDtoMono = Mono.just(productDto);
        when(productService.saveProduct(productDtoMono)).thenReturn(productDtoMono);

        webTestClient.post().uri("/products")
                .body(Mono.just(productDtoMono), ProductDto.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateProduct() {
        Mono<ProductDto> productDtoMono = Mono.just(productDto);
        when(productService.updateProduct(productDtoMono, "102")).thenReturn(productDtoMono);

        webTestClient.put().uri("/products/update/102")
                .body(Mono.just(productDtoMono), ProductDto.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteProduct() {
        given(productService.deleteProduct(any())).willReturn(Mono.empty());

        webTestClient.delete().uri("/products/delete/102")
                .exchange()
                .expectStatus().isOk();
    }
}