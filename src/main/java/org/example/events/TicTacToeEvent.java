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
    
    //private static String[][] board = new String[3][3];
    private static String[][] board = {{"O", "X", "O"}, 
                                       {"X", "O", "X"},
                                       {"X", "O", "X"}
                                      };
    private static String BOT_ID;
    private static String GUILD_ID;
    private static String TICTACTOE_CHANNERL_ID;
    private static String PREFIX;
    private static Guild guild;
    private static TextChannel tictactoeChannel;
    private static String[] Players = {"X", "O"};
    
    private static String[] PlayersIDs = new String[2];
    private static String ai;
    private static String human;
    private static boolean isGameInProgress = false;
    private static String sentMessageId;

    // Constructor that initializes the value of API_KEY
    public TicTacToeEvent(String GUILD_ID, String TICTACTOE_CHANNERL_ID, String BOT_ID) {
        TicTacToeEvent.GUILD_ID = GUILD_ID;
        TicTacToeEvent.TICTACTOE_CHANNERL_ID = TICTACTOE_CHANNERL_ID;
        TicTacToeEvent.BOT_ID = BOT_ID;
    }

    // Define a static method for updating the command prefix
    public static void updatePrefix(String newPrefix) {
        PREFIX = newPrefix;
    }

    @Override
    public void onReady(ReadyEvent event) {
        guild = event.getJDA().getGuildById(GUILD_ID);
        tictactoeChannel = guild.getTextChannelById(TICTACTOE_CHANNERL_ID);
    }

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

    public static void handleTTTCommand(String[] message) {
        if (message.length != 2){
            tictactoeChannel.sendMessage("**Wrong command**").queue();
            return;
        }
        switch (message[1].toLowerCase()) {
            case "help":
                String helpMessage = String.format("Here are the available commands for %sTTT:\n\n" +
                "%sTTT newGame - Starts a new Tictactoe game.\n" + 
                "%sTTT stop - *feture to be added*\n\n" + 
                "Good luck and have fun!", PREFIX, PREFIX,PREFIX);
                tictactoeChannel.sendMessage(helpMessage).queue();
                return;
            /*case "stop":
                tictactoeChannel.sendMessage("**Tictactoe game Stoped**").queue();

                return;*/
            case "newgame":
                if (isGameInProgress) {
                    tictactoeChannel.sendMessage("**Game is in progress**").queue();
                    return;
                }
                startNewGame();
                return;
            default:
                tictactoeChannel.sendMessage("**Wrong command**").queue();
                return;
        }
        
    }

    public static void startNewGame() {
        tictactoeChannel.sendMessage("**You will have 5 seconds to input answers (or I will ask you again).**").queue();
        resetTheBoard();
        tictactoeChannel.sendMessage("How many players [1/2]?").queue();
        int currentNumberOfPlayers;
        while (true) {
            currentNumberOfPlayers = getNumberFromPlayer();
            if (currentNumberOfPlayers == 1 || currentNumberOfPlayers == 2) {
                break;
            }
        }
        if (currentNumberOfPlayers == 1) {
            handleGameForOne();
        } else {
            handleGameForTwo();
        }
    }

    public static void handleGameForOne() { 
        tictactoeChannel.sendMessage("Which player do you want to play as?").queue();
        int whichPlayer;
        while (true) {
            whichPlayer = getNumberFromPlayer();
            if (whichPlayer == 1 || whichPlayer == 2) {
                break;
            }
        }
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

    public static void doMove(int player) {
        if (PlayersIDs[player] == null) {
            ai = Players[player];
            botMove(player);
            return;
        }
        int[] input = getInputFromPlayer(player);
        board[input[0]][input[1]] = Players[player];
    }

    public static void handleGameForTwo() { 
        getPlayer(0);
        getPlayer(1);
        printTheBoard(true);
        for (int i = 0; i < 9; i++) {
            if (i%2 == 0) {
                doMove(i%2);
                printTheBoard(false);
                if (!(checkIfGameOver() == null || checkIfGameOver() == "Tie")) {
                    tictactoeChannel.sendMessage("**GAME END, player 1 won**").queue();
                    handleGameEnd();
                    return;
                }
            } else {
                doMove(i%2);
                printTheBoard(false);
                if (!(checkIfGameOver() == null || checkIfGameOver() == "Tie")) {
                    tictactoeChannel.sendMessage("**GAME END, player 2 won**").queue();
                    handleGameEnd();
                    return;
                }
            }
        }
        tictactoeChannel.sendMessage("**GAME END, it's a tie**").queue();
        handleGameEnd();
        return;
    }

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

    }

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

    public static int[] getInputFromPlayer(int playerNum) {
        Message message;
        int[] coordinates = null;
        while (true) {
            if (coordinates == null) {
                tictactoeChannel.sendMessage("Player " + (playerNum + 1)+ " input your choice like \"x, y\": ").queue();
            } else {
                tictactoeChannel.sendMessage("Invalid input, player " + (playerNum + 1) + " input your choice like \"x, y\": ").queue();
            }
            message = getResponse();
            String Author = message.getAuthor().getId();
            if (!Author.equals(PlayersIDs[playerNum])) {
                tictactoeChannel.sendMessage("**Timeout**").queue();
                continue;
            }
            coordinates = getNumsFromString(message.getContentRaw());
            if (coordinates.length == 2 && checkIfEmpty(coordinates)) {
                break;
            }
        }
        return coordinates;
    }

    public static boolean checkIfEmpty(int[] coordinates) {
        if (board[coordinates[0]][coordinates[1]] == " ") {
            return true;
        }
        return false;
    }

    public static int[] getNumsFromString(String message) {
        String[] coordinatsString = message.split(", ");
        int[] coordinatsInt = new int[coordinatsString.length];

        
        for (int i = 0; i < coordinatsString.length; i++) {
            try {
                coordinatsInt[i] = Integer.parseInt(coordinatsString[i])-1;
                if (Integer.parseInt(coordinatsString[i]) < 1 || Integer.parseInt(coordinatsString[i]) > 3) {
                    return new int[] {};
                }
            } catch (NumberFormatException e) {
                System.out.println("Error in input");
                return new int[] {};
            }
        }
        return coordinatsInt;
    }

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

    public static void resetTheBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = " ";
            }
        }
    }

    public static int getNumberFromPlayer() {
        int numberInput;
        while(true) {
            String[] message = getResponse().getContentRaw().split("\\s+");  
            if (message.length == 1 && isValidNumber(message[0])) {
                numberInput = Integer.parseInt(message[0]);
                break;
            }
            tictactoeChannel.sendMessage("**Wrong number or timeout. Send the number again**").queue();
        }
        return numberInput;
    }

    public static Message getResponse() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tictactoeChannel.getHistory().retrievePast(1).complete().get(0);
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

    public static String printLineOX(int line, String sign) {
        if (sign == "O") {
            switch (line) {
                case 0:
                    return "      *  *      ";
                case 1:
                    return "   *        *   ";
                case 2:
                    return "  *          *  ";
                case 3:
                    return "  *          *  ";
                case 4:
                    return "   *        *   ";
                case 5:
                    return "      *  *      ";
            }
        } else if (sign == "X") {
            switch (line) {
                case 0:
                    return "   *        *   ";
                case 1:
                    return "     *    *     ";
                case 2:
                    return "       **       ";
                case 3:
                    return "       **       ";
                case 4:
                    return "     *    *     ";
                case 5:
                    return "   *        *   ";
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

        /*tictactoeChannel.sendMessage("```\n" +
                                "      *  *      \n" +
                                "   *        *   \n" +
                                "  *          *  \n" +
                                "  *          *  \n" +
                                "   *        *   \n" +
                                "      *  *      \n" +
                                "```").queue();
        tictactoeChannel.sendMessage("```\n" +
                                "   *        *   \n" +
                                "     *    *     \n" +
                                "       **       \n" +
                                "       **       \n" +
                                "     *    *     \n" +
                                "   *        *   \n" +
                                "```").queue();*/
    }

    public static String printInbetweenLine() {
        return "+----------------+----------------+----------------+\n";
    }

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

    public static boolean checkIfSame(String a, String b, String c) {
        return (a == b && b == c && a != " ");
    }
}
