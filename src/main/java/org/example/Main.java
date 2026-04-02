package org.example;

import org.example.library.SearchLibrary;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        try (SearchLibrary library = new SearchLibrary();
             Scanner scanner = new Scanner(System.in)) {
            printHelp();

            while (true) {
                System.out.print("> ");
                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\s+", 2);
                String command = parts[0].toLowerCase();

                if ("exit".equals(command)) {
                    break;
                }

                try {
                    handleCommand(library, command, parts.length > 1 ? parts[1] : "");
                } catch (Exception e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось запустить библиотеку: " + e.getMessage());
        }
    }

    private static void handleCommand(SearchLibrary library, String command, String argument) throws IOException {
        switch (command) {
            case "add" -> {
                ensureArgument(argument, "Укажите путь: add <path>");
                Path path = Path.of(argument).toAbsolutePath().normalize();
                if (library.addPath(path)) {
                    System.out.println("Добавлено: " + path);
                }
                else {
                    System.out.println("Путь не добавлен (не существует или файл не .txt): " + path);
                }
            }
            case "remove" -> {
                ensureArgument(argument, "Укажите путь: remove <path>");
                Path path = Path.of(argument).toAbsolutePath().normalize();
                if (library.removePath(path)) {
                    System.out.println("Удалено: " + path);
                }
                else {
                    System.out.println("Путь не удален (не существует или файл не .txt): " + path);
                }
            }
            case "search" -> {
                ensureArgument(argument, "Укажите слово: search <word>");
                Set<Path> files = library.search(argument);
                if (files.isEmpty()) {
                    System.out.println("Ничего не найдено");
                } else {
                    files.forEach(System.out::println);
                }
            }
            case "help" -> printHelp();
            default -> System.out.println("Неизвестная команда. Введите help");
        }
    }

    private static void ensureArgument(String argument, String message) {
        if (argument == null || argument.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void printHelp() {
        System.out.println("Команды:");
        System.out.println("  add <path>    - добавить файл или директорию в индекс");
        System.out.println("  remove <path> - удалить файл или директорию из индекса");
        System.out.println("  search <word> - найти файлы по слову");
        System.out.println("  help          - показать команды");
        System.out.println("  exit          - выйти");
    }
}