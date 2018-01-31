package com._98point6.droptoken.model;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *
 */
public class PostMoveResponse {
    private String move;

    public PostMoveResponse() {}

    private PostMoveResponse(Builder builder) {
        this.move = Preconditions.checkNotNull(builder.move);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("move", move)
                .toString();
    }

    public String getMove() {
        return move;
    }

    public static class Builder {
        private String move;

        public Builder move(String move) {
            this.move = move;
            return this;
        }

        public Builder fromPrototype(PostMoveResponse prototype) {
            move = prototype.move;
            return this;
        }

        public PostMoveResponse build() {
            return new PostMoveResponse(this);
        }
    }
}
