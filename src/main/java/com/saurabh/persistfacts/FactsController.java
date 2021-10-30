package com.saurabh.persistfacts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

@RestController
public class FactsController {

    @Value("${facts.server:http://localhost:8080/dummy}")
    public String factsServer;

    private static final Path path = Paths.get("facts.txt");

    /**
     * Method that gets facts.
     */
    @GetMapping("/facts")
    public List<String> getFacts() throws IOException {
        return Files.readAllLines(path, Charset.defaultCharset());
    }

    @GetMapping("/dummy")
    public String dummy() {
        return "New printer fact: Printers dislike citrus scent.";
    }

    /**
     * Method that loads facts and persists to a file.
     */
    @GetMapping("/facts/persist")
    public Mono<Void> loadFacts(@RequestParam(defaultValue = "10") int count) {

        var webClient = WebClient.create(factsServer);
        return Flux.range(0, count)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(i -> webClient
                        .get()
                        .retrieve()
                        .bodyToMono(String.class))
                .map(s -> s.replaceFirst("^New printer fact: ", ""))
                .collectSortedList(Comparator.naturalOrder())
                .mapNotNull(facts -> {
                    try {
                        return Files.write(path, facts, Charset.defaultCharset());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .then();
    }
}
