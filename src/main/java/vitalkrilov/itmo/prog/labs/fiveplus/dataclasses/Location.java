package vitalkrilov.itmo.prog.labs.fiveplus.dataclasses;

import org.jetbrains.annotations.NotNull;
import vitalkrilov.itmo.prog.labs.fiveplus.utilities.*;

import java.awt.geom.Arc2D;
import java.io.Reader;
import java.util.Objects;

public class Location implements ReaderFillable, Cloneable, Comparable<Location> {
    private @NotNull Integer x; //Поле не может быть null
    private @NotNull Float y; //Поле не может быть null
    private double z;
    private @NotNull String name; //Поле не может быть null

    public Location() {
        this.x = 0;
        this.y = 0f;
        this.z = 0;
        this.name = "";
    }

    public Integer getX() {
        return x;
    }

    public Result<Object, String> setX(Integer x) {
        if (x == null)
            return new Result.Err<>("Value `x` can't be null");
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

    public double getZ() {
        return z;
    }

    public Result<Object, String> setZ(double z) {
        this.z = z;
        return new Result.Ok<>();
    }

    public String getName() {
        return name;
    }

    public Result<Object, String> setName(String name) {
        if (name == null)
            return new Result.Err<>("Name can't be null");
        this.name = name;
        return new Result.Ok<>();
    }

    @Override
    public Result<Object, String> fillFromReader(boolean isInteractive, Reader reader, int spacingLevel) {
        NullableLineInputProcessor nlip = new NullableLineInputProcessor(reader);

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, "Location's x (Integer, not null)", false, ValueParser::parseInteger).andThen(this::setX);
            if (res.isErr()) return res;
        }

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, "Location's y (Float, not null)", false, ValueParser::parseFloat).andThen(this::setY);
            if (res.isErr()) return res;
        }

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, "Location's z (double)", false, ValueParser::parseDouble).andThen(this::setZ);
            if (res.isErr()) return res;
        }

        {
            var res = nlip.collectInteractively(isInteractive, spacingLevel, "Location's name (String, not null)", false, s -> new Result.Ok<>(s)).andThen(this::setName);
            if (res.isErr()) return res;
        }

        return new Result.Ok<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Location o2 = (Location) o;
        return this.x.equals(o2.x) && this.y.equals(o2.y) && this.z == o2.z && this.name.equals(o2.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.x.hashCode(), this.y.hashCode(), Double.valueOf(this.z).hashCode(), this.name.hashCode());
    }

    @Override
    public String toString() {
        return String.format("Location { x = %d, y = %f, z = %f, name = \"%s\" }", this.x, this.y, this.z, this.name);
    }

    @Override
    public Location clone() {
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int compareTo(@NotNull Location o2) {
        int compareResult;

        compareResult = this.x.compareTo(o2.x);
        if (compareResult != 0)
            return compareResult;

        compareResult = this.y.compareTo(o2.y);
        if (compareResult != 0)
            return compareResult;

        compareResult = Double.compare(this.z, o2.z);
        if (compareResult != 0)
            return compareResult;

        compareResult = this.name.compareTo(o2.name);

        return compareResult;
    }
}