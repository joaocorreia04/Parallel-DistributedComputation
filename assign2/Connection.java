import java.io.*;
import java.net.Socket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Connection {
    private enum State {
        LOGIN, PLAYING, POST_GAME
    }

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader stdIn;
    private State state;

    public Connection(String address, int port) {
        try {
            this.socket = new Socket(address, port);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.stdIn = new BufferedReader(new InputStreamReader(System.in));
            this.state = State.LOGIN;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(JSONObject request) {
        out.println(request.toJSONString());
    }

    public String getResponse() throws IOException {
        return in.readLine();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showMenu() {
        System.out.println("Welcome! Please choose an option:");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
    }

    public void handleUserInput() throws ParseException {
        String userInput;
        try {
            showMenu();
            userInput = stdIn.readLine().trim();
            if ("1".equals(userInput)) {
                handleLogin();
            } else if ("2".equals(userInput)) {
                handleRegister();
            } else if ("3".equals(userInput)) {
                System.out.println("Exiting...");
                close();
            } else {
                System.out.println("Invalid option, please try again.");
                handleUserInput();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private void handleLogin() throws IOException, ParseException {
        System.out.print("Enter username: ");
        String username = stdIn.readLine().trim();

        JSONObject request = new JSONObject();
        request.put("action", "login");
        request.put("username", username);

        sendRequest(request);

        String response = getResponse();
        handleServerResponse(response);

        if (response.contains("Username exists")) {
            System.out.print("Enter password: ");
            String password = stdIn.readLine().trim();

            request.put("password", password);
            sendRequest(request);

            response = getResponse();
            handleServerResponse(response);

            JSONObject jsonResponse = (JSONObject) new JSONParser().parse(response);
            String message = (String) jsonResponse.get("message");
            String elo = (String) jsonResponse.get("elo");

            if (message.contains("Login successful")) {
                System.out.println("Current elo: " + elo);
                state = State.PLAYING;
                promptMatchmakingType();
            }
        } else if (response.contains("User already logged in")) {
            System.out.println("User already logged in.");
            handleUserInput();
        } else {
            System.out.println("Username does not exist");
            handleUserInput();
        }
    }

    private void handleRegister() throws IOException, ParseException {
        System.out.print("Enter username: ");
        String username = stdIn.readLine().trim();

        JSONObject request = new JSONObject();
        request.put("action", "register");
        request.put("username", username);

        sendRequest(request);

        String response = getResponse();
        handleServerResponse(response);

        if (response.contains("Username already exists")) {
            System.out.println("Username already exists! Enter a different username.");
            handleUserInput();
        } else if (response.contains("Username accepted")) {
            System.out.print("Enter password: ");
            String password = stdIn.readLine().trim();

            request.put("password", password);
            sendRequest(request);

            response = getResponse();
            handleServerResponse(response);

            if (response.contains("Registration successful")) {
                handleUserInput();
            }
        } else {
            System.out.println("Registration failed, please try again.");
            handleUserInput();
        }
    }

    private void handleServerResponse(String response) throws IOException {
        try {
            JSONObject jsonResponse = (JSONObject) new JSONParser().parse(response);
            String message = (String) jsonResponse.get("message");
            System.out.println("Response: " + message);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void promptMatchmakingType() throws IOException {
        System.out.println("Choose matchmaking type:");
        System.out.println("1. Simple");
        System.out.println("2. Ranked");

        String matchmakingType = stdIn.readLine().trim();
        JSONObject request = new JSONObject();
        request.put("action", "queue");
        request.put("matchmakingType", matchmakingType);

        sendRequest(request);

        String response = getResponse();
        handleServerResponse(response);

        if (response.contains("Waiting for an opponent") || response.contains("Matched with")) {
            playGame(response, matchmakingType);
        }
    }

    private void playGame(String response, String matchmakingType) throws IOException {
        if (response.contains("Waiting for an opponent")) {
            System.out.println("Waiting for an opponent...");
            String response2;
            while ((response2 = getResponse()) != null) {
                handleServerResponse(response2);
                if (response2.contains("Matched with")) {
                    System.out.println("Let's play Rock-Paper-Scissors!");
                    System.out.print("Enter your choice (rock, paper, scissors): ");
                    String userChoice = stdIn.readLine().trim();

                    JSONObject request = new JSONObject();
                    request.put("action", "play");
                    request.put("matchmakingType", matchmakingType);
                    request.put("choice", userChoice);

                    sendRequest(request);

                    handleGameResult();
                    break;
                }
            }
        } else if (response.contains("Matched with")) {
            System.out.println("Let's play Rock-Paper-Scissors!");
            System.out.print("Enter your choice (rock, paper, scissors): ");
            String userChoice = stdIn.readLine().trim();

            JSONObject request = new JSONObject();
            request.put("action", "play");
            request.put("matchmakingType", matchmakingType);
            request.put("choice", userChoice);

            sendRequest(request);

            handleGameResult();
        }
    }

    private void handleGameResult() throws IOException {
        String response = getResponse();
        try {
            JSONObject jsonResponse = (JSONObject) new JSONParser().parse(response);
            String opponentChoice = (String) jsonResponse.get("opponentChoice");
            String result = (String) jsonResponse.get("result");

            System.out.println("Opponent chose: " + opponentChoice);
            System.out.println("Result: " + result);

            state = State.POST_GAME;
            handleQueueOrExit();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void handleQueueOrExit() throws IOException {
        System.out.print("Game over. Would you like to join the queue again or exit? (queue/exit): ");
        String userInput = stdIn.readLine().trim();

        JSONObject request = new JSONObject();
        request.put("action", userInput.equalsIgnoreCase("queue") ? "queue" : "exit");

        sendRequest(request);

        String response = getResponse();
        handleServerResponse(response);

        if (userInput.equalsIgnoreCase("queue")) {
            state = State.PLAYING;
            promptMatchmakingType();
        } else {
            System.out.println("Exiting...");
            close();
        }
    }

    public static void main(String[] args) throws ParseException {
        if (args.length < 2) {
            System.out.println("Usage: java Connection <address> <port>");
            return;
        }

        String address = args[0];
        int port = Integer.parseInt(args[1]);

        Connection client = new Connection(address, port);
        client.handleUserInput();
    }
}
