package client;

import java.util.Scanner;

public class ChessClient {
    private final ServerFacade facade;
    private String authToken;

    public ChessClient(int port) {
        facade = new ServerFacade(port);
    }

    public void run() {
        System.out.println("Welcome to 240 Chess");
        System.out.println(preloginHelp());
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.print(">>> ");
            String input = scanner.nextLine();

            if (input.trim().equalsIgnoreCase("quit")) {
                System.out.println("Goodbye");
                running = false;
            } else {
                System.out.println(eval(input));
            }
        }
    }

    private String eval(String input) {
        if (input.isBlank()) {
            return "Type help for commands";
        }

        String[] words = input.trim().split("\\s+");
        String command = words[0].toLowerCase();
        try {
            if (authToken != null) {
                return postloginEval(command);
            }
            return switch (command) {
                case "help" -> preloginHelp();
                case "login" -> login(words);
                case "register" -> register(words);
                default -> "Unknown command. Type help.";
            };
        } catch (ResponseException error) {
            return error.getMessage();
        }
    }

    private String login(String[] words) throws ResponseException {
        if (words.length != 3) {
            return "Usage: login <USERNAME> <PASSWORD>";
        }

        var user = facade.login(words[1], words[2]);
        authToken = user.authToken();
        return "Logged in as " + user.username();
    }

    private String register(String[] words) throws ResponseException {
        if (words.length != 4) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";
        }

        var user = facade.register(words[1], words[2], words[3]);
        authToken = user.authToken();
        return "Registered and logged in as " + user.username();
    }

    private String postloginEval(String command) {
        if (command.equals("help")) {
            return "Post-login commands are coming next";
        }
        return "Unknown command. Type help.";
    }

    private String preloginHelp() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL>
                login <USERNAME> <PASSWORD>
                help
                quit
                """;
    }
}