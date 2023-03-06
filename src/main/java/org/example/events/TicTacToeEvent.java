package org.example.events;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicTacToeEvent extends ListenerAdapter {
    
    // Define constant variables
    private static String[][] board = new String[3][3];
    private static String BOT_ID;
    private static String GUILD_ID;
    private static String TICTACTOE_CHANNEL_ID;
    private static String PREFIX;
    private static Guild guild;
    private static TextChannel tictactoeChannel;
    private static String[] Players = {"X", "O"};
    
    // Define variables
    private static String[] PlayersIDs = new String[2];
    private static String ai;
    private static String human;
    private static boolean isGameInProgress = false;
    private static String sentMessageId;

    // Constructor that initializes the value of API_KEY
    public TicTacToeEvent(String GUILD_ID, String TICTACTOE_CHANNEL_ID, String BOT_ID) {
        TicTacToeEvent.GUILD_ID = GUILD_ID;
        TicTacToeEvent.TICTACTOE_CHANNEL_ID = TICTACTOE_CHANNEL_ID;
        TicTacToeEvent.BOT_ID = BOT_ID;
    }

    // Define a static method for updating the command prefix
    public static void updatePrefix(String newPrefix) {
        PREFIX = newPrefix;
    }

    // Method is called when the bot is ready
    @Override
    public void onReady(ReadyEvent event) {
        guild = event.getJDA().getGuildById(GUILD_ID);
        tictactoeChannel = guild.getTextChannelById(TICTACTOE_CHANNEL_ID);
    }

    // Overriding the onMessageReceived method to handle incoming messages
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        
        if(!event.getChannel().asTextChannel().equals(tictactoeChannel)){
            return;
        }
        if (event.getMember().getUser().isBot()) {
            return;
        }   
        // Splitting the message into an array of strings
        String[] message = event.getMessage().getContentRaw().split("\\s+");
        // Checking if the first string in the message array is the trivia command
        if (message[0].equals(PREFIX + "TTT")) {
            handleTTTCommand(message);
            return;
        }
    }

    // Method handles TicTacToe commands
    public static void handleTTTCommand(String[] message) {
        // Returns if command is not !TTT [help, newgame, other]
        if (message.length != 2){
            tictactoeChannel.sendMessage("**Wrong command**").queue();
            return;
        }
        switch (message[1].toLowerCase()) {
            // Handles case !TTT help
            case "help":
                String helpMessage = String.format("Here are the available commands for %sTTT:\n\n" +
                "%sTTT newGame(n) - Starts a new Tictactoe game.\n" + 
                "%sTTT stop - *Upcoming feature*\n\n" + 
                "Good luck and have fun!", PREFIX, PREFIX,PREFIX);
                tictactoeChannel.sendMessage(helpMessage).queue();
                return;
            // Handles case !TTT help (Upcoming feature)
            /*case "stop":
                tictactoeChannel.sendMessage("**Tictactoe game Stoped**").queue();

                return;*/
            // Handles case !TTT newGame
            case "n":
            case "newgame":
                if (isGameInProgress) {
                    tictactoeChannel.sendMessage("**Game is in progress**").queue();
                    return;
                }
                startNewGame();
                return;
            // Handles case !TTT other
            default:
                tictactoeChannel.sendMessage("**Wrong command**").queue();
                return;
        }
        
    }

    // Method starts a new game
    public static void startNewGame() {
        tictactoeChannel.sendMessage("You will have 5 seconds to input answers (or I will ask you again).").queue();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resetTheBoard();
        deleteLastBotMessage();
        tictactoeChannel.sendMessage("How many players [1/2]?").queue();
        int currentNumberOfPlayers;
        while (true) {
            currentNumberOfPlayers = getNumberFromPlayer();
            if (currentNumberOfPlayers == 1 || currentNumberOfPlayers == 2) {
                break;
            }
        }
        deleteLastBotMessage();
        if (currentNumberOfPlayers == 1) {
            handleGameForOne();
        } else {
            handleGameForTwo();
        }
    }

    // Method starts a new game for one
    public static void handleGameForOne() { 
        tictactoeChannel.sendMessage("Which player do you want to play as [1/2]?").queue();
        int whichPlayer;
        while (true) {
            whichPlayer = getNumberFromPlayer();
            if (whichPlayer == 1 || whichPlayer == 2) {
                break;
            }
        }
        deleteLastBotMessage();
        human = Players[whichPlayer-1];
        PlayersIDs[whichPlayer-1] = getResponse().getAuthor().getId();
        printTheBoard(true);
        for (int i = 0; i < 9; i++) {
            if (i%2 == 0) {
                doMove(i%2);
                printTheBoard(false);
                if (checkIfGameOver() != null) {
                    handleGameEnd();
                    return;
                }
            } else {
                doMove(i%2);
                printTheBoard(false);
                if (checkIfGameOver() != null) {
                    handleGameEnd();
                    return;
                }
            } 
        }
        handleGameEnd();
        return;
    }

    // Method does a move for bot or asks player for their move
    public static void doMove(int player) {
        if (PlayersIDs[player] == null) {
            ai = Players[player];
            botMove(player);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deleteLastBotMessage();
            return;
        }
        int[] input = getCoordinatesFromPlayer(player);
        deleteLastBotMessage();
        board[input[0]][input[1]] = Players[player];
    }

    // Method starts a new game for two
    public static void handleGameForTwo() { 
        getPlayer(0);
        getPlayer(1);
        printTheBoard(true);
        for (int i = 0; i < 9; i++) {
            if (i%2 == 0) {
                doMove(i%2);
                printTheBoard(false);
                if (checkIfGameOver() != null) {
                    handleGameEnd();
                    return;
                }
            } else {
                doMove(i%2);
                printTheBoard(false);
                if (checkIfGameOver() != null) {
                    handleGameEnd();
                    return;
                }
            }
        }
        handleGameEnd();
        return;
    }

    // Method calculates a bot move
    public static void botMove(int player) {
        int bestScore = Integer.MIN_VALUE;
        int [] Move = new int[2];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == " ") {
                    board[i][j] = ai;
                    int score = minimax(board, 0, false);
                    board[i][j] = " ";
                    if (bestScore < score) {
                        bestScore = score;
                        Move = new int[] {i, j};
                    }
                }
            }
        }
        board[Move[0]][Move[1]] = ai;
        tictactoeChannel.sendMessage("Bot played " + (Move[0] + 1) + ", " + (Move[1] + 1) + "." ).complete();
    }

    // Method is minimax algorithm for calculating bot moves
    public static int minimax(String[][] board, int depth, boolean isMax) {
        String result = checkIfGameOver();
        if (result != null) {
            if (result == "Tie"){
                return 0;
            } else if (result == ai) {
                return 1;
            } else {
                return -1;
            }
        }
        if (isMax) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board.length; j++) {
                    if (board[i][j] == " ") {
                        board[i][j] = ai;
                        int score = minimax(board, depth + 1, false);
                        board[i][j] = " ";
                        bestScore = Math.max(bestScore, score);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board.length; j++) {
                    if (board[i][j] == " ") {
                        board[i][j] = human;
                        int score = minimax(board, depth + 1, true);
                        board[i][j] = " ";
                        bestScore = Math.min(bestScore, score);
                    }
                }
            }
            return bestScore;
        }
    }

    // Method ends a game and resets variables
    public static void handleGameEnd() {
        switch (checkIfGameOver()) {
            case "X":
                tictactoeChannel.sendMessage("**GAME END, player 1 won**").queue();
                break;
            case "O":
                tictactoeChannel.sendMessage("**GAME END, player 1 won**").queue();
                break;
            case "Tie":
                tictactoeChannel.sendMessage("**GAME END, it's a tie**").queue();
                break;
        }
        PlayersIDs = new String[2];
        isGameInProgress = false;
        sentMessageId = null;
        human = null;
        ai = null;
        return;
    }

    // Method gets coordinate input from player
    public static int[] getCoordinatesFromPlayer(int playerNum) {
        Message message;
        int[] coordinates = null;
        while (true) {
            if (coordinates == null) {
                tictactoeChannel.sendMessage("Player " + (playerNum + 1)+ " input your choice like \"y, x\": ").queue();
            } else {
                deleteLastBotMessage();
                tictactoeChannel.sendMessage("Invalid input, player " + (playerNum + 1) + " input your choice like \"y, x\": ").queue();
            }
            message = getResponse();
            String Author = message.getAuthor().getId();
            coordinates = getNumsFromString(message.getContentRaw());
            if (!Author.equals(PlayersIDs[playerNum])) {
                deleteLastBotMessage();
                tictactoeChannel.sendMessage("**Timeout!**").complete();
                continue;
            }
            if (coordinates.length == 2 && checkIfEmpty(coordinates)) {
                deleteLastPlayerMessage(playerNum);
                break;
            }
        }
        return coordinates;
    }

    // Method checks if place is empty on the board
    public static boolean checkIfEmpty(int[] coordinates) {
        if (board[coordinates[0]][coordinates[1]] == " ") {
            return true;
        }
        return false;
    }

    // Method converts message to coordinates
    public static int[] getNumsFromString(String message) {
        String[] coordinatesString = message.split(", ");
        int[] coordinatesInt = new int[coordinatesString.length];

        
        for (int i = 0; i < coordinatesString.length; i++) {
            try {
                coordinatesInt[i] = Integer.parseInt(coordinatesString[i])-1;
                if (Integer.parseInt(coordinatesString[i]) < 1 || Integer.parseInt(coordinatesString[i]) > 3) {
                    return new int[] {};
                }
            } catch (NumberFormatException e) {
                return new int[] {};
            }
        }
        return coordinatesInt;
    }
    
    // Method gets ID of a player
    public static void getPlayer(int playerNum) {
        while (PlayersIDs[playerNum] == null) {
            tictactoeChannel.sendMessage("Who is playing with " + Players[playerNum] + "? Type anything.").queue();
            Message message = getResponse();
            PlayersIDs[playerNum] = message.getAuthor().getId();
            if (PlayersIDs[playerNum].equals(BOT_ID)) {
                PlayersIDs[playerNum] = null;
            }
            if (PlayersIDs[playerNum] == null) {
                tictactoeChannel.sendMessage("**Timeout**").queue();
            }
        }
    }

    // Method resets the board
    public static void resetTheBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = " ";
            }
        }
    }

    // Method gets number 1/2 from player
    public static int getNumberFromPlayer() {
        int numberInput;
        while(true) {
            String[] message = getResponse().getContentRaw().split("\\s+");
            if (message.length == 1 && isValidNumber(message[0])) {
                numberInput = Integer.parseInt(message[0]);
                break;
            }
            deleteLastBotMessage();
            tictactoeChannel.sendMessage("Wrong number or timeout. Send the number again [1/2].").queue();
        }
        return numberInput;
    }

    // Method gets response from a player
    public static Message getResponse() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tictactoeChannel.getHistory().retrievePast(1).complete().get(0);
    }

    // Method deletes last bot message
    public static void deleteLastBotMessage() {
        Message lastBotMessage = null;
        for (Message message : tictactoeChannel.getIterableHistory().cache(false)) {
            if (message.getAuthor().getId().equals(BOT_ID)) {
                lastBotMessage = message;
                break;
            }
        }
        if (lastBotMessage != null) {
            lastBotMessage.delete().complete();
        }
        return;
    }

    // Method deletes last player message
    public static void deleteLastPlayerMessage(int player) {
        Message lastPlayerMessage = null;
        for (Message message : tictactoeChannel.getIterableHistory().cache(false)) {
            if (message.getAuthor().getId().equals(PlayersIDs[player])) {
                lastPlayerMessage = message;
                break;
            }
        }
        if (lastPlayerMessage != null) {
            lastPlayerMessage.delete().complete();
        }
        return;
    }

    // Method checks if number is valid
    public static boolean isValidNumber(String number) {
        if ((number.chars().allMatch(Character::isDigit))) {
            if (Integer.parseInt(number) < 1) {
                return false;
            }
            return true;
        }
        return false;
    }

    // Method prints a board
    public static void printTheBoard(boolean first) {
        String boardString = "```\n";
        for(int i = 0; i < board.length; i++) {
            boardString += printInbetweenLine();
            for (int k = 0; k < 6; k++) {
                for(int j = 0; j < board[i].length; j++) {
                    boardString += "|";
                    boardString += printLineOX(k, board[i][j]);
                }
                boardString += "|\n";
            }
        }
        boardString += printInbetweenLine();
        boardString += "```";
        if (first) {
            CompletableFuture<String> future = new CompletableFuture<>();
            tictactoeChannel.sendMessage(boardString).queue(message -> {
                future.complete(message.getId());
            });
            try {
                sentMessageId = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } else {
            tictactoeChannel.editMessageById(sentMessageId, boardString).queue();
        }
    }

    // Method returns line of O and X
    public static String printLineOX(int line, String sign) {
        if (sign == "O") {
            switch (line) {
                case 0:
                case 5:
                    return "      *  *      ";
                case 1:
                case 4:
                    return "   *        *   ";
                case 2:
                case 3:
                    return "  *          *  ";
            }
        } else if (sign == "X") {
            switch (line) {
                case 0:
                case 5:
                    return "   *        *   ";
                case 1:
                case 4:
                    return "     *    *     ";
                case 2:
                case 3:
                    return "       **       ";
            }
        }
        switch (line) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return "                ";
        }
        return "";
    }

    /*

        *  *      
     *        *   
    *          *  
    *          *  
     *        *   
        *  *      

     *        *   
       *    *     
         **       
         **       
       *    *     
     *        *   
    
     */

    // Method returns line between Xs and Os
    public static String printInbetweenLine() {
        return "+----------------+----------------+----------------+\n";
    }

    // Method checks if game is over
    public static String checkIfGameOver() {
        if (checkIfSame(board[0][0], board[1][1], board[2][2])) {
            return board[1][1];
        }
        if (checkIfSame(board[0][2], board[1][1], board[2][0])) {
            return board[1][1];
        }
        for (int i = 0; i < board.length; i++) {
            if (checkIfSame(board[i][0], board[i][1], board[i][2])) {
                return board[i][1];
            }
            if (checkIfSame(board[0][i], board[1][i], board[2][i])) {
                return board[1][i];
            }
        }
        boolean tie = true;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == " ") {
                    tie = false;
                }
            }
        }
        if (tie) {
            return "Tie";
        }
        return null;
    }

    // Method checks if 3 strings are same as " "
    public static boolean checkIfSame(String a, String b, String c) {
        return (a == b && b == c && a != " ");
    }
}