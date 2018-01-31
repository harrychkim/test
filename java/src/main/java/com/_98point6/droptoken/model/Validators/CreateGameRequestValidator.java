package com._98point6.droptoken.model.Validators;

import com._98point6.droptoken.DropTokenConfiguration;
import com._98point6.droptoken.exceptions.CreateGameException;
import com._98point6.droptoken.model.CreateGameRequest;

public class CreateGameRequestValidator implements Validator<CreateGameRequest> {
    private final int winningLength;
    private final int playersAllowed;
    public CreateGameRequestValidator(DropTokenConfiguration configuration) {
        this.winningLength = configuration.getWinningLength();
        this.playersAllowed = configuration.getAllowedPlayers();
    }

    public CreateGameRequest validate(CreateGameRequest request) {
        if (request.getPlayers().size() != playersAllowed) {
            throw new CreateGameException();
        }

        if (request.getRows() != 4 || request.getColumns() != 4) {
            throw new CreateGameException();
        }

        return request;

//        if (request.getRows() < winningLength || request.getColumns() < winningLength) {
//            throw new CreateGameException();
//        }
    }
}
