package com.api.api.Controller;

import com.api.api.Client.RickAndMortyClient;
import com.api.api.Response.*;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@AllArgsConstructor
@RequestMapping("/webclient")
public class RickAndMortyController {

    RickAndMortyClient rickAndMortyClient;

    /*@GetMapping("/character/{id}")
    public Mono<CharacterResponse> getCharacterById(@PathVariable String id){
        return rickAndMortyClient.findAndCharacterById(id);
    }

     */
/*
    @GetMapping("/location/{id}")
    public Mono<LocationResponse> getLocationById(@PathVariable String id){
        return rickAndMortyClient.findALugarById(id);
    }
*/
    @GetMapping("/character")
    public Flux<ListCharacterResponse> getCharacters(){
        return rickAndMortyClient.listTodosLosPersonajes();
    }

    @GetMapping("/locations/")
    public Flux<LocationResponse> getAllLocation() {
        Flux<ListLocationResponse> listLocationResponseFlux = rickAndMortyClient.listAllLocation("1");

        Mono<Integer> totalCountMono = listLocationResponseFlux
                .map(listLocationResponse -> Integer.parseInt(listLocationResponse.getInfo().getCount())) // Convertir a entero
                .reduce(0, Integer::sum);

        // Crear la URL con todas las IDs de ubicación
        Mono<String> locationIdsMono = totalCountMono.map(maxCount -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= maxCount; i++) {
                sb.append(i);
                if (i < maxCount) {
                    sb.append(",");
                }
            }
            return sb.toString();
        });

        // Realizar una sola consulta al API con todas las IDs de ubicación
        return locationIdsMono.flatMapMany(locationIds -> rickAndMortyClient.findALugarById(locationIds))
                .flatMap(Flux::fromIterable)
                .onErrorResume(error -> {
                    // Manejar el error aquí, por ejemplo, registrándolo o devolviendo un valor predeterminado
                    return Flux.empty();
                });
    }


    @GetMapping("/characters/")
    public Flux<CharacterResponse> getAllCharacters(){
        int totalPages = 42; // Total de páginas
        return Flux.range(1, totalPages)
                .flatMap(page -> rickAndMortyClient.characterByPage(String.valueOf(page)))
                .flatMap(response -> Flux.fromIterable(response.getResults()));
    }
/*
    @GetMapping("/location/")
    public Flux<CharacterResponse> getListFilterLocation(@RequestParam String name, @RequestParam(required = false) String search) {
        // Obtener la lista de ubicaciones filtradas por el nombre
        Flux<ListLocationResponse> filteredLocationsFlux = rickAndMortyClient.getListFilterLocation(name);

        // Extraer las IDs de los residentes y obtener los detalles de cada residente
        // Obtener los detalles de cada residente de las ubicaciones filtradas
        return filteredLocationsFlux
                .flatMap(locationResponse -> Flux.fromIterable(locationResponse.getResults())) // Obtener todas las ubicaciones
                .flatMap(location -> Flux.fromIterable(location.getResidents())) // Obtener los residentes de cada ubicación
                .flatMap(residentUrl -> { // Para cada URL de residente
                    String characterId = extractCharacterId(residentUrl); // Extraer la ID del personaje
                    // Obtener el personaje por su ID
                    Mono<CharacterResponse> characterResponseFlux = rickAndMortyClient.findAndCharacterById(characterId);
                    // Si se proporciona un parámetro de búsqueda por nombre, filtrar por nombre del personaje
                    if (search != null && !search.isEmpty()) {
                        characterResponseFlux = characterResponseFlux.filter(character -> character.getName().contains(search));
                    }
                    return characterResponseFlux;
                });
    }
*/
    /****************************************************************************************************
     *
     * @param
     * @return
     */

    @RequestMapping("/getallcharacter")
    public Mono<ListCharacterResponse> obtener(@RequestParam String page,@RequestParam(required = false) String name){
        return rickAndMortyClient.ListAllCharacter(page, name);
    }

    @GetMapping("/characterforall")
    public Mono<ListCharacterResponse> characterForAll(@RequestParam String page,
                                                       @RequestParam(required = false) String location,
                                                       @RequestParam(required = false) String name) {


        Flux<CharacterResponse> characterResponseFlux = rickAndMortyClient.findLocationByName(location)
                .flatMap(locationResponse -> {
                    if (locationResponse != null && locationResponse.getResults() != null
                            && !locationResponse.getResults().isEmpty()) {
                        LocationResponse result = locationResponse.getResults().get(0);
                        List<String> residentsUrls = result.getResidents();
                        if (residentsUrls != null && !residentsUrls.isEmpty()) {
                            // Extraer las IDs de los URLs
                            List<String> residentIds = residentsUrls.stream()
                                    .map(url -> url.substring(url.lastIndexOf("/") + 1))
                                    .collect(Collectors.toList());

                            // Concatenar todas las IDs en una sola cadena separada por comas
                            String allResidentIds = String.join(",", residentIds);

                            // Hacer una sola solicitud para obtener información sobre todos los personajes
                            return rickAndMortyClient.findAndCharacterById(allResidentIds)
                                    // Filtrar por nombre si se proporciona
                                    .filter(characterResponse -> name == null ||
                                            characterResponse.getName().toLowerCase().contains(name.toLowerCase()));
                        }
                    }

                    return null;
                });


        // Verificar si hay elementos en characterResponseFlux
        return characterResponseFlux.hasElements()
                .flatMap(hasElements -> {
                    if (hasElements) {
                        // Contar el total de elementos
                        Mono<Long> countMono = characterResponseFlux.count();

                        // Calcular el número total de páginas
                        return countMono.flatMap(count -> {
                            long totalPages = (count + 19) / 20; // Redondear hacia arriba

                            // Calcular el índice de inicio y fin de la página actual
                            int startIndex = (Integer.parseInt(page) - 1) * 20;
                            int endIndex = Math.min(startIndex + 20, count.intValue());

                            // Obtener los resultados de la página actual
                            Flux<CharacterResponse> pagedResultsFlux = characterResponseFlux.skip(startIndex).take(endIndex - startIndex);

                            // Crear una respuesta con los resultados y la información de paginación
                            return pagedResultsFlux.collectList().map(results -> {
                                ListCharacterResponse listCharacterResponse = new ListCharacterResponse();
                                InfoResponse infoResponse = new InfoResponse();
                                infoResponse.setCount(String.valueOf(count));
                                infoResponse.setPages(String.valueOf(totalPages));

                                // Establecer enlaces para las páginas anterior y siguiente
                                if (Integer.parseInt(page) < totalPages) {
                                    infoResponse.setNext("/webclient/characterforall?page=" + String.valueOf((Integer.parseInt(page) + 1))
                                            +"&location=" + location.replace(" ", "%20") +"&name=" + name );
                                }
                                if (Integer.parseInt(page) > 1) {
                                    infoResponse.setPrev("/webclient/characterforall?page=" + String.valueOf((Integer.parseInt(page) - 1))
                                            +"&location=" + location.replace(" ", "%20") +"&name=" + name );
                                }

                                listCharacterResponse.setResults(results);
                                listCharacterResponse.setInfo(infoResponse);
                                return listCharacterResponse;
                            });
                        });
                    } else {
                        // Devolver un objeto ListCharacterResponse vacío
                        return Mono.just(new ListCharacterResponse());
                    }
                });
    }

    // Método para extraer las IDs de una cadena que contiene URLs
    private List<String> extractIds(String input) {
        Pattern pattern = Pattern.compile("/\\d+");
        Matcher matcher = pattern.matcher(input);
        List<String> ids = new ArrayList<>();
        while (matcher.find()) {
            ids.add(matcher.group().substring(1)); // Quita el '/' del inicio
        }
        return ids;
    }


    // Método para extraer la ID de la URL de un residente
    private String extractCharacterId(String residentUrl) {
        // Obtener la ID de la URL
        String[] parts = residentUrl.split("/"); // Dividir la URL por "/"
        return parts[parts.length - 1]; // La ID estará en el último elemento del array resultante
    }

    }


