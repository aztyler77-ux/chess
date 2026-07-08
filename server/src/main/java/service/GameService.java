package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResult createGame(CreateGameRequest request)
            throws DataAccessException, BadRequestException, UnauthorizedException {

        if (request == null || isBlank(request.gameName())) {
            throw new BadRequestException("Error: bad request");
        }

        if (isBlank(request.authToken())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        AuthData authData = authDAO.getAuth(request.authToken());
        if (authData == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        int gameID = gameDAO.createGame(request.gameName());
        return new CreateGameResult(gameID);
    }

    public ListGamesResult listGames(ListGamesRequest request)
            throws DataAccessException, UnauthorizedException {
        if (request == null ||  isBlank(request.authToken())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        AuthData authData = authDAO.getAuth(request.authToken());

        if (authData == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }
        return new ListGamesResult(gameDAO.listGames());
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException, BadRequestException, UnauthorizedException, AlreadyTakenException {
        if (request == null) {throw new BadRequestException("Error: bad request");}
        if (isBlank(request.authToken())) {throw new UnauthorizedException("Error: unauthorized");}

        AuthData authData = authDAO.getAuth(request.authToken());
        if (authData == null) {throw new UnauthorizedException("Error: unauthorized");}
        if (request.gameID() == null) {throw new BadRequestException("Error: bad request");}
        if (!"WHITE".equals(request.playerColor()))
            if (!"BLACK".equals(request.playerColor())) {
                throw new BadRequestException("Error: bad request");}

        GameData gameData = gameDAO.getGame(request.gameID());
        if (gameData == null) {throw new BadRequestException("Error: bad request");}
        if ("WHITE".equals(request.playerColor())) {
            if (gameData.whiteUsername() != null) {
                throw new AlreadyTakenException("Error: already taken");
            }

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    authData.username(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()
            );
            gameDAO.updateGame(updatedGame);
        }

        if ("BLACK".equals(request.playerColor())) {
            if (gameData.blackUsername() != null) {
                throw new AlreadyTakenException("Error: already taken");
            }

            GameData updatedGame = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    authData.username(),
                    gameData.gameName(),
                    gameData.game()
            );
            gameDAO.updateGame(updatedGame);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}