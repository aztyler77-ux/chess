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
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5T9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVx0UBQwAA1jAALLZVRIBwwWEARx8SFhHBgADNUAgOAAKVQASkwbE43FgpXuN2eMAAQsAOBkcWowABRAAeKmwBAK12qlDu92K5nqgScTmG4zm6mA9nm9TGvKg3jqMD0BOxqRyULMnBpj3pd0qN1E9VhWRylDJGQdYEoXKNYGpNpUVpk2iUKnU9TVYAAqgMyW8PihqbJ5IG1Ko-cZ6gAxdEEiOUePAPWWGDR2ZiYySyq5xPBmDZqC53owNA+BAITAwuGIhSFgYwTZIMDxGD97JdqbFmDABCwjkFlD87JgDQV5RJstVEQqeo13Ni9eGRmVWlPXUKSdwjiWdoI9ACoUiwqHy37tc1eonqfny-w6+ClDC-KFZkGSlZAZRgOVgkVMZlVUVV1WWLUdXqCd3wLeQvzQE10A4TAlyDZMnx9FB6kbZsdyoUQ-VwpN6hAKc3RrKMBlzOMA2XdQUx0V8OCzJjtE0HRVyoqtVB8NjF1YvDV0I+oRLEsiKPuITVBouiUAUHx+zJYANPiZicIkld7lTGAFG4kydO3UsnyUmTRLw-SEzEqTqltGBZPswjVwfFkXXnd01CbLBvKA61xV1V4BhjL4QW0-t2ggdCviuQDV2lDB6nCeVIKLT5IRinT4sSyEriwzxvD8fxoHYNUYjTOBeWkOBOwAGQgLJCjS5gmTC+pmjaLpegMdR-2yyLi2i9Z9D+HYkrIiV7mCl4JjG3L9hBX5-kBfZoSnREUWGjFDTxFACWJFBSQpalguc6gWXZTkUG5HIb1-O85pC+AQPSsCstGKCk1ghZ4O1aB6n1TFHs9TCzWu7rd3qBA2vRMlWvaj0eW9FzfUUgyq1DBictjBzFCcozOJgDNOGrXj5HzEd3mLfjBNx5TqZzbQYGgBsmxbNsERgdp4mHQmjAgNQ0AAcmYWd52Jyt8Lh8iN250jPKfRbq2mHToCQAAvE6Xr-UVYcVlkwy1-sdf1jhDbelKn062UnAARkg6DAY1BDQcLC34itk7oewpSbqVoi2drPi1dC+H4AgGZeEMftDGwbxyI4E6YA2nZBwgNzIHxTOps21tdoF+Jk6L7OODF1RJeYY4wBAeJ5OxhaLTN33-Ztn8jfvduPsAzdO6gPWDZ7u2wtSr6wCd12-vdtUgc1EHdR8YfR92EqzSj59XLgVTzP7bcd5smBaLhN11M02LdO0FjHMksmuIJK-b-kJnrJZ2y5ODgisbD9ySYW57jbnSFkqN0QZBEggIK-cQ4sgiqOVawJlg30KugWa9tgIlBnjATKCo-qEwmmMNBCUMHFVNNhTAXhfABC8CgdAMQ4iJHoYw1GvgsCdT9IPRo0heTNV5O0Xk3QehDTRAUYYN8rZ5AKEhAqZCAKT1AUeORcUFHmjAQyaOocEZtQ4SjPRGl0Y5ExruSiX8YChlflpeR6F74k0fpUYylMX4WQ5joAspD0Ifxxg-aih8355i5iRXmpcvGMOHCgPs5dYAzFROiRwXN0RUAnEgIOLMQ6uRCcAry-ch5SJHtbW2-4NFHngbqc2BSN7FNFFgyojsfpzyVADRensV71DXlU62gc5ak0Vq5V+x9-7mL8VWdO3BL46RsWouxvTHFGHJtIc6KA3QBMsgJT+ozWaAPUHMwy-TlY7I0GrZR9J6jsKMQFGBpTHym3CslJR2DQL4MVFvKhNCKoF38NgdEiJmrzhgAAcWLBoLhdzeqAsESI+wxZJHa0KTItAqj4joMUbdLRDxNHItRTc+a2jXLIByMC5UKN5zErUMYr0OTfEOP8VYqZ4S0D2PlhxdMmY1nuM8bY9APjywWMGRzYJPMS7tnHNytAMBInRIlHEg6iTYDJNSekrZmTlbZJOQePJmsulj1vCUk22iO46u7nq2pjz6nT1nm7FpcFl6IR9sanpv8DlhwFe-E+FjCVgHJaoMkzLSZOPJi48czYgXFnWczLZ5z5wAB5yW5nKHs9if8Y7-JyHG8N2hE0asxSomAabvUgqgYFXFA8eowHGDC5UCwGiVuLAASWkAsZ24RgiBBBJsIW9EVrqjWssZIoB4Q1iinlZYVaUAADlxrFU6A89FU8cEZV+nW6ttRa1jHHY25trb23LE7X2FAw7p19rGAOkAQ6e3EPHVO5BUJZ2ULKrQ-wHAADsbgnAoCcDEXkwQ4D1QAGzwAPuSoo09uHlr6h0aFsL1660RdihRkFr3HsuO9P0GtGVIeLDe3tqG+aImQ3MSVyZZVogcKW8p9R7qUpqWi24DtLU-QIc0lUrTgb2vBoaHkPTYb4uVufOQKByVkn3hfITxZKWmNDiM2leMVnWMZf6+Zzj2VurzB4sVMyeVWRpfLV8bjaZCtIvhzTKKFHEZgFEpOsT4kYiSWgFJyBlWyYVnxsO6rhnqy1ZU+F1Tx76rgSml82rfNFP82a+dDHF2NOtax21XtV6wYDm8pNrnd7KzU9Stz9RROCbDXMIZZjdNiRUmJ4T469K-0Dc-fLKAI2bJcyGBt0hUuqrDpulrOaNa5bdOS4t1yDXPgQRu5r2621zvo0876Ly-odbG4EFL1DyoBEsOdRGmwmFIASGAVbzYIAbYAFIQHRLVmIZ74SgZweB9FvUmhhgGj0cdcLLYIrvAh9CkFsAIGAKtqAcAICIygHsAA6iwetQjvhzdqC28baHTkskw39L7P3KD-cByDsHEOQRQ5hwtkV-N9pkccEnTO33fto65lzDrFGgt3Q5DR8LdH5pTdwXKZj-04tLwS2DDkEMnpgB44F7LMAABWx20DCaO+iPrkMMZZf9FGyx8mGXiqU4ZarFNVMGfU1yrThQdN8sV2pzmsBskmcZRZqzMSYCkYSSbyVDmlWtaC1k4VXXvNJZNa9ALmjKMhZe3501TOPoNLlE0jnME2N2u9p00LyWH3OuF5lj1ivQzlea2r5NGvg3xo5uRPM1ODcK8a7VusRnQmisFsOcdotxZS0s3OHIzuXXETd55+HupqOy+eozmn4K2T0+73yXvdTPrRbZ7FyP8X2l6l51x40KXE-pbDrn91nnhdwHLuens5drODnLnzz0GcQAA6ScmZCZ5UJXkKCZwWhh59ugJCfwHFmL-TkHNf6lHfXynmnJ+b8Qefehqx4v+H41+tGcOLOTsEE88NqXOM+b+54H+iUKWKeJefgWgvWxYZIFWd8zuGuYY2AmBhgq+eYvY-Y++hgIshIXMJOBg3A4Ax+p+UAvKxeembkdkQCS+0kHBck7uWKMAUuEuEmVysCvutO9ykBFq0WM2Iwi2HydCUAP2m222XgyhiAcIsAwA2AX2hAiKl25g12wWDQfCAiQiIixgcOmqAhIA3AeAh+PINOwuthmhImdhUAkm8up8LheAfq+BCy9QSyEyhgE4CA4cuYawamawjwpefERep8RyzewuiR-BeaGheA-WYhZSEhLwE2zO0hzyy6byQAA
