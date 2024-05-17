package vitalkrilov.itmo.prog.labs.fiveplus.utilities;

/**
 * Rust-like Option thing. <i>Let's make programming in Java at least a bit better than ...</i>
 * @param <T> Contained value's type
 */
public class Option<T> {
    private final static Option<Object> NONE = new Option<>(null);

    T value;

    /**
     * Creates Option::Some.
     * @param value Value to put into.
     */
    public Option(T value) {
        this.value = value;
    }

    /**
     * Creates Option::None.
     */
    public Option() {
        this.value = (T)NONE;
    }

    /**
     * Tell's if this object has no value inside.
     * @return result
     */
    public boolean isNone() {
        return this.value == NONE;
    }

    /**
     * Tell's if this object has some value inside.
     * @return result
     */
    public boolean isSome() {
        return this.value != NONE;
    }

    /**
     * Gets contained value.
     * @return contained value
     */
    public T get() {
        if (this.value == NONE)
            throw new RuntimeException("Tried to take value from Option::NONE.");
        return this.value;
    }

    /**
     * Takes contained value so after calling this current Option will become None.
     * @return contained value
     */
    public T take() {
        if (this.value == NONE)
            throw new RuntimeException("Tried to take value from Option::NONE.");
        T value = this.value;
        this.value = (T)NONE;
        return value;
    }

    /**
     * Puts value so after calling this current Option will become Some.
     * @param value value to put
     */
    public void put(T value) {
        if (this.value != NONE)
            throw new RuntimeException("Tried to put value into Option::SOME.");
        this.value = value;
    }

    /**
     * Swaps values between Option and argument.
     * @param value value to swap
     */
    public T swap(T value) {
        if (this.value == NONE)
            throw new RuntimeException("Tried to swap value with Option::NONE.");
        T tempValue = this.value;
        this.value = value;
        return tempValue;
    }
}
