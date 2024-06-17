# Assignment 2

## Compilation

Using Java SE 21 and importing the external libraries in the libs folder:

### To compile every file:
```bash
javac -cp ".:libs/bcrypt-0.10.2.jar:libs/bytes-1.5.0.jar:libs/json-simple-1.1.1.wso2v2.jar:libs/spring-security-crypto-3.2.10.RELEASE.jar" Connection.java ClientHandler.java Server.java Database.java Player.java
```

### To run the server
```bash
java -cp ".:libs/bcrypt-0.10.2.jar:libs/bytes-1.5.0.jar:libs/json-simple-1.1.1.wso2v2.jar:libs/spring-security-crypto-3.2.10.RELEASE.jar" Server <PORT> <DATABASE>
```
- PORT must be a valid port, i.e. 8000;
- DATABASE must be a .json file

### To run a client
```bash
java -cp ".:libs/json-simple-1.1.1.wso2v2.jar" Connection localhost <PORT>
```
- PORT must be a valid port where a server is running, i.e. 8000;

## Database

The database used by the server is a .json file with the following structure:
```json
{"database":[
    {
    "password":"$2a$10$xCx0okcv8H8wz\/Emae\/8oOAEAd..5WEItmcxkqHm8SMSXPYjIrIP2",
    "elo":832,
    "username":"Pedro",
    "token":"$2a$10$8xhKsLc0oXH9Hhp1ZnPiXujbtmF7.1dwGOLs8XQYYd94B1aVE4.92"
    },
    {
    "password":"$2a$10$6vn.2UDMrYp8..GmOyPMme09VLKIHSqjJtdOp6hZQC7t1lSkCnGAG",
    "elo":906,
    "username":"Maximo",
    "token":"$2a$10$aIU1fs.YtnlVb9xV617GUOqpkwQYHO353JQgePCSbeA79.D0S77GC"
    },
    {
    "password":"$2a$10$6.gtGUdnu4lIhqWE53ln1egOYP7Vzyf6fop3ktHDjF7PX0ms9Ays6",
    "elo":1102,
    "username":"Joao",
    "token":"$2a$10$XZeBxDdeVhATu0nk6I0lEemIkbumQPzn6FkC8\/lUL2lcHRbkeRwHi"
    }
]}
```

Every stored password is encrypted accordingly, as well as the token for each user. They result from the concatenation of the username and the password in order to increase security, randomness and uniqueness.

## Authentication process

After connecting to the server, the users are greeted with the following:
- 1 - Login
- 2 - Register
- 3 - Exit

Both login and register require the user to input credentials (username and password).

## Matchmaking modes

The game is available in two different modes:

### Simple

In the simple matchmaking mode, the players win and lose a game without altering their elo.

### Ranked

In the ranked matchmaking mode, the players are separated by ranks / elos; every win and loss count towards a player's elo: the higher, the better. Every win/loss increases the disparity between the two players by 100 elo points. In case of a tie, players keep their elo the same and the game ends without a winner.

## Queues

After logging in and choosing the game mode, players are thrown into the respective queue.

### Simple

The players are paired up by order of arrival in the queue, obeying a first come, "first served" principle.

### Ranked

Players are only paired up if their elo difference is within a certain limit allowed by the server in order to maintain fairness and competitiveness.

## Game

The game is the simple and traditional Rock, Paper, Scissors. Each user chooses and then the game is solved: Paper covers Rock, Rock smashes Scissors and Scissors cuts Paper.
