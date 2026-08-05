package websocket;

import chess.ChessGame;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class WebSocketHandler {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = gson.fromJson(
                    ctx.message(),
                    UserGameCommand.class
            );

            if (command == null
                    || command.getCommandType() == null
                    || command.getAuthToken() == null
                    || command.getAuthToken().isBlank()
                    || command.getGameID() == null) {
                connections.send(ctx, new ErrorMessage("Error: bad command"));
                return;
            }

            if (command.getCommandType() == UserGameCommand.CommandType.CONNECT) {
                connect(ctx, command);

            } else if (command.getCommandType() == UserGameCommand.CommandType.MAKE_MOVE) {
                MakeMoveCommand moveCommand = gson.fromJson(
                        ctx.message(),
                        MakeMoveCommand.class
                );

                makeMove(ctx, moveCommand);

            } else if (command.getCommandType() == UserGameCommand.CommandType.RESIGN) {
                resign(ctx, command);

            } else if (command.getCommandType() == UserGameCommand.CommandType.LEAVE) {
                leave(ctx, command);

            } else {
                connections.send(ctx, new ErrorMessage("Error: unknown command"));
            }

        } catch (Exception e) {
            connections.send(ctx, new ErrorMessage("Error: bad command"));
        }
    }

    public void onClose(WsContext ctx) {
        connections.remove(ctx);
    }

    private void connect(WsMessageContext ctx, UserGameCommand command) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());

            if (auth == null) {
                connections.send(ctx, new ErrorMessage("Error: bad auth token"));
                return;
            }

            GameData game = gameDAO.getGame(command.getGameID());

            if (game == null) {
                connections.send(ctx, new ErrorMessage("Error: game not found"));
                return;
            }

            connections.add(game.gameID(), auth.username(), ctx);
            connections.send(ctx, new LoadGameMessage(game));

            String message;

            if (auth.username().equals(game.whiteUsername())) {
                message = auth.username() + " joined as white";
            } else if (auth.username().equals(game.blackUsername())) {
                message = auth.username() + " joined as black";
            } else {
                message = auth.username() + " joined as an observer";
            }

            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(message),
                    ctx
            );

        } catch (DataAccessException e) {
            connections.send(
                    ctx,
                    new ErrorMessage("Error: " + e.getMessage())
            );
        }
    }

    private synchronized void makeMove(
            WsMessageContext ctx,
            MakeMoveCommand command
    ) {
        try {
            if (command == null
                    || command.getMove() == null
                    || command.getMove().getStartPosition() == null
                    || command.getMove().getEndPosition() == null) {
                connections.send(ctx, new ErrorMessage("Error: invalid move"));
                return;
            }

            AuthData auth = authDAO.getAuth(command.getAuthToken());

            if (auth == null) {
                connections.send(ctx, new ErrorMessage("Error: bad auth token"));
                return;
            }

            GameData game = gameDAO.getGame(command.getGameID());

            if (game == null) {
                connections.send(ctx, new ErrorMessage("Error: game not found"));
                return;
            }

            if (!connections.connected(
                    game.gameID(),
                    auth.username(),
                    ctx
            )) {
                connections.send(
                        ctx,
                        new ErrorMessage("Error: connect to the game first")
                );
                return;
            }

            boolean whitePlayer = auth.username().equals(game.whiteUsername());
            boolean blackPlayer = auth.username().equals(game.blackUsername());

            if (!whitePlayer && !blackPlayer) {
                connections.send(
                        ctx,
                        new ErrorMessage("Error: observers cannot move")
                );
                return;
            }

            if (game.game().isGameOver()) {
                connections.send(
                        ctx,
                        new ErrorMessage("Error: game is already over")
                );
                return;
            }

            ChessGame.TeamColor turn = game.game().getTeamTurn();

            if (turn == ChessGame.TeamColor.WHITE && !whitePlayer) {
                connections.send(ctx, new ErrorMessage("Error: not your turn"));
                return;
            }

            if (turn == ChessGame.TeamColor.BLACK && !blackPlayer) {
                connections.send(ctx, new ErrorMessage("Error: not your turn"));
                return;
            }

            game.game().makeMove(command.getMove());

            ChessGame.TeamColor nextTurn = game.game().getTeamTurn();
            String statusMessage = null;

            if (game.game().isInCheckmate(nextTurn)) {
                game.game().setGameOver(true);

                if (nextTurn == ChessGame.TeamColor.WHITE) {
                    statusMessage = game.whiteUsername() + " is in checkmate";
                } else {
                    statusMessage = game.blackUsername() + " is in checkmate";
                }

            } else if (game.game().isInStalemate(nextTurn)) {
                game.game().setGameOver(true);
                statusMessage = "game ended in stalemate";

            } else if (game.game().isInCheck(nextTurn)) {
                if (nextTurn == ChessGame.TeamColor.WHITE) {
                    statusMessage = game.whiteUsername() + " is in check";
                } else {
                    statusMessage = game.blackUsername() + " is in check";
                }
            }

            GameData updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );

            gameDAO.updateGame(updatedGame);

            connections.broadcast(
                    game.gameID(),
                    new LoadGameMessage(updatedGame),
                    null
            );

            ChessPosition start = command.getMove().getStartPosition();
            ChessPosition end = command.getMove().getEndPosition();

            char startCol = (char) ('a' + start.getColumn() - 1);
            char endCol = (char) ('a' + end.getColumn() - 1);

            String message = auth.username()
                    + " moved "
                    + startCol
                    + start.getRow()
                    + " to "
                    + endCol
                    + end.getRow();

            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(message),
                    ctx
            );

            if (statusMessage != null) {
                connections.broadcast(
                        game.gameID(),
                        new NotificationMessage(statusMessage),
                        null
                );
            }

        } catch (InvalidMoveException e) {
            connections.send(ctx, new ErrorMessage("Error: invalid move"));

        } catch (DataAccessException e) {
            connections.send(
                    ctx,
                    new ErrorMessage("Error: " + e.getMessage())
            );

        } catch (Exception e) {
            connections.send(
                    ctx,
                    new ErrorMessage("Error: couldn't make move")
            );
        }
    }

    private synchronized void resign(
            WsMessageContext ctx,
            UserGameCommand command
    ) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());

            if (auth == null) {
                connections.send(ctx, new ErrorMessage("Error: bad auth token"));
                return;
            }

            GameData game = gameDAO.getGame(command.getGameID());

            if (game == null) {
                connections.send(ctx, new ErrorMessage("Error: game not found"));
                return;
            }

            if (!connections.connected(
                    game.gameID(),
                    auth.username(),
                    ctx
            )) {
                connections.send(
                        ctx,
                        new ErrorMessage("Error: connect to the game first")
                );
                return;
            }

            if (!auth.username().equals(game.whiteUsername())
                    && !auth.username().equals(game.blackUsername())) {
                connections.send(
                        ctx,
                        new ErrorMessage("Error: observers cannot resign")
                );
                return;
            }

            if (game.game().isGameOver()) {
                connections.send(
                        ctx,
                        new ErrorMessage("Error: game is already over")
                );
                return;
            }

            game.game().setGameOver(true);

            GameData updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game()
            );

            gameDAO.updateGame(updatedGame);

            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(auth.username() + " resigned"),
                    null
            );

        } catch (DataAccessException e) {
            connections.send(
                    ctx,
                    new ErrorMessage("Error: " + e.getMessage())
            );

        } catch (Exception e) {
            connections.send(
                    ctx,
                    new ErrorMessage("Error: couldn't resign")
            );
        }
    }

    private synchronized void leave(
            WsMessageContext ctx,
            UserGameCommand command
    ) {
        try {
            AuthData auth = authDAO.getAuth(command.getAuthToken());

            if (auth == null) {
                connections.send(ctx, new ErrorMessage("Error: bad auth token"));
                return;
            }

            GameData game = gameDAO.getGame(command.getGameID());

            if (game == null) {
                connections.send(ctx, new ErrorMessage("Error: game not found"));
                return;
            }

            if (!connections.connected(
                    game.gameID(),
                    auth.username(),
                    ctx
            )) {
                connections.send(
                        ctx,
                        new ErrorMessage("Error: connect to the game first")
                );
                return;
            }

            String white = game.whiteUsername();
            String black = game.blackUsername();

            if (auth.username().equals(white)) {
                white = null;
            } else if (auth.username().equals(black)) {
                black = null;
            }

            GameData updatedGame = new GameData(
                    game.gameID(),
                    white,
                    black,
                    game.gameName(),
                    game.game()
            );

            gameDAO.updateGame(updatedGame);
            connections.remove(game.gameID(), ctx);

            connections.broadcast(
                    game.gameID(),
                    new NotificationMessage(
                            auth.username() + " left the game"
                    ),
                    null
            );

        } catch (DataAccessException e) {
            connections.send(
                    ctx,
                    new ErrorMessage("Error: " + e.getMessage())
            );

        } catch (Exception e) {
            connections.send(
                    ctx,
                    new ErrorMessage("Error: couldn't leave game")
            );
        }
    }
}