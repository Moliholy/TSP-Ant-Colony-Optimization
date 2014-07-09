package com.molina.tsp;

import com.molina.view.TSPView;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AntColonyTSP extends TSP {

    protected final int numHormigas;
    protected final TSPView vista;
    protected static final double ALFA = 1d;
    protected static final double BETA = 2d;
    protected static final double RO = 0.1d;
    protected static final Long[] TIEMPOS = {5L * 60 * 1000, 10L * 60 * 1000,
        15L * 60 * 1000};
    protected static final Long[] SEMILLAS_AC = {77355807L, 77355808L,
        77355809L, 77355810L, 77355811L};
    // Ant Colony - Ant System
    public static final String TSP_AC_AS = "antSystem";
    // Ant Colony - Ant System Max Min
    public static final String TSP_AC_ASMM = "maxMinAntSystem";
    private double minX;
    private double maxY;
    private double minY;
    private double maxX;

    public AntColonyTSP(File file, String TAG, int nHormigas, TSPView _vista)
            throws IOException {
        super(file, TAG);
        vista = _vista;
        numHormigas = nHormigas;
        calcularDimensiones();
    }

    private void calcularDimensiones() {
        maxX = ciudades[0].getX();
        maxY = ciudades[0].getY();
        minX = maxX;
        minY = maxY;
        for (int i = 1; i < ciudades.length; i++) {
            if (maxX < ciudades[i].getX())
                maxX = ciudades[i].getX();
            if (maxY < ciudades[i].getY())
                maxY = ciudades[i].getY();
            if (minX > ciudades[i].getX())
                minX = ciudades[i].getX();
            if (minY > ciudades[i].getY())
                minY = ciudades[i].getY();
        }
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public static void ejecucion(int times, String algoritmo, Method method,
            boolean sobreescribir) throws IOException {
        String nombre = algoritmo + ".csv";
        int lineasEscritas = 0;
        try (PrintWriter pw = new PrintWriter(new FileWriter(nombre, sobreescribir))) {
            if (sobreescribir) {
                try (LineNumberReader lnr = new LineNumberReader(new FileReader(nombre))) {
                    lnr.skip(Long.MAX_VALUE);
                    lineasEscritas = lnr.getLineNumber() - 1;
                }
                if (lineasEscritas + 1 > 0)
                    try (RandomAccessFile raf = new RandomAccessFile(nombre, "rw")) {
                        File file = new File(nombre);
                        raf.seek(file.length() - 1);
                        // le escribimos el caracter vacio en sustitucion del '\n'
                        raf.writeChar('\000');
                    }
            }
            if (!sobreescribir || lineasEscritas <= 0) {
                pw.write(algoritmo);
                for (int i = 0; i < FICHEROS.size(); i++)
                    pw.write(";" + FICHEROS.get(i).TAG);
            }
            for (int i = Math.max(lineasEscritas, 0); i < times; i++) {
                pw.write("\nEjecución " + (i + 1));
                System.out.println("\nEjecución " + (i + 1));

                for (int j = 0; j < FICHEROS.size(); j++)
                    try {
                        System.out.print("\t- " + FICHEROS.get(j).getTag());
                        Integer[] sol = null;
                        if (method.getParameterTypes().length > 0)
                            try {
                                sol = (Integer[]) method.invoke(FICHEROS.get(j),
                                        (Object) TIEMPOS[j], (Object) SEMILLAS_AC[i]);
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(AntColonyTSP.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        else
                            sol = (Integer[]) method.invoke(FICHEROS.get(j),
                                    new Object[0]);
                        double valor = FICHEROS.get(j).calcularValorSolucion(sol);
                        long valorFinal = Math.round(valor);
                        pw.write(";" + valorFinal);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(AntColonyTSP.class.getName()).log(Level.SEVERE, null, ex);
                    }

            }
        }
    }

    public static double getAlfa() {
        return ALFA;
    }

    public static double getBeta() {
        return BETA;
    }

    protected double[][] inicializarMatrizFeromona(double valor) {
        int n = numCiudades();
        double[][] matrizFeromona = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j)
                    matrizFeromona[i][j] = valor;
                else
                    matrizFeromona[i][j] = 0d;
        return matrizFeromona;
    }

    protected void actualizarMatrizFeromona(double[][] matrizFeromona,
            Integer[] mejorSolucion, double costeMejorSolucion,
            double maxFeromona, double minFeromona) {
        // primero evaporamos feromona
        for (int i = 0; i < matrizFeromona.length; i++)
            for (int j = i + 1; j < matrizFeromona.length; j++) {
                double valor = matrizFeromona[i][j] * (1d - RO);
                matrizFeromona[i][j] = matrizFeromona[j][i] = Math.max(valor,
                        minFeromona);
            }
        double valor = Math.min(1d / costeMejorSolucion, maxFeromona);
        for (int i = 0; i < mejorSolucion.length - 1; i++) {
            int pos1 = mejorSolucion[i] - 1;
            int pos2 = mejorSolucion[i + 1] - 1;
            matrizFeromona[pos1][pos2] += valor;
            matrizFeromona[pos2][pos1] += valor;
        }
    }

    protected void actualizarMatrizFeromona(double[][] matrizFeromona,
            Integer[][] soluciones, double[] costeSoluciones) {
        // primero evaporamos feromona
        for (int i = 0; i < matrizFeromona.length; i++)
            for (int j = i + 1; j < matrizFeromona.length; j++) {
                double valor = matrizFeromona[i][j] * (1d - RO);
                matrizFeromona[i][j] = matrizFeromona[j][i] = valor;
            }
        // ahora a cada arco de cada solución hallada le aumentamos su valor de
        // feromona
        for (int i = 0; i < soluciones.length; i++) {
            Integer[] actual = soluciones[i];
            for (int j = 0; j < actual.length - 1; j++) {
                int pos1 = actual[j] - 1;
                int pos2 = actual[j + 1] - 1;
                double coste = costeSoluciones[i];
                matrizFeromona[pos1][pos2] += 1d / coste;
                matrizFeromona[pos2][pos1] += 1d / coste;
            }
        }
    }

    protected void suavizarFeromona(double[][] matrizFeromona,
            double maxFeromona, double lambda) {
        // se usa a modo de reinicialización
        // se suele usar un valor de lambda de 0.5
        for (int i = 0; i < matrizFeromona.length; i++)
            for (int j = i + 1; j < matrizFeromona.length; j++)
                matrizFeromona[i][j] = matrizFeromona[i][j] + lambda
                        * (maxFeromona - matrizFeromona[i][j]);
    }

    protected void reiniciarFeromona(double[][] matrizFeromona,
            double maxFeromona) {
        for (int i = 0; i < matrizFeromona.length; i++)
            for (int j = i + 1; j < matrizFeromona.length; j++)
                matrizFeromona[i][j] = maxFeromona;
    }

    protected int mejorVecino(Integer[][] soluciones, double[] costeSoluciones) {
        int pos = 0;
        double mejorValor = costeSoluciones[pos];
        for (int i = 1; i < soluciones.length; i++) {
            double valorVecino = calcularValorSolucion(soluciones[i]);
            if (valorVecino < mejorValor) {
                mejorValor = valorVecino;
                pos = i;
            }
        }
        return pos;
    }

    public Integer[] antSystem(long milis, long semilla) {
        int n = numCiudades();
        long numEvaluaciones = 0;
        double feromona = 1d / (n * calcularValorSolucion(algoritmoVoraz()));
        long tiempo = System.currentTimeMillis();
        double[] costeSoluciones = new double[numHormigas];
        Integer[] mejorSolucion = new Integer[n];
        double costeMejorSolucion = Double.POSITIVE_INFINITY;
        // creamos la matriz de feromona y la inicializamos
        double[][] matrizFeromona = inicializarMatrizFeromona(feromona);
        // inicializamos las soluciones
        Integer[][] soluciones = new Integer[numHormigas][n];
        Hormiga[] hormigas = new Hormiga[numHormigas];
        while (System.currentTimeMillis() - tiempo < milis) {
            // incializamos las hormigas
            for (int i = 0; i < numHormigas; i++)
                hormigas[i] = new Hormiga(matrizFeromona, soluciones[i], this,
                        semilla);
            for (int i = 0; i < numHormigas; i++)
                hormigas[i].start();
            // AHORA HAY QUE PARARSE HASTA QUE TERMINEN
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < numHormigas; i++)
                costeSoluciones[i] = calcularValorSolucion(soluciones[i]);
            numEvaluaciones += numHormigas;
            int pos = mejorVecino(soluciones, costeSoluciones);
            Integer[] solucion = soluciones[pos];
            double costeSolucion = Math.round(costeSoluciones[pos]);
            if (costeSolucion < costeMejorSolucion) {
                costeMejorSolucion = costeSolucion;
                System.arraycopy(solucion, 0, mejorSolucion, 0, solucion.length);
                //System.out.println("Nuevo coste hallado: " + costeMejorSolucion);
            }
            // ACTUALIZAR LA MATRIZ DE FEROMONA
            actualizarMatrizFeromona(matrizFeromona, soluciones,
                    costeSoluciones);
            printSolutions(solucion, mejorSolucion, costeMejorSolucion);
        }
        System.out.println("\nEJECUCION TERMINADA. Evaluaciones totales = "
                + numEvaluaciones + "\n Coste de la solucion encontrada = "
                + costeMejorSolucion);
        return mejorSolucion;
    }

    private void printSolutions(final Integer[] local, final Integer[] mejor, double costeMejor) {
        vista.printSolutions(this, local, mejor, costeMejor);
    }

    public Integer[] maxMinAntSystem(long milis, long semilla) {
        int n = numCiudades();
        long numEvaluaciones = 0;
        long fragmentoTiempo = (long) (n * 300L);
        long tiempoMejorSolucion = 0;
        double maxFeromona = 1d, minFeromona = 0d;
        long tiempo = System.currentTimeMillis();
        double[] costeSoluciones = new double[numHormigas];
        Integer[] mejorSolucion = new Integer[n];
        double costeMejorSolucion = Double.POSITIVE_INFINITY;
        // creamos la matriz de feromona y la inicializamos
        double[][] matrizFeromona = inicializarMatrizFeromona(maxFeromona);
        // inicializamos las soluciones
        Integer[][] soluciones = new Integer[numHormigas][n];
        Hormiga[] hormigas = new Hormiga[numHormigas];
        while (System.currentTimeMillis() - tiempo < milis) {
            // incializamos las hormigas
            for (int i = 0; i < numHormigas; i++)
                hormigas[i] = new Hormiga(matrizFeromona, soluciones[i], this,
                        semilla);
            for (int i = 0; i < numHormigas; i++)
                hormigas[i].start();
            // AHORA HAY QUE PARARSE HASTA QUE TERMINEN
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < numHormigas; i++)
                costeSoluciones[i] = calcularValorSolucion(soluciones[i]);
            numEvaluaciones += numHormigas;
            int pos = mejorVecino(soluciones, costeSoluciones);
            Integer[] solucion = soluciones[pos];
            double costeSolucion = Math.round(costeSoluciones[pos]);
            if (costeSolucion < costeMejorSolucion) {
                tiempoMejorSolucion = System.currentTimeMillis();
                costeMejorSolucion = Math.round(costeSolucion);
                System.arraycopy(solucion, 0, mejorSolucion, 0, solucion.length);
                /*
                 System.out.println("\nNuevo coste hallado: " + costeMejorSolucion
                 + "\nNumero Iteraciones: " + numEvaluaciones);
                 */
                // actualizamos los valores máximos y mínimos de feromona
                maxFeromona = 1d / (RO * costeMejorSolucion);
                minFeromona = maxFeromona / (2 * n);
            }
            // ACTUALIZAR LA MATRIZ DE FEROMONA
            if (System.currentTimeMillis() - tiempoMejorSolucion < fragmentoTiempo)
                // si es menor que un tiempo determinado actualizamos
                actualizarMatrizFeromona(matrizFeromona, solucion,
                        costeSolucion, maxFeromona, minFeromona);
            else {
                // y sino, se ha estancado, por lo que reiniciamos
                //System.out.println("Se reinicia la matriz");
                tiempoMejorSolucion = System.currentTimeMillis();
                reiniciarFeromona(matrizFeromona, maxFeromona);
            }
            printSolutions(solucion, mejorSolucion, costeMejorSolucion);
        }
        System.out.println("\nEJECUCION TERMINADA. Evaluaciones totales = "
                + numEvaluaciones + "\n Coste de la solucion encontrada = "
                + costeMejorSolucion);
        return mejorSolucion;
    }

    public Integer[] busquedaTabu(Integer[] solucion) {
        int numVecinos = 40, n = numCiudades();
        int maxIteraciones = 40 * n;
        int itReinicializacion = maxIteraciones / 5, iteraciones = 0;
        int[][] matrizFrecuencias = new int[n][n];
        Integer[][] listaTabu = new Integer[n][2];
        Random random = new Random();
        ArrayList<Cambio> parejas = generarParejas();
        Collections.shuffle(parejas);

        // generamos la solucion inicial aleatoriamente
        Integer[] actual = solucion;
        double valorActual = calcularValorSolucion(actual);

        // y guardamos a parte el mejor resultado
        Integer[] mejorResultado = new Integer[actual.length];
        System.arraycopy(actual, 0, mejorResultado, 0, actual.length);
        double mejorValor = valorActual;
        int valorAleatorio = -1;
        while (iteraciones < maxIteraciones) {
            int contador = -1;
            // aqui tienen que ir el resto de posibilidades
            switch (valorAleatorio) {
                case 0:
                    // reinicializacion con nueva solucion aleatoria
                    // System.out.println("Generando nueva solucion aleatoria");
                    actual = generarSolucionAleatoria();
                    valorActual = calcularValorSolucion(actual);
                    if (valorActual < mejorValor) {
                        mejorValor = valorActual;
                        System.arraycopy(actual, 0, mejorResultado, 0,
                                actual.length);
                    }
                    int n1 = cambioCapacidadListaTabu(listaTabu.length);
                    listaTabu = new Integer[n1][2];
                    break;
                case 1:
                    // reinicializacionn desde la mejor solucionn obtenida
                    // System.out.println("Reinicializacion desde la mejor solucion obtenida");
                    System.arraycopy(mejorResultado, 0, actual, 0,
                            mejorResultado.length);
                    valorActual = mejorValor;
                    int n2 = cambioCapacidadListaTabu(listaTabu.length);
                    listaTabu = new Integer[n2][2];
                    break;
                case 2:
                case 3:
                    // uso de memoria a largo plazo para generar una nueva soluciÃƒÂ³n
                    // System.out.println("Usando memoria a largo plazo");
                    actual = generarSolucionGreedyLargoPlazo(matrizFrecuencias);
                    valorActual = calcularValorSolucion(actual);
                    if (valorActual < mejorValor) {
                        mejorValor = valorActual;
                        System.arraycopy(actual, 0, mejorResultado, 0,
                                actual.length);
                    }
                    int n3 = cambioCapacidadListaTabu(listaTabu.length);
                    listaTabu = new Integer[n3][2];
                    break;
            }

            actualizarMatrizFrecuencias(matrizFrecuencias, actual);
            // aqui comenzamos las iteraciones en sÃƒÂ­
            for (int h = 0; h < itReinicializacion; h++) {
                // generamos los 40 vecinos y hacemos el cambio si procede
                Cambio mejorCambio = null;
                double valorMejorVecino = Double.POSITIVE_INFINITY;
                for (int i = 0; i < numVecinos; i++) {
                    Cambio cambio = parejas.get(random.nextInt(parejas.size()));
                    double valorVecino = calcularValorVecino_Intercambio(
                            actual, valorActual, cambio);
                    if (valorVecino > mejorValor
                            && esTabu(cambio, actual, listaTabu))
                        continue;
                    if (valorVecino < valorMejorVecino) {
                        mejorCambio = cambio;
                        valorMejorVecino = valorVecino;
                    }
                }
                iteraciones++;
                if (!Double.isInfinite(valorMejorVecino)) {
                    // introducimos en la lista tabÃƒÂº el movimiento
                    listaTabu[contador = (contador + 1) % listaTabu.length][0] = actual[mejorCambio
                            .getPos1()];
                    listaTabu[contador][1] = mejorCambio.getPos2();
                    listaTabu[contador = (contador + 1) % listaTabu.length][0] = actual[mejorCambio
                            .getPos2()];
                    listaTabu[contador][1] = mejorCambio.getPos1();
                    // hacemos el movimiento acabadas las 40 iteraciones
                    if (valorMejorVecino < valorActual) {
                        swap(actual, mejorCambio);
                        valorActual = valorMejorVecino;
                    }
                    // actualizamos la matriz de frecuencias
                    actualizarMatrizFrecuencias(matrizFrecuencias, actual);
                }
            }
            // si es mejor que lo que habÃƒÂ­a hasta ahora se cambia
            if (valorActual < mejorValor) {
                mejorValor = valorActual;
                System.arraycopy(actual, 0, mejorResultado, 0, actual.length);
            }
            valorAleatorio = random.nextInt(4);
        }
        return mejorResultado;
    }
}
