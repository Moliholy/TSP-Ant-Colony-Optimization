package com.molina.tsp;

public class HormigaOptimizada extends Hormiga {

    public HormigaOptimizada(double[][] matrizF, Integer[] sol,
            AntColonyTSP _padre, long semilla) {
        super(matrizF, sol, _padre, semilla);
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
        // PARTE NUEVA QUE SE LE PONE A LA HORMIGA PARA QUE HAGA MEJORES BÚSQUEDAS
        if (random.nextBoolean())
            solucion = padre.blAscensionColinas(solucion);
        else
            solucion = padre.busquedaTabu(solucion);
        synchronized (padre) {
            disminuirHormigasActivas();
            if (numHormigasActual <= 0)
                padre.notifyAll();
        }
    }
}
