package com._98point6.droptoken.app;

import com._98point6.droptoken.DropTokenConfiguration;
import com._98point6.droptoken.model.CreateGameRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class GameTests {
    Game game;
    @Before
    public void setUp() {
        CreateGameRequest request = new CreateGameRequest.Builder()
                .columns(4)
                .rows(4)
                .players(Arrays.asList("player1", "player2"))
                .build();
    }
    @Test
    public void testAddMoveToFullBoard() {

    }

    @Test
    public void testPlayerQuit() {

    }

    @Test
    public void testPlayerIsNotInGame() {

    }

    @Test
    public void testGameIsDone() {

    }
}
