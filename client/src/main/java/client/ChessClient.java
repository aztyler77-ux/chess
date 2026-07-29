package client;

import model.GameData;
import java.util.List;
import java.util.Scanner;

public class ChessClient {
    private ServerFacade server;
    private String token;
    private List<GameData> games;

    public ChessClient(int port) {
        server = new ServerFacade(port);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to 240 Chess");
        System.out.println(preHelp());
        while (true) {
            System.out.print(">>> ");
            String line = scanner.nextLine();
            if (line.trim().equalsIgnoreCase("quit")) {
                System.out.println("Goodbye");
                break;
            }
            System.out.println(doCommand(line));
        }
    }

    private String doCommand(String line) {
        if (line.isBlank()) {return "Type help";}
        String[] stuff = line.trim().split("\\s+");
        try {
            if (token == null) {
                if (stuff[0].equalsIgnoreCase("help")) {return preHelp();}
                if (stuff[0].equalsIgnoreCase("login")) {return login(stuff);}
                if (stuff[0].equalsIgnoreCase("register")) {return register(stuff);}
            } else {
                if (stuff[0].equalsIgnoreCase("help")) {return postHelp();}
                if (stuff[0].equalsIgnoreCase("create")) {return create(stuff);}
                if (stuff[0].equalsIgnoreCase("list")) {return list();}
                if (stuff[0].equalsIgnoreCase("logout")) {return logout();}
            }
        } catch (ResponseException error) {return error.getMessage();}
        return "Unknown command. Type help.";
    }

    private String login(String[] stuff) throws ResponseException {
        if (stuff.length != 3) {return "Usage: login <USERNAME> <PASSWORD>";}
        var result = server.login(stuff[1], stuff[2]);
        token = result.authToken();
        return "Logged in as " + result.username();
    }

    private String register(String[] stuff) throws ResponseException {
        if (stuff.length != 4) {return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";}
        var result = server.register(stuff[1], stuff[2], stuff[3]);
        token = result.authToken();
        return "Registered as " + result.username();
    }

    private String create(String[] stuff) throws ResponseException {
        if (stuff.length < 2) {return "Usage: create <GAME NAME>";}
        String name = stuff[1];
        for (int i = 2; i < stuff.length; i++) {
            name += " " + stuff[i];}
        server.createGame(token, name);
        return "Created " + name;
    }

    private String list() throws ResponseException {
        games = server.listGames(token);
        if (games.isEmpty()) {return "No games";}
        String answer = "";
        for (int i = 0; i < games.size(); i++) {
            var game = games.get(i);
            String white = game.whiteUsername();
            String black = game.blackUsername();
            if (white == null) {white = "open";}
            if (black == null) {black = "open";}

            answer += (i + 1) + ". " + game.gameName()
                    + " | white: " + white
                    + " | black: " + black + "\n";
        }
        return answer;
    }

    private String logout() throws ResponseException {
        server.logout(token);
        token = null;
        games = null;
        return "Logged out";
    }

    private String preHelp() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL>
                login <USERNAME> <PASSWORD>
                help
                quit
                """;
    }

    private String postHelp() {
        return """
                create <GAME NAME>
                list
                logout
                help
                quit
                """;
    }
}