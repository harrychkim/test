package com._98point6.droptoken;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 *
 */
public class DropTokenConfiguration extends Configuration {
    @NotEmpty
    private String template;

    @NotNull
    private int winningLength;

    @NotNull
    private int allowedPlayers;

    @NotEmpty
    private String defaultName = "Stranger";

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public int getWinningLength() {
        return winningLength;
    }

    @JsonProperty
    public void setWinningLength(int winningLength) {
        this.winningLength = winningLength;
    }

    @JsonProperty
    public int getAllowedPlayers() {
        return allowedPlayers;
    }

    @JsonProperty
    public void setAllowedPlayers(int allowedPlayers) {
        this.allowedPlayers = allowedPlayers;
    }
}
