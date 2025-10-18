package com.antonio.adapter.console;

import com.antonio.application.service.ProductService;
import com.antonio.application.service.TodoService;
import com.antonio.application.service.impl.ProductServiceImpl;
import com.antonio.application.service.impl.TodoServiceImpl;
import com.antonio.domain.model.Product;
import com.antonio.domain.model.Todo;
import com.antonio.infrastructure.http.HttpClientWrapper;
import com.antonio.infrastructure.repository.ProductRepositoryHttp;
import com.antonio.infrastructure.repository.TodoRepositoryHttp;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ConsoleApp {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String base = "https://dummyjson.com"; // base url
        HttpClientWrapper client = new HttpClientWrapper(base, mapper);

        var productRepo = new ProductRepositoryHttp(client, mapper);
        var todoRepo = new TodoRepositoryHttp(client, mapper);

        ProductService productService = new ProductServiceImpl(productRepo);
        TodoService todoService = new TodoServiceImpl(todoRepo);

        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Catálogo & Tarefas (DummyJSON) ===");
            System.out.println("1) Listar produtos (limit, skip)");
            System.out.println("2) Buscar produtos por texto (q)");
            System.out.println("3) Listar todos (limit, skip)");
            System.out.println("4) Adicionar todo (texto + userId)");
            System.out.println("5) Marcar/Desmarcar todo (id + completed true/false)");
            System.out.println("6) Remover todo (id)");
            System.out.println("7) Sair");
            System.out.print("Escolha: ");
            String opt = sc.nextLine().trim();
            try {
                switch (opt) {
                    case "1" -> {
                        System.out.print("limit: ");
                        int limit = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("skip: ");
                        int skip = Integer.parseInt(sc.nextLine().trim());
                        List<Product> products = productService.list(limit, skip);
                        products.forEach(p -> System.out.printf("%d | %s | %.2f%n", p.getId(), p.getTitle(), p.getPrice()));
                    }
                    case "2" -> {
                        System.out.print("q: ");
                        String q = sc.nextLine().trim();
                        List<Product> results = productService.search(q);
                        results.forEach(p -> System.out.printf("%d | %s | %.2f%n", p.getId(), p.getTitle(), p.getPrice()));
                    }
                    case "3" -> {
                        System.out.print("limit: ");
                        int limit = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("skip: ");
                        int skip = Integer.parseInt(sc.nextLine().trim());
                        List<Todo> todos = todoService.list(limit, skip);
                        todos.forEach(t -> System.out.printf("%d | %s | %s | user=%d%n",
                                t.getId(), t.getTodo(), t.getCompleted() ? "OK" : "PEND", t.getUserId()));
                    }
                    case "4" -> {
                        System.out.print("texto: ");
                        String text = sc.nextLine().trim();
                        System.out.print("userId: ");
                        int userId = Integer.parseInt(sc.nextLine().trim());
                        var created = todoService.add(text, userId);
                        System.out.println("Criado: " + created);
                    }
                    case "5" -> {
                        System.out.print("id: ");
                        int id = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("completed (true/false): ");
                        boolean completed = Boolean.parseBoolean(sc.nextLine().trim());
                        var updated = todoService.toggle(id, completed);
                        System.out.println("Atualizado: " + updated);
                    }
                    case "6" -> {
                        System.out.print("id: ");
                        int id = Integer.parseInt(sc.nextLine().trim());
                        boolean ok = todoService.delete(id);
                        System.out.println(ok ? "Removido com sucesso" : "Falha ao remover");
                    }
                    case "7" -> {
                        System.out.println("Tchau!");
                        sc.close();
                        return;
                    }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IOException e) {
                System.err.println("Erro IO/HTTP: " + e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("Erro: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Erro inesperado: " + e.getMessage());
            }
        }
    }
}
