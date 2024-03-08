package com.api.api.Client;

import com.api.api.Response.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class RickAndMortyClient {
    private final WebClient webClient;

    public RickAndMortyClient(WebClient.Builder builder) {
        webClient = builder.baseUrl("https://rickandmortyapi.com/api").build();
    }

    public Flux<CharacterResponse> findAndCharacterById(String id){
        log.info("buscando personaje por su id" + id);
        return webClient
                .get()
                .uri("/character/" + id)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(CharacterResponse.class);
    }

    public Flux<ListLocationResponse> findLocationByName(String name){
        log.info("buscando personaje por su id" + name);
        return webClient
                .get()
                .uri("/location?name=" + name)
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ListLocationResponse.class);
    }

    public Flux<List<LocationResponse>> findALugarById(String id) {
        log.info("buscando personaje por su id" + id);
        return webClient
                .get()
                .uri("/location/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(LocationResponse.class)
                .collectList()
                .flux()
                .onErrorResume(error -> {
                    // Manejar el error aquí, por ejemplo, registrándolo o devolviendo un valor predeterminado
                    return Flux.empty();
                });
    }

    public Flux<ListCharacterResponse> listTodosLosPersonajes(){
        log.info("Listando todos los personajes");
        return webClient
                .get()
                .uri("/character")
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ListCharacterResponse.class);
    }

    public Mono<ListCharacterResponse> ListAllCharacter(String page, String name){
        log.info("Listando todos los personajes");
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/character/")
                        .queryParam("page", page)
                        .queryParam("name", name)
                        .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ListCharacterResponse.class);
    }

    public Flux<ListLocationResponse> listAllLocation(String page){
        log.info("list all location");
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/location/")
                        .queryParam("page", page)
                        .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ListLocationResponse.class);
    }


    public Flux<ListCharacterResponse> characterByPage(String page){
        log.info("Listando todos los personajes de la página {}", page);
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/character")
                        .queryParam("page", page)
                        .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ListCharacterResponse.class);
    }

    public Flux<ListLocationResponse> getListFilterLocation(String locationName) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/location/")
                        .queryParam("name", locationName)
                        .build())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(ListLocationResponse.class);

    }


}
