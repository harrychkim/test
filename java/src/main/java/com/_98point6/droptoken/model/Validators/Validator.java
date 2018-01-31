package com._98point6.droptoken.model.Validators;

public interface Validator<E> {
    E validate(E obj);
}
