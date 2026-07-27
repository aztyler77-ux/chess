package client;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import model.AuthData;
import model.UserData;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public void clear() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        UserData user = new UserData(username, password, email);
        var request = buildRequest("POST", "/user", user, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        var request = HttpRequest.newBuilder().uri(URI.create(serverUrl + path));
        if (authToken != null) {
            request.header("authorization", authToken);
        }

        if (body != null) {
            String json = new Gson().toJson(body);
            request.header("Content-Type", "application/json");
            request.method(method, HttpRequest.BodyPublishers.ofString(json));
        } else {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        }

        return request.build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception error) {
            throw new ResponseException("Couldn't reach the server");
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass)
            throws ResponseException {
        if (response.statusCode() / 100 != 2) {
            var error = new Gson().fromJson(response.body(), ErrorResponse.class);
            throw new ResponseException(error.message());
        }

        if (responseClass == null) {return null;}
        return new Gson().fromJson(response.body(), responseClass);
    }

    private record ErrorResponse(String message) {
    }
}