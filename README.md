# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## Phase 2 Server Design
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5T9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVx0UBQwAA1jAALLZVRIBwwWEARx8SFhHBgADNUAgOAAKVQASkwbE43FgpXuN2eMAAQsAOBkcWowABRAAeKmwBAK12qlDu92K5nqgScTmG4zm6mA9nm9TGvKg3jqMD0BOxqRyULMnBpj3pd0qN1E9VhWRylDJGQdYEoXKNYGpNpUVpk2iUKnU9TVYAAqgMyW8PihqbJ5IG1Ko-cZ6gAxdEEiOUePAPWWGDR2ZiYySyq5xPBmDZqC53owNA+BAITAwuGIhSFgYwTZIMDxGD97JdqbFmDABCwjkFlD87JgDQV5RJstVEQqeo13Ni9eGRmVWlPXUKSdwjiWdoI9ACoUiwqHy37tc1eonqfny-w6+ClDC-KFZkGSlZAZRgOVgkVMZlVUVV1WWLUdXqCd3wLeQvzQE10A4TAlyDZMnx9FB6kbZsdyoUQ-VwpN6hAKc3RrKMBlzOMA2XdQUx0V8OCzJjtE0HRVyoqtVB8NjF1YvDV0I+oRLEsiKPuITVBouiUAUHx+zJYANPiZicIkld7lTGAFG4kydO3UsnyUmTRLw-SEzEqTqltGBZPswjVwfFkXXnd01CbLBvKA61xV1V4BhjL4QW0-t2ggdCviuQDV2lDB6nCeVIKLT5IRinT4sSyEriwzxvD8fxoHYNUYjTOBeWkOBOwAGQgLJCjS5gmTC+pmjaLpegMdR-2yyLi2i9Z9D+HYkrIiV7mCl4JjG3L9hBX5-kBfZoSnREUWGjFDTxFACWJFBSQpalguc6gWXZTkUG5HIb1-O85pC+AQPSsCstGKCk1ghZ4O1aB6n1TFHs9TCzWu7rd3qBA2vRMlWvaj0eW9FzfUUgyq1DBictjBzFCcozOJgDNOGrXj5HzEd3mLfjBNx5TqZzbQYGgBsmxbNsERgdp4mHQmjAgNQ0AAcmYWd52Jyt8Lh8iN250jPKfRbq2mHToCQAAvE6Xr-UVYcVlkwy1-sdf1jhDbelKn062UnAARkg6DAY1BDQcLC34itk7oewpSbqVoi2drPi1dC+H4HiFAQERDadlbXaBbjmAk4BUXxalmBjjAEB4nk7GFotM3ff9m2fyN+8y4+wDNwrqA9YN6u7bC1KvrAJ3Xb+921SBzUQd1Hwm5b3YSrNKPn1cuBVPM-tt2nmyYFouE3XUzTYt07QWMcySya4glN53+QmeslnbLk4OCKxsP3KTYu91LukWVR9EMhEhAgrrkOWQi0cq1gTLG3oVdAs17bARKN3GAmUFR-UJhNMYoCErgOKqabCmAvC+ACF4FA6AYhxESHgghqNfBYE6n6BujRpC8marydovJug9CGmiAowxt5WzyAUJCBVUEAQ7i-I8vC4r8PNK-Bk0dQ4IzauQlGsiNLoxyJjXclFL4wFDCfLSfD0J7xJgfSoxlKbHwshzHQBYUHoXPjjfe1EF6nzzFzEivNU6WIIcOFAfY46wBmKidEjguboioBOJAQcWYh1cs4p+Xk66N04c3a2tt-ziKPH-XU5t4njySaKSBlRHY-V7kqAGA9PbD3qKPTJ1tA5y1JorVyJ8l53zUbYqsHBzooA3jpbRojdE1IMUYcm0h2lunsZZASF8WmswfuoPphk6nK2mRoNWQj6T1DIYogK38UmPlNuFZKgioGgTgYqSemDsEVXxDEbA6JETNXnDAAA4sWDQlDdm9QeQw5h9hiwcO1gk7haARHxDAQI26kiHgSKBSC7Z80pGuWQDkJ5yoUbziRWoJRXpok2P0XYzRXS3FoD0fLDi6ZMyjLMRYnR6BrHlnUQ0jmTieYp3bOOKlaAYAeK8RKXxB0AmwCCSEsJkyInKyicsg8sTNaVNbreZJJspHl2lVXWVOSDl5K7j3N2xS4JD0Qj7JV1Sb7zLDvSs+y91EIrAGi1QZIiWk0MeTYx45myPOLGM5mky1nzgADxotzOUWZ7Fb4xzuTkX1brtABvFRC4RMBQ1WueZ-QKML649RgOMb5yoFgNAzcWAAktIBYztwjBECCCTYQt6IrXVGtZYyRQDwhrFFPKyxM0oAAHLjWKp0fZYLO7QIyr9XNWbag5rGG2gtRaS1luWBWvsKAm1dtrWMetCdF1AO+G2ztG6Lg9owWVHB-gOAAHY3BOBQE4GIvJghwHqgANngPPNFRQu5ULTX1DoXyflj11gCqF-DIJbqXZcd6foNYEsA8WbdNaQN80REBuYHLkw8rRA4FNaT6j3Qxdk0FtwHYap+vAopKoSnAz1eDQ0PJqmwzhcrNecgUBorJHPdejHiwYpUaHZpOK8YdK0QSu1-SjFktNXmcxrKenUqsti+Wr5TG00ZaRODEngX8KQzATx-ZuV+IxIEtAwTkBCp4wrWjYcxVNPVpKjJfysltzlb-YNL4pU2cSXZ1Vfb8MDoKVqkjOqvYjx-QHU5gaTMz2VqJrFpn6gsYY66uYjTVEybEipVjTG216Rvg6o+cWUDuomcZkM+bpAhZFWHCdxXo0axi26NFSatnyufP-cdRWp2lt7Xhw531jl-XK61wIwWsHlQCJYc6iNNiEKQAkMAI3mwQHGwAKQgOiHLMRV3whfdAt9YLepNDDANHobbfmW3+Xef96FILYAQMAEbUA4AQERlAPYAB1FgebGGbpa7UYtbXQMrJZBBv6l3ruUDuw957r33sgl6196dO0WX7VQ44LTGcrs3dB1zLm5X0OObuhybDbncPzU6zAuURH-q+cHv5sGHIIZPTANRhzUWYAACsltoCY4t9EtXIYY0i-6T1Gi+P4rZYJwyWWKYifk2JylknCjSdpQL0TnNYBROUwS9TmnvF5x03yjl+nBUlcc5EpllWrOBeVa9ezEiMPOeO7ZlVhOPr5LlIU8nMFSO6u9hUlzQX91GqZxF81AvQxpaK6LoN4unV+o5uRPMWP5f84KzlusimXEssFsONt2dVCS2lnOHIhvjXERNxZv7uosM8+egT7Hby2R48r3yavuTPpedJz593fmyl6hp5R40wX-dhbDtHs1FmmdwDjgnDlhJBzxDkKve7gTkxoWpcpwWhhe9ugJCABf-LkzITPKhK8uGFJl9fKeacn5vwO5rwq485+PxH5w794nTsIJ921ZTrv+-pyDiP9UoPSefgWgNWxYZI6Wu8hu4uYY2AwBhgw+eYvY-YM+hgIshIXMyOBg3A4AJ08+D2NKiesmbkdkj8A+0kRBckpukKMAnO7O7GmyP81uOOeyz+6qXm3WIwA25yuCUA12E2U2XgvBiAcIsAwA2Al2hAAKG25gW2TmDQtC9CjCzCxgv2EqVBIA3AeAtOno2OTO6hwhzGGhUAHGfOK8eheAtqkBAy9QQy3AIyE4CA4cuYawomawjwyefECeK8iyheTO3hlBsaQheAdWDBqSTBLw7WROrBRyQ6pyQAA
