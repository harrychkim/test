package com._98point6.droptoken;

import com._98point6.droptoken.app.Game;
import com._98point6.droptoken.exceptions.*;
import com._98point6.droptoken.model.*;
import com._98point6.droptoken.model.Validators.CreateGameRequestValidator;
import com._98point6.droptoken.model.Validators.Validator;
import com._98point6.droptoken.store.GamesDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */

// TODO: check that wrong variables e.g. a integer or array for a string returns a 400
// TODO: add response entities that describe the error to user
@Path("/drop_token")
@Produces(MediaType.APPLICATION_JSON)
public class DropTokenResource {
    private static final String DONE = "DONE";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String QUIT = "QUIT";
    private static final String MOVE = "MOVE";
    private static final Logger logger = LoggerFactory.getLogger(DropTokenResource.class);
    private final DropTokenConfiguration configuration;
    private final Validator<CreateGameRequest> createGameRequestValidator;
    private GamesDB database;

    public DropTokenResource(DropTokenConfiguration configuration) {
        this.database = new GamesDB();
        this.configuration = configuration;
        this.createGameRequestValidator = new CreateGameRequestValidator(configuration);
    }

    @GET
    public Response getGames() {
        List<Game> games = this.database.getActiveGames();
        GetGamesResponse response = new GetGamesResponse.Builder()
                .games(games.stream().map(Game::getId).collect(Collectors.toList()))
                .build();
        return Response.ok(response).build();
    }

    @POST
    public Response createNewGame(CreateGameRequest request) {
        logger.info("request={}", request);
        try {
            request = createGameRequestValidator.validate(request);
        } catch (InvalidRequestException e) {
            // TODO: add response entities that describe the error to user
            return Response.status(400).build();
        }

        Game newGame = this.database.createGame(request, configuration);
        CreateGameResponse response = new CreateGameResponse.Builder()
                .gameId(newGame.getId())
                .build();
        return Response.ok(response).build();
    }

    @Path("/{id}")
    @GET
    public Response getGameStatus(@PathParam("id") String gameId) {
        logger.info("gameId = {}", gameId);
        Game game = this.database.getGame(gameId);
        if (game == null) {
            return Response.status(404).build();
        }
        GameStatusResponse response = new GameStatusResponse.Builder()
                .players(game.getPlayers())
                .state(game.isDone() ? DONE : IN_PROGRESS)
                .build();
        // TODO - check if null winner is printed out to the user
        return Response.ok(response).build();
    }

    @Path("/{id}/{playerId}")
    @POST
    public Response postMove(@PathParam("id")String gameId, @PathParam("playerId") String playerId, PostMoveRequest request) {
        logger.info("gameId={}, playerId={}, move={}", gameId, playerId, request);
        Game game = this.database.getGame(gameId);
        if (game == null || !game.getPlayers().contains(playerId)) {
            return Response.status(404).build();
        }

        GetMoveResponse move = new GetMoveResponse.Builder()
                .player(playerId)
                .type(MOVE)
                .column(request.getColumn())
                .build();
        try {
            int moveId = game.addMove(move);
            PostMoveResponse response = new PostMoveResponse.Builder()
                    .move(String.format("%s/moves/%s", gameId, moveId))
                    .build();

            return Response.ok(response).build();
        } catch (IllegalMoveException e) {
            return Response.status(400).build();
        } catch (OutOfTurnException e) {
            return Response.status(409).build();
        } catch (InvalidMoveException e) {
            return Response.status(404).build();
        }
    }

    @Path("/{id}/{playerId}")
    @DELETE
    public Response playerQuit(@PathParam("id")String gameId, @PathParam("playerId") String playerId) {
        logger.info("gameId={}, playerId={}", gameId, playerId);
        Game game = this.database.getGame(gameId);
        if (game == null || !game.getPlayers().contains(playerId)) {
            return Response.status(404).build();
        }

        GetMoveResponse quitMove = new GetMoveResponse.Builder()
                .type(QUIT)
                .player(playerId)
                .build();
        try {
            game.addMove(quitMove);
            return Response.status(202).build();
        } catch (GameStatusException e) {
            return Response.status(410).build();
        } catch (GameAccessException e) {
            return Response.status(404).build();
        }
    }

    @Path("/{id}/moves")
    @GET
    public Response getMoves(@PathParam("id") String gameId, @QueryParam("start") Integer start, @QueryParam("until") Integer until) {
        logger.info("gameId={}, start={}, until={}", gameId, start, until);
        Game game = this.database.getGame(gameId);
        if (game == null) {
            return Response.status(404).build();
        }
        List<GetMoveResponse> moves = game.getMoves();
        // TODO: ask if movesSubset being null should return 404 (if start > 0)
        GetMovesResponse response = new GetMovesResponse.Builder()
                .moves(moves.subList(start, until))
                .build();

        return Response.ok(response).build();
    }

    @Path("/{id}/moves/{moveId}")
    @GET
    public Response getMove(@PathParam("id") String gameId, @PathParam("moveId") Integer moveId) {
        logger.info("gameId={}, moveId={}", gameId, moveId);
        if (moveId < 0) {
            return Response.status(400).build();
        }
        Game game = this.database.getGame(gameId);
        if (game == null) {
            return Response.status(404).build();
        }
        try {
            GetMoveResponse response = game.getMoves().get(moveId);
            return Response.ok(response).build();
        } catch (IndexOutOfBoundsException e) {
            return Response.status(404).build();
        }
    }


    public static void main(String[] args) {
        String foo = String.format("%s/moves/%s", "foo", 5);
        System.out.println(foo);
    }
}
