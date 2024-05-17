package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Rust-like Result thing. <i>Let's make programming in Java at least a bit better than ...</i>
 * @param <O> Contained value's type if it's Ok
 * @param <E> Contained value's type if it's Err
 */
public abstract class Result<O, E> implements Serializable {
    /**
     * Result::Ok
     * @param <O> Contained value's type if it's Ok
     * @param <E> Contained value's type if it's Err
     */
    public static class Ok<O, E> extends Result<O, E> {
        /**
         * Result::Ok(value).
         * @param value Value to contain.
         */
        public Ok(O value) {
            this();
            this.ok = value;
        }

        /**
         * Result::Ok().
         */
        public Ok() {
            this.errorAssigned = false;
            this.ok = null;
            this.err = null;
        }
    }

    /**
     * Result::Err
     * @param <O> Contained value's type if it's Ok
     * @param <E> Contained value's type if it's Err
     */
    public static class Err<O, E> extends Result<O, E> {
        /**
         * Result::Err(value).
         * @param value Value to contain.
         */
        public Err(E value) {
            this();
            this.err = value;
        }

        /**
         * Result::Err().
         */
        public Err() {
            this.errorAssigned = true;
            this.ok = null;
            this.err = null;
        }
    }

    boolean errorAssigned;
    O ok;
    E err;

    /**
     * Checks if it's Result::Ok.
     * @return result
     */
    public boolean isOk() {
        return !this.errorAssigned;
    }

    /**
     * Checks if it's Result::Err.
     * @return result
     */
    public boolean isErr() {
        return this.errorAssigned;
    }

    /**
     * Gets Result::Ok's value.
     * @return null means value was not provided or was null
     */
    public O getOk() {
        if (this.errorAssigned)
            throw new RuntimeException("Tried to take Ok(...) value from Result::Err.");
        return this.ok;
    }

    /**
     * Gets Result::Err's value.
     * @return null means value was not provided or was null
     */
    public E getErr() {
        if (!this.errorAssigned)
            throw new RuntimeException("Tried to take Err(...) value from Result::Err.");
        return this.err;
    }

    /**
     * Performs applying provided function if it's Result::Ok
     * @param after Provided function to be applied (must return Result).
     * @return Result of applying or initial error.
     * @param <V> Value's type after applying.
     */
    public <V> Result<V, E> andThen(Function<O, Result<V, E>> after) {
        if (this.isErr())
            return new Result.Err<>(this.getErr());
        return after.apply(this.getOk());
    }

    /**
     * Performs applying provided function if it's Result::Ok
     * @param after Provided function to be applied.
     * @return Result of applying or initial error.
     * @param <V> Value's type after applying.
     */
    public <V> Result<V, E> andThenF(Function<O, V> after) {
        if (this.isErr())
            return new Result.Err<>(this.getErr());
        return new Result.Ok<>(after.apply(this.getOk()));
    }

    /**
     * Performs applying provided function if it's Result::Ok
     * @param after Provided function to be applied.
     * @return Result of applying or initial error.
     * @param <V> Value's type after applying.
     */
    public <V> Result<V, E> onErrorF(Function<E, V> after) {
        if (this.isOk())
            return new Result.Err<>();
        return new Result.Ok<>(after.apply(this.getErr()));
    }
}
