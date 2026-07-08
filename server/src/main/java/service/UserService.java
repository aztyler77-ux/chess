package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request)
            throws DataAccessException, BadRequestException, AlreadyTakenException {

        if (request == null) {throw new BadRequestException("Error: bad request");}
        if (isBlank(request.username())) {throw new BadRequestException("Error: bad request");}
        if (isBlank(request.password())) {throw new BadRequestException("Error: bad request");}
        if (isBlank(request.email())) {throw new BadRequestException("Error: bad request");}

        UserData existingUser = userDAO.getUser(request.username());
        if (existingUser != null) {throw new AlreadyTakenException("Error: already taken");}

        UserData newUser = new UserData(request.username(), request.password(), request.email());
        userDAO.createUser(newUser);
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, request.username());
        authDAO.createAuth(authData);
        return new RegisterResult(request.username(), authToken);
    }

    public LoginResult login(LoginRequest request) throws DataAccessException, BadRequestException, UnauthorizedException {
        if (request == null ||
            isBlank(request.username()) ||
            isBlank(request.password())
        ) {
            throw new BadRequestException("Error: bad request");
        }

        UserData user = userDAO.getUser(request.username());

        if (user == null ||
            !request.password().equals(user.password())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, request.username());
        authDAO.createAuth(authData);
        return new LoginResult(request.username(), authToken);
    }

    public void logout (LogoutRequest request) throws DataAccessException, UnauthorizedException {
        if (request == null ||
            isBlank(request.authToken())) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        AuthData authData = authDAO.getAuth(request.authToken());

        if (authData == null) {
            throw new UnauthorizedException("Error: unauthorized");
        }

        authDAO.deleteAuth(request.authToken());
    }

    // helper method to detect if string value is missing or blank
    private boolean isBlank(String value) {return value == null || value.isBlank();}
}