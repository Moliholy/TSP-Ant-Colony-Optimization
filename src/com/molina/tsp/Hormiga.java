package com.molina.tsp;

import java.util.Random;

public class Hormiga extends Thread {

    protected double[][] matrizFeromona;
    protected Integer[] solucion;
    protected boolean[] visitadas;
    protected static int numHormigasActual;
    protected AntColonyTSP padre;
    protected static Random random;

    public Hormiga(double[][] matrizF, Integer[] sol, AntColonyTSP _padre,
            long semilla) {
        super();
        matrizFeromona = matrizF;
        solucion = sol;
        aumentarHormigasActivas();
        padre = _padre;
        //random = new Random(semilla);
        random = new Random();
        visitadas = new boolean[padre.numCiudades()];
    }

    public static int getNumHormigasActual() {
        return numHormigasActual;
    }

    public static void setNumHormigasActual(int numHormigasActual) {
        Hormiga.numHormigasActual = numHormigasActual;
    }

    protected void disminuirHormigasActivas() {
        numHormigasActual--;
    }

    protected static void aumentarHormigasActivas() {
        numHormigasActual++;
    }

    protected double calcularSumaProbabilidades(int actual) {
        double suma = 0d;
        for (int i = 0; i < padre.numCiudades(); i++)
            if (!visitadas[i])
                suma += Math.pow(matrizFeromona[actual - 1][i],
                        AntColonyTSP.getAlfa())
                        * Math.pow((1d / padre.distancias[actual - 1][i]),
                        AntColonyTSP.getBeta());
        return suma;
    }

    @Override
    public void run() {
        int n = padre.numCiudades();
        solucion[0] = 1 + random.nextInt(n);
        int actual = solucion[0];
        visitadas[actual - 1] = true;
        for (int i = 1; i < n; i++) {
            double sumaProbabilidades = calcularSumaProbabilidades(actual);
            double tirada = random.nextDouble() * sumaProbabilidades;
            double contador = 0d;
            boolean parar = false;
            for (int j = 0; j < n && !parar; j++)
                if (!visitadas[j]) {
                    contador += Math.pow(matrizFeromona[actual - 1][j],
                            AntColonyTSP.getAlfa())
                            * Math.pow(1d / padre.distancias[actual - 1][j],
                            AntColonyTSP.getBeta());
                    if (contador >= tirada) {
                        parar = true;
                        actual = j + 1;
                        solucion[i] = actual;
                        visitadas[j] = true;
                    }
                }
        }
        // solucion=padre.blAscensionColinas(solucion);
        synchronized (padre) {
            disminuirHormigasActivas();
            if (numHormigasActual <= 0)
                padre.notifyAll();
        }
    }
}
