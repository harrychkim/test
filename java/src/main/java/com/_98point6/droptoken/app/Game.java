package com._98point6.droptoken.app;

import com._98point6.droptoken.DropTokenConfiguration;
import com._98point6.droptoken.exceptions.GameAccessException;
import com._98point6.droptoken.exceptions.GameStatusException;
import com._98point6.droptoken.exceptions.IllegalMoveException;
import com._98point6.droptoken.exceptions.OutOfTurnException;
import com._98point6.droptoken.model.CreateGameRequest;
import com._98point6.droptoken.model.GetMoveResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private static final Logger logger = LoggerFactory.getLogger(Game.class);
    private ArrayList<GetMoveResponse> moves = new ArrayList<>();
    private final int winningLength;
    private String id;
    private List<String> players;
    private List<String> playerOrder;
    private int orderPointer;
    private String[][] board;
    private int boardMoves;
    private String winner;

    public Game(CreateGameRequest request, DropTokenConfiguration configuration) {
        this.winningLength = configuration.getWinningLength();
        players = request.getPlayers();
        playerOrder = new ArrayList<>(request.getPlayers());
        orderPointer = 0;
        this.board = new String[request.getColumns()][request.getRows()];
        boardMoves = 0;
        this.id = UUID.randomUUID().toString();
    }

    public int addMove(GetMoveResponse nextMove) {
        String player = nextMove.getPlayer();
        String type = nextMove.getType();
        int column = nextMove.getColumn().isPresent()
                ? nextMove.getColumn().get()
                : -1;

        if (isDone()) {
            throw new GameStatusException();
        }
        if (!players.contains(player)) {
            throw new GameAccessException();
        }

        if ("QUIT".equals(type)) {
            playerOrder.remove(player);
            orderPointer = orderPointer % playerOrder.size();
            moves.add(nextMove);
            return -1;
        }

        String nextPlayer = playerOrder.get(orderPointer);
        if (!player.equals(nextPlayer)) {
            throw new OutOfTurnException();
        }
        int row = lowestAvailableRow(column);
        board[column][row] = player;
        checkWinner(column, row);
        orderPointer++;
        orderPointer = orderPointer % playerOrder.size();
        moves.add(nextMove);
        return boardMoves++;
    }

    public List<String> getPlayers() {
        return players;
    }

    public ArrayList<GetMoveResponse> getMoves() {
        return moves;
    }

    public boolean isDone() {
        return winner != null || playerOrder.size() == 1 || boardMoves == getBoardSize();
    }

    public String getWinner() {
        if (playerOrder.size() == 1) {
            return playerOrder.get(0);
        }
        return winner;
    }

    public String getId() {
        return this.id;
    }

    private int getBoardSize() {
        return board.length * board[0].length;
    }

    private int lowestAvailableRow(int column) {
        try {
            String[] row = board[column];
            for (int i = 0; i < row.length; i++) {
                if (row[i] == null) {
                    return i;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalMoveException("column value is invalid");
        }
        throw new IllegalMoveException("column has no available rows");
    }

    private void checkWinner(int column, int row) {
        String player = this.board[column][row];
        if (checkNS(column, row) || checkEW(column, row) || checkPosDiag(column, row) || checkNegDiag(column, row)) {
            winner = player;
        }
    }

    private boolean checkNS(int column, int row) {
        return 1 + checkDirection(column, row, 0, 1) + checkDirection(column, row, 0, -1) >= winningLength;
    }

    private boolean checkEW(int column, int row) {
        return 1 + checkDirection(column, row, 1, 0) + checkDirection(column, row, -1, 0) >= winningLength;
    }

    private boolean checkPosDiag(int column, int row) {
        return 1 + checkDirection(column, row, 1, 1) + checkDirection(column, row, -1, -1) >= winningLength;
    }

    private boolean checkNegDiag(int column, int row) {
        return 1+ checkDirection(column, row, 1, -1) + checkDirection(column, row, -1, 1) >= winningLength;
    }

    // TODO make column and row direction an Enum with values -1, 0, and 1
    private int checkDirection(int columnStart, int rowStart, int columnDirection, int rowDirection) {
        String player = this.board[columnStart][rowStart];
        int nextColumn = columnStart;
        int nextRow = rowStart;

        int depth = 0;
        while (depth < winningLength - 1) {
            nextColumn += columnDirection;
            nextRow += rowDirection;
            try {
                if (player.equals(this.board[nextColumn][nextRow])) {
                    depth++;
                    continue;
                }
                break;
            } catch (ArrayIndexOutOfBoundsException e) {
                break;
            }
        }
        return depth;
    }
}
