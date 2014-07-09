package com.molina.tsp;

import java.io.BufferedReader;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TSP {

    protected final String TAG;
    protected Ciudad[] ciudades;
    protected double[][] distancias;
    public static final ArrayList<TSP> FICHEROS = new ArrayList<>();
    public static final Long[] SEMILLAS = {12345678L, 12345678L, 34567812L,
        45678123L, 56781234L, 67812345L, 78123456L, 81234567L, 87654321L,
        18765432L};
    // Greedy Algorithm
    public static final String TSP_GREEDY = "algoritmoVoraz";
    // Local Search Hill Climbing
    public static final String TSP_LS_HC = "blAscensionColinas";
    // Local Search First Better Neighbor
    public static final String TSP_LS_FBN = "blPrimerMejorVecino";
    // Local Search + Greedy
    public static final String TSP_LS_GREEDY = "blGreedy";
    // Local Search Basic Multiboot
    public static final String TSP_LS_BM = "busquedaMultiarranqueBasica";
    // Simulated Annealing using Swap
    public static final String TSP_SA_S = "enfriamientoSimulado_Intercambio";
    // Simulated Annealing using Position
    public static final String TSP_SA_P = "enfriamientoSimulado_Posicion";
    // Tabu Search
    public static final String TSP_TS = "busquedaTabu";
    // GRASP
    public static final String TSP_GRASP = "GRASP";
    // ILS
    public static final String TSP_ILS = "ILS";
    // VNS
    public static final String TSP_VNS = "VNS";

    public Ciudad[] getCiudades() {
        return ciudades;
    }

    public double[][] getDistancias() {
        return distancias;
    }

    protected class Cambio {

        protected int pos1;
        protected int pos2;

        public Cambio(int a, int b) {
            pos1 = a;
            pos2 = b;
        }

        public int getPos1() {
            return pos1;
        }

        public int getPos2() {
            return pos2;
        }
    }

    protected static class EjecucionMultihilo {

        protected static Vector<Integer[]> soluciones;

        public static Integer[][] ejecutar(final TSP padre,
                final int numSoluciones, final Method method,
                final Vector<Integer[]> inicio) {
            soluciones = new Vector<>(numSoluciones);
            int numProcesadores = Runtime.getRuntime().availableProcessors();
            final Object CANDADO = new Object();
            final Runnable[] HILOS = new Runnable[numProcesadores];
            for (int i = 0; i < HILOS.length; i++) {
                HILOS[i] = new Runnable() {
                    @Override
                    public void run() {
                        while (soluciones.size() < (numSoluciones
                                - HILOS.length + 1))
                            try {
                                Integer[] param = inicio
                                        .remove(inicio.size() - 1);
                                // HAY QUE HACER UN CASTING A OBJECT!!!!!
                                Integer[] solucion = (Integer[]) method.invoke(
                                        padre, (Object) param);
                                soluciones.add(solucion);
                            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                                Logger.getLogger(TSP.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        if (soluciones.size() >= numSoluciones)
                            synchronized (CANDADO) {
                                CANDADO.notifyAll();
                            }
                    }
                };
                new Thread(HILOS[i]).start();
            }
            synchronized (CANDADO) {
                try {
                    CANDADO.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Integer[][] devolver = soluciones.toArray(new Integer[soluciones
                    .size()][]);
            soluciones = null;
            return devolver;
        }
    }

    public TSP(File file, String TAG) throws IOException {
        this.TAG = TAG.toUpperCase();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String contenido = "";
            while (!contenido.contains("DIMENSION"))
                contenido = br.readLine();
            int tam = Integer.parseInt(contenido.split(" ")[2]);
            ciudades = new Ciudad[tam];
            br.readLine();
            br.readLine();
            contenido = br.readLine();
            int contador = 0;
            while (!contenido.equals("EOF")) {
                String[] datos = contenido.split(" ");
                int numero = Integer.parseInt(datos[0]);
                double x = Double.parseDouble(datos[1]);
                double y = Double.parseDouble(datos[2]);
                ciudades[contador++] = new Ciudad(numero, x, y);
                contenido = br.readLine();
            }
        }
        calcularMatrizDistancias();
        TSP.FICHEROS.add(this);
    }

    protected void barajarSublista(Integer[] sol, int tamSublista) {
        Random random = new Random();
        int i = random.nextInt(sol.length - tamSublista);
        Collections.shuffle(Arrays.asList(sol).subList(i, i + tamSublista));
    }

    protected ArrayList<Cambio> generarParejas() {
        int tam = (numCiudades() - 1) * (numCiudades() - 2) / 2;
        ArrayList<Cambio> vector = new ArrayList<>(tam);
        for (int i = 1; i < numCiudades(); i++)
            for (int j = i + 1; j < numCiudades(); j++)
                vector.add(new TSP.Cambio(i, j));
        return vector;
    }

    protected static double distancia(Ciudad c1, Ciudad c2) {
        return Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2d)
                + Math.pow(c1.getY() - c2.getY(), 2d));
    }

    public double recorridoReducido(int c1, int c2, Integer[] sol) {
        Ciudad ciudad1 = ciudades[c1 - 1];
        Ciudad ciudad2 = ciudades[c2 - 1];
        return calcularValorSolucion(sol) - distancia(ciudad1, ciudad2);
    }

    protected void calcularMatrizDistancias() {
        int tam = ciudades.length;
        distancias = new double[tam][tam];
        for (int i = 0; i < tam; i++)
            for (int j = 0; j < tam; j++)
                distancias[i][j] = distancia(ciudades[i], ciudades[j]);
    }

    protected Ciudad ciudadMasCercana(Ciudad ciudad, boolean[] excepciones) {
        Ciudad masCercana = null;
        double distancia = Double.POSITIVE_INFINITY;
        for (int i = 0; i < ciudades.length; i++)
            if (!excepciones[i]
                    && distancias[ciudad.getNumero() - 1][i] < distancia) {
                distancia = distancias[ciudad.getNumero() - 1][i];
                masCercana = ciudades[i];
            }
        return masCercana;
    }

    protected Ciudad ciudadMasCercana(Ciudad ciudad,
            ArrayList<Integer> excepciones, int[][] matrizFrecuencias,
            double Dmax, double Dmin, int Fmax) {
        Ciudad masCercana = null;
        double mu = 0.3;
        double distancia = Double.POSITIVE_INFINITY;
        for (int i = 0; i < ciudades.length; i++) {
            int pos = ciudad.getNumero() - 1;
            double distanciaVecina = distancias[pos][i] + mu * (Dmax - Dmin)
                    * matrizFrecuencias[pos][i] / Fmax;
            if (!excepciones.contains(ciudades[i].getNumero())
                    && distanciaVecina < distancia) {
                distancia = distanciaVecina;
                masCercana = ciudades[i];
            }
        }
        return masCercana;
    }

    protected Integer[] generarSolucionAleatoria(long semilla) {
        Integer[] vector = new Integer[numCiudades()];
        for (int i = 0; i < numCiudades(); i++)
            vector[i] = ciudades[i].getNumero();
        Collections.shuffle(Arrays.asList(vector).subList(1, vector.length),
                new Random(semilla));
        return vector;
    }

    protected Integer[] generarSolucionAleatoria() {
        Integer[] vector = new Integer[numCiudades()];
        for (int i = 0; i < numCiudades(); i++)
            vector[i] = ciudades[i].getNumero();
        Collections.shuffle(Arrays.asList(vector).subList(1, vector.length));
        return vector;
    }

    protected Integer[] mejorVecino(Integer[][] vecindario) {
        Integer[] mejorVecino = vecindario[0];
        double mejorValor = calcularValorSolucion(mejorVecino);
        for (int i = 1; i < vecindario.length; i++) {
            double valorVecino = calcularValorSolucion(vecindario[i]);
            if (valorVecino < mejorValor) {
                mejorValor = valorVecino;
                mejorVecino = vecindario[i];
            }
        }
        return mejorVecino;
    }

    protected void swap(Integer[] vector, Cambio cambio) {
        if (cambio != null) {
            Integer aux = vector[cambio.getPos1()];
            vector[cambio.getPos1()] = vector[cambio.getPos2()];
            vector[cambio.getPos2()] = aux;
        }
    }

    protected double calcularValorVecino_Intercambio(Integer[] a,
            double valorActual, Cambio cambio) {
        if (cambio == null)
            return Double.POSITIVE_INFINITY;
        int n = numCiudades();
        int i = cambio.getPos1();
        int j = cambio.getPos2();
        // si estÃ¡n consecutivos...
        if (Math.abs(i - j) == 1 || (i == 0 && j == a.length - 1))
            return valorActual - distancias[a[i - 1] - 1][a[i] - 1]
                    - distancias[a[j] - 1][a[(j + 1) % n] - 1]
                    + distancias[a[i - 1] - 1][a[j] - 1]
                    + distancias[a[i] - 1][a[(j + 1) % n] - 1];
        else
            return valorActual - distancias[a[i - 1] - 1][a[i] - 1]
                    - distancias[a[i] - 1][a[i + 1] - 1]
                    - distancias[a[j - 1] - 1][a[j] - 1]
                    - distancias[a[j] - 1][a[(j + 1) % n] - 1]
                    + distancias[a[i - 1] - 1][a[j] - 1]
                    + distancias[a[j] - 1][a[i + 1] - 1]
                    + distancias[a[j - 1] - 1][a[i] - 1]
                    + distancias[a[i] - 1][a[(j + 1) % n] - 1];
    }

    protected double calcularValorVecino_Posicion(Integer[] a,
            double valorActual, Cambio cambio) {
        if (cambio == null)
            return Double.POSITIVE_INFINITY;
        int i = cambio.getPos1();
        int j = cambio.getPos2();
        if (Math.abs(i - j) == 1)
            return calcularValorVecino_Intercambio(a, valorActual, cambio);
        else
            return valorActual - distancias[a[i - 1] - 1][a[i] - 1]
                    - distancias[a[i] - 1][a[i + 1] - 1]
                    + distancias[a[i - 1] - 1][a[i + 1] - 1]
                    + distancias[a[i] - 1][a[j] - 1]
                    + distancias[a[i] - 1][a[(j + 1) % a.length] - 1]
                    - distancias[a[j] - 1][a[(j + 1) % a.length] - 1];

    }

    protected Cambio mejorVecino(Integer[] solucionInicial,
            double valorInicial, ArrayList<Cambio> cambios) {
        Cambio mejor = cambios.get(0);
        double mejorValor = calcularValorVecino_Intercambio(solucionInicial,
                valorInicial, mejor);
        for (int i = 1; i < cambios.size(); i++) {
            Cambio vecino = cambios.get(i);
            double valorVecino = calcularValorVecino_Intercambio(
                    solucionInicial, valorInicial, vecino);
            if (valorVecino < mejorValor) {
                mejorValor = valorVecino;
                mejor = vecino;
            }
        }
        return mejor;
    }

    protected Cambio primerMejorVecinoEncontrado(Integer[] solucionInicial,
            double valorInicial, ArrayList<Cambio> cambios) {
        int puntero = 0;
        int rango = cambios.size();
        Random random = new Random();
        for (int i = 0; i < cambios.size(); i++) {
            int pos = puntero + random.nextInt(rango);
            Cambio vecino = cambios.get(pos);
            double valorVecino = calcularValorVecino_Intercambio(
                    solucionInicial, valorInicial, vecino);
            Collections.swap(cambios, puntero++, pos);
            rango--;
            if (valorVecino < valorInicial)
                return vecino;
        }
        return null;
    }

    protected Integer[] cambioIndice(Integer[] sol, Cambio cambio) {
        int i = cambio.getPos1();
        int j = cambio.getPos2();
        Integer aux = sol[j];
        sol[j] = sol[i];
        for (int u = j - 1; u >= i; u--) {
            Integer aux2 = sol[u];
            sol[u] = aux;
            aux = aux2;
        }
        return sol;
    }

    protected boolean esTabu(Cambio cambio, Integer[] actual,
            Integer[][] listaTabu) {
        Integer[] mov1 = {actual[cambio.getPos1()], cambio.getPos2()};
        Integer[] mov2 = {actual[cambio.getPos2()], cambio.getPos1()};
        for (int i = 0; i < listaTabu.length; i++) {
            Integer[] movimiento = listaTabu[i];
            if (Arrays.equals(movimiento, mov1)
                    || Arrays.equals(movimiento, mov2))
                return true;
        }
        return false;
    }

    protected int cambioCapacidadListaTabu(int capacidadInicial) {
        Random random = new Random();
        int newTam;
        if (random.nextBoolean())
            newTam = (int) (capacidadInicial * 1.5);
        else
            newTam = capacidadInicial / 2;
        return newTam;
    }

    protected void actualizarMatrizFrecuencias(int[][] matrizFrecuencias,
            Integer[] sol) {
        for (int i = 0; i < sol.length; i++) {
            int pos1 = sol[i] - 1;
            int pos2 = sol[(i + 1) % sol.length] - 1;
            matrizFrecuencias[pos1][pos2]++;
            matrizFrecuencias[pos2][pos1]++;
        }
    }

    protected Integer[] generarSolucionGreedyLargoPlazo(
            int[][] matrizFrecuencias) {
        int tam = numCiudades();
        // obtenemos los valores de Fmax, Dmax y Dmin
        double Dmax = 0, Dmin = Double.POSITIVE_INFINITY;
        int Fmax = 0;
        for (int i = 0; i < tam; i++)
            for (int j = i + 1; j < tam; j++) {
                double valor = distancias[i][j];
                if (valor > Dmax)
                    Dmax = valor;
                if (valor < Dmin)
                    Dmin = valor;
                if (matrizFrecuencias[i][j] > Fmax)
                    Fmax = matrizFrecuencias[i][j];
            }
        // el resto sigue parecido
        ArrayList<Integer> solucion = new ArrayList<>(tam);
        Ciudad actual = getCiudadInicial();
        solucion.add(actual.getNumero());
        for (int i = 1; i < tam; i++) {
            actual = ciudadMasCercana(actual, solucion, matrizFrecuencias,
                    Dmax, Dmin, Fmax);
            solucion.add(actual.getNumero());
        }
        return solucion.toArray(new Integer[tam]);
    }

    protected List<Ciudad> obtenerListaCiudadesCandidatas(final Ciudad origen,
            boolean[] excepciones, int tamLista) {
        List<Ciudad> candidatas = new ArrayList<>();
        for (int i = 0; i < ciudades.length; i++)
            if (!excepciones[i])
                candidatas.add(ciudades[i]);
        Comparator<Ciudad> c = new Comparator<Ciudad>() {
            @Override
            public int compare(Ciudad c1, Ciudad c2) {
                int posOrigen = origen.getNumero() - 1;
                int pos1 = c1.getNumero() - 1;
                int pos2 = c2.getNumero() - 1;
                double distC1 = distancias[posOrigen][pos1];
                double distC2 = distancias[posOrigen][pos2];
                if (distC1 < distC2)
                    return -1;
                // hay que poner el comparador para que te ponga el igual!!!
                if (distC1 - distC2 == 0d)
                    return 0;
                return 1;
            }
        };
        Collections.sort(candidatas, c);
        int tamFinal = Math.min(tamLista, candidatas.size());
        return candidatas.subList(0, tamFinal);
    }

    protected Integer[] generarSolucionGreedyGRASP() {
        Random random = new Random();
        int tam = numCiudades();
        int tamLista = Math.max((int) (0.1 * tam), 2);
        Integer[] solucion = new Integer[tam];
        boolean[] excepciones = new boolean[tam];// se inicia solo a false
        // escogemos una ciudad inicial aleatoria
        int posInicial = random.nextInt(ciudades.length);
        Ciudad actual = ciudades[posInicial];
        solucion[0] = actual.getNumero();
        excepciones[posInicial] = true;
        for (int i = 1; i < tam; i++) {
            List<Ciudad> candidatas = obtenerListaCiudadesCandidatas(actual,
                    excepciones, tamLista);
            actual = candidatas.get(random.nextInt(candidatas.size()));
            candidatas.clear();
            solucion[i] = actual.getNumero();
            excepciones[actual.getNumero() - 1] = true;
        }
        return solucion;
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
                        // le escribimos el carÃ¡cter vacÃ­o en sustituciÃ³n del '\n'
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
                System.out.print("\nEjecución " + (i + 1));
                for (int j = 0; j < FICHEROS.size(); j++)
                    try {
                        System.out.print(" - " + FICHEROS.get(j).getTag());
                        Integer[] sol;
                        if (method.getParameterTypes().length > 0)
                            sol = (Integer[]) method.invoke(FICHEROS.get(j),
                                    (Object) SEMILLAS[i]);
                        else
                            sol = (Integer[]) method.invoke(FICHEROS.get(j),
                                    new Object[0]);
                        double valor = FICHEROS.get(j).calcularValorSolucion(sol);
                        long valorFinal = Math.round(valor);
                        pw.write(";" + valorFinal);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(TSP.class.getName()).log(Level.SEVERE, null, ex);
                    }

            }
        }
    }

    public Integer[] blAscensionColinas(Integer[] sol) {
        Integer[] actual = sol;
        double valorActual = calcularValorSolucion(actual);
        boolean condicionSalida;
        ArrayList<Cambio> cambios = generarParejas();
        do {
            condicionSalida = true;
            // codigo nuevo
            Cambio mejorCambio = mejorVecino(actual, valorActual, cambios);
            double valorMejorVecino = calcularValorVecino_Intercambio(actual,
                    valorActual, mejorCambio);
            if (valorMejorVecino < valorActual) {
                valorActual = valorMejorVecino;
                swap(actual, mejorCambio);
                condicionSalida = false;
            }
        } while (!condicionSalida);
        return actual;
    }

    public String getTag() {
        return TAG;
    }

    public Ciudad getCiudadInicial() {
        return ciudades[0];
    }

    public int numCiudades() {
        return ciudades.length;
    }

    public double calcularValorSolucion(Integer[] sol) {
        // sumamos el ultimo y primero
        double valor = distancias[sol[sol.length - 1] - 1][sol[0] - 1];
        // sumamos el resto
        for (int i = 0; i < sol.length - 1; i++)
            valor += distancias[sol[i] - 1][sol[i + 1] - 1];
        return valor;
    }

    public void pintarSolucion(Integer[] sol) {
        System.out.print("\n" + TAG + ": { ");
        for (int i = 0; i < sol.length; i++) {
            System.out.print(sol[i]);
            if (i < sol.length - 1)
                System.out.print(" -> ");
        }
        System.out.println(" }");
        System.out.println("Valor de la solución: "
                + calcularValorSolucion(sol));
    }

    // ////////////////////////////////////////////////////////////
    // /////////////////ALGORITMOS DE LA PR�?CTICA//////////////////
    // ////////////////////////////////////////////////////////////
    public Integer[] algoritmoVoraz() {
        int tam = numCiudades();
        Integer[] solucion = new Integer[tam];
        boolean[] excepciones = new boolean[tam];// se inicia solo a false
        Ciudad actual = getCiudadInicial();
        solucion[0] = actual.getNumero();
        excepciones[0] = true;
        for (int i = 1; i < tam; i++) {
            actual = ciudadMasCercana(actual, excepciones);
            solucion[i] = actual.getNumero();
            excepciones[actual.getNumero() - 1] = true;
        }
        return solucion;
    }

    public Integer[] blAscensionColinas(long semilla) {
        Integer[] actual = generarSolucionAleatoria(semilla);
        double valorActual = calcularValorSolucion(actual);
        boolean condicionSalida;
        ArrayList<Cambio> cambios = generarParejas();
        do {
            condicionSalida = true;
            // codigo nuevo
            Cambio mejorCambio = mejorVecino(actual, valorActual, cambios);
            double valorMejorVecino = calcularValorVecino_Intercambio(actual,
                    valorActual, mejorCambio);
            if (valorMejorVecino < valorActual) {
                valorActual = valorMejorVecino;
                swap(actual, mejorCambio);
                condicionSalida = false;
            }
        } while (!condicionSalida);
        return actual;
    }

    public Integer[] blPrimerMejorVecino(long semilla) {
        Integer[] actual = generarSolucionAleatoria(semilla);
        double valorActual = calcularValorSolucion(actual);
        ArrayList<Cambio> parejas = generarParejas();
        Collections.shuffle(parejas);
        boolean condicionSalida;
        do {
            condicionSalida = true;
            Cambio seleccion = primerMejorVecinoEncontrado(actual, valorActual,
                    parejas);
            double valorVecino = calcularValorVecino_Intercambio(actual,
                    valorActual, seleccion);
            if (valorVecino < valorActual) {
                swap(actual, seleccion);
                valorActual = valorVecino;
                condicionSalida = false;
            }
        } while (!condicionSalida);
        return actual;
    }

    public Integer[] blGreedy() {
        Integer[] actual = algoritmoVoraz();
        double valorActual = calcularValorSolucion(actual);
        boolean condicionSalida;
        ArrayList<Cambio> cambios = generarParejas();
        do {
            condicionSalida = true;
            // codigo nuevo
            Cambio mejorCambio = mejorVecino(actual, valorActual, cambios);
            double valorMejorVecino = calcularValorVecino_Intercambio(actual,
                    valorActual, mejorCambio);
            if (valorMejorVecino < valorActual) {
                valorActual = valorMejorVecino;
                swap(actual, mejorCambio);
                condicionSalida = false;
            }
        } while (!condicionSalida);
        return actual;
    }

    public Integer[] busquedaMultiarranqueBasica() {
        int numSoluciones = 50;
        Vector<Integer[]> solucionesIniciales = new Vector<>(
                numSoluciones);
        Integer[][] solucionesBL = new Integer[numSoluciones][];
        for (int i = 0; i < numSoluciones; i++)
            solucionesIniciales.add(generarSolucionAleatoria());
        if (Runtime.getRuntime().availableProcessors() == 1
                || numSoluciones <= 1)
            for (int i = 0; i < solucionesBL.length; i++)
                solucionesBL[i] = blAscensionColinas(solucionesIniciales.get(i));
        Method method = null;
        try {
            method = TSP.class.getMethod(TSP.TSP_LS_HC, Integer[].class);

            solucionesBL = EjecucionMultihilo.ejecutar(this, numSoluciones,
                    method, solucionesIniciales);
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(TSP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mejorVecino(solucionesBL);
    }

    public Integer[] enfriamientoSimulado_Intercambio(long semilla) {
        // definimos los parÃ¡metros
        double fi = 0.3;
        double mu = 0.3;
        int numVecinos = 20;
        int evaluacionesActuales = 0;
        int maxEvaluaciones = 1600 * numCiudades();
        int M = 80 * numCiudades();
        Integer[] sol = generarSolucionAleatoria(semilla);
        double costo = calcularValorSolucion(sol);
        double T0 = mu * costo / (-Math.log(fi));
        double Tf = 0.01;
        double beta = (T0 - Tf) / (M * T0 * Tf);
        ArrayList<Cambio> parejas = generarParejas();
        Collections.shuffle(parejas);
        Random random = new Random();
        double T = T0;
        // iniciamos el bucle
        while (evaluacionesActuales < maxEvaluaciones) {
            int index = random.nextInt(parejas.size() - numVecinos);
            List<Cambio> vecinos = parejas.subList(index, index + numVecinos);
            for (int i = 0; i < numVecinos; i++) {
                Cambio seleccion = vecinos.get(i);
                double costeVecino = calcularValorVecino_Intercambio(sol,
                        costo, seleccion);
                double diferencia = costeVecino - costo;
                evaluacionesActuales++;
                if (diferencia < 0 || Math.random() < Math.exp(-diferencia / T)) {
                    swap(sol, seleccion);
                    costo = costeVecino;
                }
            }
            // barajamos los vecinos que hemos usado por si vuelven a tocar
            Collections.shuffle(vecinos);
            // cambiamos la temperatura
            T = T / (1 + beta * T);
        }
        return sol;
    }

    public Integer[] enfriamientoSimulado_Posicion(long semilla) {
        // definimos los parÃ¡metros
        double fi = 0.3;
        double mu = 0.3;
        int numVecinos = 20;
        int evaluacionesActuales = 0;
        int maxEvaluaciones = 1600 * numCiudades();
        int M = 80 * numCiudades();
        Integer[] sol = generarSolucionAleatoria(semilla);
        double costo = calcularValorSolucion(sol);
        double T0 = mu * costo / (-Math.log(fi));
        double Tf = 0.001;
        double beta = (T0 - Tf) / (M * T0 * Tf);
        ArrayList<Cambio> parejas = generarParejas();
        Collections.shuffle(parejas);
        Random random = new Random();
        double T = T0;
        // iniciamos el bucle
        while (evaluacionesActuales < maxEvaluaciones) {
            int index = random.nextInt(parejas.size() - numVecinos);
            List<Cambio> vecinos = parejas.subList(index, index + numVecinos);
            for (int i = 0; i < numVecinos; i++) {
                Cambio seleccion = vecinos.get(i);
                double costeVecino = calcularValorVecino_Posicion(sol, costo,
                        seleccion);
                double diferencia = costeVecino - costo;
                evaluacionesActuales++;
                if (diferencia < 0 || Math.random() < Math.exp(-diferencia / T)) {
                    cambioIndice(sol, seleccion);
                    costo = costeVecino;
                }
            }
            // barajamos los vecinos que hemos usado por si vuelven a tocar
            Collections.shuffle(vecinos);
            // cambiamos la temperatura
            T = T / (1 + beta * T);
        }
        return sol;
    }

    public Integer[] busquedaTabu(long semilla) {
        int numVecinos = 40, n = numCiudades();
        int maxIteraciones = 40 * n;
        int itReinicializacion = maxIteraciones / 5, iteraciones = 0;
        int[][] matrizFrecuencias = new int[n][n];
        Integer[][] listaTabu = new Integer[n][2];
        Random random = new Random(semilla);
        ArrayList<Cambio> parejas = generarParejas();
        Collections.shuffle(parejas);

        // generamos la soluciÃ³n inicial aleatoriamente
        Integer[] actual = generarSolucionAleatoria(semilla);
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
                    // reinicializaciÃ³n con nueva soluciÃ³n aleatoria
                    // System.out.println("Generando nueva soluciÃ³n aleatoria");
                    actual = generarSolucionAleatoria(semilla);
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
                    // reinicializaciÃ³n desde la mejor soluciÃ³n obtenida
                    // System.out.println("ReinicializaciÃ³n desde la mejor soluciÃ³n obtenida");
                    System.arraycopy(mejorResultado, 0, actual, 0,
                            mejorResultado.length);
                    valorActual = mejorValor;
                    int n2 = cambioCapacidadListaTabu(listaTabu.length);
                    listaTabu = new Integer[n2][2];
                    break;
                case 2:
                case 3:
                    // uso de memoria a largo plazo para generar una nueva soluciÃ³n
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
            // aqui comenzamos las iteraciones en sÃ­
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
                    // introducimos en la lista tabÃº el movimiento
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
            // si es mejor que lo que habÃ­a hasta ahora se cambia
            if (valorActual < mejorValor) {
                mejorValor = valorActual;
                System.arraycopy(actual, 0, mejorResultado, 0, actual.length);
            }
            valorAleatorio = random.nextInt(4);
        }
        return mejorResultado;
    }

    public Integer[] GRASP() {
        int numSoluciones = 50;
        Vector<Integer[]> solucionesIniciales = new Vector<>(
                numSoluciones);
        Integer[][] solucionesGREEDY = new Integer[numSoluciones][];
        for (int i = 0; i < numSoluciones; i++)
            solucionesIniciales.add(generarSolucionGreedyGRASP());
        if (Runtime.getRuntime().availableProcessors() == 1
                || numSoluciones <= 1)
            for (int i = 0; i < solucionesGREEDY.length; i++)
                solucionesGREEDY[i] = blAscensionColinas(solucionesIniciales
                        .get(i));
        else
            try {
                Method method = null;
                method = TSP.class.getMethod(TSP.TSP_LS_HC, Integer[].class);
                solucionesGREEDY = EjecucionMultihilo.ejecutar(this, numSoluciones,
                        method, solucionesIniciales);
            } catch (NoSuchMethodException | SecurityException ex) {
                Logger.getLogger(TSP.class.getName()).log(Level.SEVERE, null, ex);
            }
        return mejorVecino(solucionesGREEDY);
    }

    public Integer[] ILS() {
        int numSoluciones = 50;
        Integer[] actual = generarSolucionAleatoria();
        double valorActual = calcularValorSolucion(actual);
        Integer[] mejorSolucion = new Integer[numCiudades()];
        System.arraycopy(actual, 0, mejorSolucion, 0, actual.length);
        double valorMejorSolucion = valorActual;
        for (int i = 0; i < numSoluciones; i++) {
            blAscensionColinas(actual);
            valorActual = calcularValorSolucion(actual);
            if (valorActual < valorMejorSolucion) {
                System.arraycopy(actual, 0, mejorSolucion, 0, actual.length);
                valorMejorSolucion = valorActual;
            } else
                System.arraycopy(mejorSolucion, 0, actual, 0,
                        mejorSolucion.length);
            barajarSublista(actual, numCiudades() / 4);
        }
        return mejorSolucion;
    }

    public Integer[] VNS() {
        int n = numCiudades(), blMax = 50, bl = 0, kmax = 5, k = 1;
        Integer[] solucionActual = generarSolucionAleatoria();
        Integer[] solucionVecina = Arrays.copyOf(solucionActual,
                solucionActual.length);
        double valorActual = calcularValorSolucion(solucionActual);
        while (bl < blMax) {
            if (k > kmax)
                k = 1;
            barajarSublista(solucionVecina, n / (9 - k));
            blAscensionColinas(solucionVecina);
            bl++;
            double valorVecino = calcularValorSolucion(solucionVecina);
            if (valorVecino < valorActual) {
                System.arraycopy(solucionVecina, 0, solucionActual, 0,
                        solucionVecina.length);
                valorActual = valorVecino;
                k = 1;
            } else
                k++;
        }
        return solucionActual;
    }
}
