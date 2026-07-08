package server;
import io.javalin.Javalin;

public class Server {
    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // endpoints will  be registered here later
    }

    public int run(int targetPort) {
        javalin.start(targetPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}