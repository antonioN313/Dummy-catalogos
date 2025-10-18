package com.antonio.infrastructure.http;

import com.antonio.exception.HttpException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpClientWrapper {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public HttpClientWrapper(String baseUrl, ObjectMapper mapper) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = baseUrl;
        this.mapper = mapper;
    }

    public String get(String path) throws IOException {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .GET()
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new HttpException(resp.statusCode(), resp.body());
            }
            return resp.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    public String post(String path, Object payload) throws IOException {
        try {
            String body = mapper.writeValueAsString(payload);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new HttpException(resp.statusCode(), resp.body());
            }
            return resp.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    public String put(String path, Object payload) throws IOException {
        try {
            String body = mapper.writeValueAsString(payload);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new HttpException(resp.statusCode(), resp.body());
            }
            return resp.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }

    public void delete(String path) throws IOException {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .DELETE()
                    .header("Accept", "application/json")
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                throw new HttpException(resp.statusCode(), resp.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }
}
