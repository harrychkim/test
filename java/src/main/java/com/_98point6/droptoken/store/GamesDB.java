package com._98point6.droptoken.store;

import com._98point6.droptoken.DropTokenConfiguration;
import com._98point6.droptoken.app.Game;
import com._98point6.droptoken.model.CreateGameRequest;

import java.util.*;
import java.util.stream.Collectors;

/*
This would be stored in an external database, but for the exercise, I'll keep it in memory.
An external database allows for multiple instances of the application to run.
 */
public class GamesDB {
    private Map<String, Game> games = new HashMap<>();

    public Game getGame(String gameId) {
        return games.get(gameId);
    }

    public List<Game> getActiveGames() {
        return games.values().stream()
                .filter(game -> !game.isDone())
                .collect(Collectors.toList());
    }

    public Game createGame(CreateGameRequest request, DropTokenConfiguration configuration) {
        Game newGame = new Game(request, configuration);
        games.put(newGame.getId(), newGame);
        return newGame;
    }
}
