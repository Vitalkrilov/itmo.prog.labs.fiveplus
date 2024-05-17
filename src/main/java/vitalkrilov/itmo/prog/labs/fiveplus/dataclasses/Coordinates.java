package vitalkrilov.itmo.prog.labs.fiveplus.dataclasses;

import org.jetbrains.annotations.NotNull;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.*;

import java.io.Reader;
import java.util.Objects;

public class Coordinates implements ReaderFillable, Cloneable, Comparable<Coordinates> {
    private int x;
    private @NotNull Float y; //Поле не может быть null

    public Coordinates() {
        this.x = 0;
        this.y = 0f;
    }

    public int getX() {
        return x;
    }

    public Result<Object, String> setX(int x) {
        this.x = x;
        return new Result.Ok<>();
    }

    public Float getY() {
        return y;
    }

    public Result<Object, String> setY(Float y) {
        if (y == null)
            return new Result.Err<>("Value `y` can't be null");
        this.y = y;
        return new Result.Ok<>();
    }

    @Override
    public Result<Object, String> fillFromReader(boolean isInteractive, Reader reader, int spacingLevel) {
        NullableLineInputProcessor nlip = new NullableLineInputProcessor(reader);

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, "Coordinates's x (int)", false, ValueParser::parseInteger).andThen(this::setX);
            if (res.isErr()) return res;
        }

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, "Coordinates's y (Float, not null)", false, ValueParser::parseFloat).andThen(this::setY);
            if (res.isErr()) return res;
        }

        return new Result.Ok<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Coordinates o2 = (Coordinates) o;
        return this.x == o2.x && this.y.equals(o2.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Integer.valueOf(this.x).hashCode(), this.y);
    }

    @Override
    public String toString() {
        return String.format("Coordinates { x = %d, y = %f }", this.x, this.y);
    }

    @Override
    public Coordinates clone() {
        try {
            return (Coordinates) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int compareTo(@NotNull Coordinates o2) {
        int compareResult;

        compareResult = Integer.compare(this.x, o2.x);
        if (compareResult != 0)
            return compareResult;

        compareResult = this.y.compareTo(o2.y);

        return compareResult;
    }
}
