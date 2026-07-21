package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class MySQLGameDAO implements GameDAO {
    private final Gson gson = new Gson();

    @Override
    public int createGame(String gameName) throws DataAccessException {
        String sql = "INSERT INTO games (gameName, game) VALUES (?, ?)";
        ChessGame chessGame = new ChessGame();
        String gameJson = gson.toJson(chessGame);

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, gameName);
            statement.setString(2, gameJson);
            statement.executeUpdate();

            try (var result = statement.getGeneratedKeys()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        } catch (SQLException e) {throw new DataAccessException("Could not create game", e);}

        throw new DataAccessException("Could not get game ID");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE gameID = ?";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setInt(1, gameID);

            try (var result = statement.executeQuery()) {
                if (result.next()) {
                    String whiteUsername = result.getString("whiteUsername");
                    String blackUsername = result.getString("blackUsername");
                    String gameName = result.getString("gameName");
                    String gameJson = result.getString("game");

                    ChessGame chessGame = gson.fromJson(gameJson, ChessGame.class);

                    return new GameData(
                            gameID,
                            whiteUsername,
                            blackUsername,
                            gameName,
                            chessGame
                    );
                }
            }
        } catch (SQLException e) {throw new DataAccessException("Could not get game", e);}
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM games";

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql);
             var result = statement.executeQuery()) {

            while (result.next()) {
                int gameID = result.getInt("gameID");
                String whiteUsername = result.getString("whiteUsername");
                String blackUsername = result.getString("blackUsername");
                String gameName = result.getString("gameName");
                String gameJson = result.getString("game");

                ChessGame chessGame = gson.fromJson(gameJson, ChessGame.class);

                GameData game = new GameData(
                        gameID,
                        whiteUsername,
                        blackUsername,
                        gameName,
                        chessGame
                );

                games.add(game);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not list games", e);
        }
        return games;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        if (gameData == null) {throw new DataAccessException("Game was null");}

        String sql = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";
        String gameJson = gson.toJson(gameData.game());

        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, gameData.whiteUsername());
            statement.setString(2, gameData.blackUsername());
            statement.setString(3, gameData.gameName());
            statement.setString(4, gameJson);
            statement.setInt(5, gameData.gameID());

            int gamesUpdated = statement.executeUpdate();
            if (gamesUpdated == 0) {
                throw new DataAccessException("Game not found");
            }
        } catch (SQLException e) {throw new DataAccessException("Could not update game", e);}
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (var connection = DatabaseManager.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {throw new DataAccessException("Could not clear games", e);}
    }
}