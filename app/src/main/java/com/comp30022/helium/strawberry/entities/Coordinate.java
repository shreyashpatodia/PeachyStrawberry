package com.comp30022.helium.strawberry.entities;


/**
 * TODO
 * Replace all usage of this with Location or LatLng
 * Then, use only Location or LatLng
 */
@Deprecated
public class Coordinate {
    private final double x;
    private final double y;

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
