package client;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import model.GameData;
import ui.BoardDrawer;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChessClient implements NotificationHandler {
    private ServerFacade server;
    private String token;
    private List<GameData> games;
    private BoardDrawer drawer;
    private WebSocketFacade websocket;
    private volatile GameData currentGame;
    private int gameID;
    private String side;
    private int port;
    private boolean inGame;
    private boolean observing;
    private Scanner scanner;

    public ChessClient(int port) {
        server = new ServerFacade(port);
        drawer = new BoardDrawer();
        scanner = new Scanner(System.in);
        this.port = port;
    }

    public void run() {
        System.out.println("Welcome to 240 Chess");
        System.out.println(preHelp());

        while (true) {
            printPrompt();
            String line = scanner.nextLine();

            if (line.trim().equalsIgnoreCase("quit")) {
                if (websocket != null) {
                    if (inGame) {
                        try {
                            websocket.leave(token, gameID);
                        } catch (ResponseException error) {
                            // quitting anyway
                        }
                    }

                    websocket.close();
                }

                System.out.println("Goodbye");
                break;
            }

            String answer = doCommand(line);

            if (answer != null && !answer.isBlank()) {
                System.out.println(answer);
            }
        }
    }

    private String doCommand(String line) {
        if (line.isBlank()) {
            return "Type help";
        }

        String[] stuff = line.trim().split("\\s+");

        try {
            if (token == null) {
                if (stuff[0].equalsIgnoreCase("help")
                        && stuff.length != 1) {
                    return "Usage: help";
                }

                if (stuff[0].equalsIgnoreCase("help")) {
                    return preHelp();
                }

                if (stuff[0].equalsIgnoreCase("login")) {
                    return login(stuff);
                }

                if (stuff[0].equalsIgnoreCase("register")) {
                    return register(stuff);
                }

            } else if (inGame) {
                if (stuff[0].equalsIgnoreCase("help")
                        && stuff.length != 1) {
                    return "Usage: help";
                }

                if (stuff[0].equalsIgnoreCase("help")) {
                    if (observing) {
                        return observerHelp();
                    }

                    return gameHelp();
                }

                if (stuff[0].equalsIgnoreCase("redraw")
                        && stuff.length != 1) {
                    return "Usage: redraw";
                }

                if (stuff[0].equalsIgnoreCase("redraw")) {
                    return redraw();
                }

                if (stuff[0].equalsIgnoreCase("highlight")) {
                    return highlight(stuff);
                }

                if (stuff[0].equalsIgnoreCase("move")) {
                    return move(stuff);
                }

                if (stuff[0].equalsIgnoreCase("resign")
                        && stuff.length != 1) {
                    return "Usage: resign";
                }

                if (stuff[0].equalsIgnoreCase("resign")) {
                    return resign();
                }

                if (stuff[0].equalsIgnoreCase("leave")
                        && stuff.length != 1) {
                    return "Usage: leave";
                }

                if (stuff[0].equalsIgnoreCase("leave")) {
                    return leave();
                }

            } else {
                if (stuff[0].equalsIgnoreCase("help")
                        && stuff.length != 1) {
                    return "Usage: help";
                }

                if (stuff[0].equalsIgnoreCase("help")) {
                    return postHelp();
                }

                if (stuff[0].equalsIgnoreCase("create")) {
                    return create(stuff);
                }

                if (stuff[0].equalsIgnoreCase("list")
                        && stuff.length != 1) {
                    return "Usage: list";
                }

                if (stuff[0].equalsIgnoreCase("list")) {
                    return list();
                }

                if (stuff[0].equalsIgnoreCase("join")) {
                    return join(stuff);
                }

                if (stuff[0].equalsIgnoreCase("observe")) {
                    return observe(stuff);
                }

                if (stuff[0].equalsIgnoreCase("logout")
                        && stuff.length != 1) {
                    return "Usage: logout";
                }

                if (stuff[0].equalsIgnoreCase("logout")) {
                    return logout();
                }
            }

        } catch (ResponseException error) {
            return error.getMessage();

        } catch (Exception error) {
            return "Something went wrong";
        }

        return "Unknown command. Type help.";
    }

    private String login(String[] stuff) throws ResponseException {
        if (stuff.length != 3) {
            return "Usage: login <USERNAME> <PASSWORD>";
        }

        var result = server.login(stuff[1], stuff[2]);

        token = result.authToken();
        games = null;

        return "Logged in as " + result.username();
    }

    private String register(String[] stuff) throws ResponseException {
        if (stuff.length != 4) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";
        }

        var result = server.register(
                stuff[1],
                stuff[2],
                stuff[3]
        );

        token = result.authToken();
        games = null;

        return "Registered as " + result.username();
    }

    private String create(String[] stuff) throws ResponseException {
        if (stuff.length < 2) {
            return "Usage: create <GAME NAME>";
        }

        String name = stuff[1];

        for (int i = 2; i < stuff.length; i++) {
            name += " " + stuff[i];
        }

        server.createGame(token, name);

        // refresh this so joining right after create does not use an old list
        games = server.listGames(token);

        return "Created " + name;
    }

    private String list() throws ResponseException {
        games = server.listGames(token);

        if (games.isEmpty()) {
            return "No games";
        }

        String answer = "";

        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);

            String white = game.whiteUsername();
            String black = game.blackUsername();

            if (white == null) {
                white = "open";
            }

            if (black == null) {
                black = "open";
            }

            answer += (i + 1)
                    + ". "
                    + game.gameName()
                    + " | white: "
                    + white
                    + " | black: "
                    + black
                    + "\n";
        }

        return answer;
    }

    private String join(String[] stuff) throws ResponseException {
        if (stuff.length != 3) {
            return "Usage: join <GAME NUMBER> <WHITE|BLACK>";
        }

        if (games == null) {
            return "Use list first";
        }

        int number;

        try {
            number = Integer.parseInt(stuff[1]);
        } catch (NumberFormatException error) {
            return "Game number has to be a number";
        }

        if (number < 1 || number > games.size()) {
            return "That game isn't in the list";
        }

        String color = stuff[2].toUpperCase();

        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            return "Color has to be WHITE or BLACK";
        }

        GameData game = games.get(number - 1);

        server.joinGame(token, color, game.gameID());

        currentGame = null;
        gameID = game.gameID();
        side = color;
        observing = false;

        try {
            websocket = new WebSocketFacade(port, this);
            websocket.connect(token, gameID);
            inGame = true;

        } catch (ResponseException error) {
            if (websocket != null) {
                websocket.close();
            }

            websocket = null;
            currentGame = null;
            gameID = 0;
            side = null;
            observing = false;
            inGame = false;

            throw error;
        }

        return "Joined " + game.gameName() + " as " + color;
    }

    private String observe(String[] stuff) throws ResponseException {
        if (stuff.length != 2) {
            return "Usage: observe <GAME NUMBER>";
        }

        if (games == null) {
            return "Use list first";
        }

        int number;

        try {
            number = Integer.parseInt(stuff[1]);
        } catch (NumberFormatException error) {
            return "Game number has to be a number";
        }

        if (number < 1 || number > games.size()) {
            return "That game isn't in the list";
        }

        GameData game = games.get(number - 1);

        currentGame = null;
        gameID = game.gameID();
        side = "WHITE";
        observing = true;

        try {
            websocket = new WebSocketFacade(port, this);
            websocket.connect(token, gameID);
            inGame = true;

        } catch (ResponseException error) {
            if (websocket != null) {
                websocket.close();
            }

            websocket = null;
            currentGame = null;
            gameID = 0;
            side = null;
            observing = false;
            inGame = false;

            throw error;
        }

        return "Observing " + game.gameName();
    }

    private String redraw() {
        if (currentGame == null) {
            return "Game hasn't loaded yet";
        }

        drawer.draw(currentGame.game().getBoard(), side);

        return "Board redrawn";
    }

    private String highlight(String[] stuff) {
        if (stuff.length != 2) {
            return "Usage: highlight <SQUARE>";
        }

        if (currentGame == null) {
            return "Game hasn't loaded yet";
        }

        if (stuff[1].length() != 2) {
            return "Square should look like e2";
        }

        int col = Character.toLowerCase(stuff[1].charAt(0)) - 'a' + 1;
        int row;

        try {
            row = Integer.parseInt(stuff[1].substring(1));
        } catch (NumberFormatException error) {
            return "Square should look like e2";
        }

        if (row < 1 || row > 8 || col < 1 || col > 8) {
            return "Square has to be on the board";
        }

        ChessPosition position = new ChessPosition(row, col);

        if (currentGame.game().getBoard().getPiece(position) == null) {
            return "There isn't a piece there";
        }

        List<ChessPosition> highlights = new ArrayList<>();
        highlights.add(position);

        var moves = currentGame.game().validMoves(position);

        for (ChessMove move : moves) {
            highlights.add(move.getEndPosition());
        }

        drawer.draw(
                currentGame.game().getBoard(),
                side,
                highlights
        );

        return "Legal moves highlighted";
    }

    private String move(String[] stuff) throws ResponseException {
        if (observing) {
            return "Observers cannot move";
        }

        if (currentGame == null) {
            return "Game hasn't loaded yet";
        }

        if (stuff.length != 3 && stuff.length != 4) {
            return "Usage: move <START> <END> [QUEEN|ROOK|BISHOP|KNIGHT]";
        }

        if (stuff[1].length() != 2 || stuff[2].length() != 2) {
            return "Squares should look like e2 e4";
        }

        int startCol = Character.toLowerCase(stuff[1].charAt(0)) - 'a' + 1;
        int endCol = Character.toLowerCase(stuff[2].charAt(0)) - 'a' + 1;

        int startRow;
        int endRow;

        try {
            startRow = Integer.parseInt(stuff[1].substring(1));
            endRow = Integer.parseInt(stuff[2].substring(1));

        } catch (NumberFormatException error) {
            return "Squares should look like e2 e4";
        }

        if (startRow < 1 || startRow > 8
                || endRow < 1 || endRow > 8
                || startCol < 1 || startCol > 8
                || endCol < 1 || endCol > 8) {
            return "Squares have to be on the board";
        }

        ChessPiece.PieceType promotion = null;

        if (stuff.length == 4) {
            try {
                promotion = ChessPiece.PieceType.valueOf(
                        stuff[3].toUpperCase()
                );

            } catch (IllegalArgumentException error) {
                return "Promotion has to be QUEEN, ROOK, BISHOP, or KNIGHT";
            }

            if (promotion == ChessPiece.PieceType.KING
                    || promotion == ChessPiece.PieceType.PAWN) {
                return "Promotion has to be QUEEN, ROOK, BISHOP, or KNIGHT";
            }
        }

        ChessPosition start = new ChessPosition(startRow, startCol);
        ChessPosition end = new ChessPosition(endRow, endCol);

        ChessMove move = new ChessMove(
                start,
                end,
                promotion
        );

        websocket.makeMove(token, gameID, move);

        return "Move submitted";
    }

    private String resign() throws ResponseException {
        if (observing) {
            return "Observers cannot resign";
        }

        System.out.print("Are you sure you want to resign? yes/no: ");
        String answer = scanner.nextLine();

        if (!answer.equalsIgnoreCase("yes")) {
            return "Resign cancelled";
        }

        websocket.resign(token, gameID);

        return "Resign submitted";
    }

    private String leave() {
        boolean serverGotLeave = true;

        if (websocket != null) {
            try {
                websocket.leave(token, gameID);
            } catch (ResponseException error) {
                serverGotLeave = false;
            }

            websocket.close();
        }

        inGame = false;
        observing = false;
        currentGame = null;
        websocket = null;
        side = null;
        gameID = 0;

        // make the user list again so the game numbers and open colors are fresh
        games = null;

        if (!serverGotLeave) {
            return "Left locally, but the server connection was already gone";
        }

        return "Left the game";
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
                join <GAME NUMBER> <WHITE|BLACK>
                observe <GAME NUMBER>
                logout
                help
                quit
                """;
    }

    private String gameHelp() {
        return """
                move <START> <END> [QUEEN|ROOK|BISHOP|KNIGHT]
                highlight <SQUARE>
                redraw
                resign
                leave
                help
                """;
    }

    private String observerHelp() {
        return """
                highlight <SQUARE>
                redraw
                leave
                help
                """;
    }

    private synchronized void printPrompt() {
        System.out.print(">>> ");
    }

    @Override
    public synchronized void notify(ServerMessage message) {
        System.out.println();

        if (message instanceof LoadGameMessage loadMessage) {
            currentGame = loadMessage.getGame();

            if (currentGame == null || currentGame.game() == null) {
                System.out.println("Error: server sent an empty game");
                System.out.print(">>> ");
                return;
            }

            if (side == null) {
                side = "WHITE";
            }

            drawer.draw(
                    currentGame.game().getBoard(),
                    side
            );

        } else if (message instanceof ErrorMessage errorMessage) {
            System.out.println(errorMessage.getErrorMessage());

        } else if (message instanceof NotificationMessage notificationMessage) {
            System.out.println(notificationMessage.getMessage());

        } else {
            System.out.println("Error: unknown message from server");
        }

        System.out.print(">>> ");
    }
}