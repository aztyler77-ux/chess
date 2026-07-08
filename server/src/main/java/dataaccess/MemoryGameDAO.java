package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(String gameName) throws DataAccessException {
        int gameID = nextGameID;
        nextGameID++;

        GameData gameData = new GameData(
                gameID,
                null,
                null,
                gameName,
                new ChessGame()
        );

        games.put(gameID, gameData);

        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        if (gameData == null || !games.containsKey(gameData.gameID())) {
            throw new DataAccessException("Game not found");
        }

        games.put(gameData.gameID(), gameData);
    }

    @Override
    public void clear() throws DataAccessException {
        games.clear();
        nextGameID = 1;
    }
}