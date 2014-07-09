package com.molina.tsp;

public class Ciudad {

    private int numero;
    private double x;
    private double y;

    public Ciudad(int numero, double x, double y) {
        super();
        this.numero = numero;
        this.x = x;
        this.y = y;
    }

    public int getNumero() {
        return numero;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
