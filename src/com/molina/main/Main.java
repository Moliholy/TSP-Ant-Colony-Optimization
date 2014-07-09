package com.molina.main;

import com.molina.tsp.AntColonyTSP;
import com.molina.view.TSPView;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static long time;

    private static void start() {
        time = System.currentTimeMillis();
    }

    private static void stop() {
        time = System.currentTimeMillis() - time;
    }

    private static long getSeconds() {
        return time / 1000;
    }

    static void ejecucionSH() {
        try {
            System.out.println("Generando solucion de Sistema de Hormigas (SH)");
            start();
            AntColonyTSP.ejecucion(5, "Sistema de Hormigas (SH)",
                    AntColonyTSP.class.getMethod(AntColonyTSP.TSP_AC_AS,
                    long.class, long.class), false);
            stop();
            System.out
                    .println("\n\nSoluci�n de B�squeda Local con Ascensi�n de Sistema de Hormigas");
            System.out.println("Tiempo invertido: " + getSeconds() / 60
                    + " minutos.");
        } catch (NoSuchMethodException | SecurityException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void ejecucionSHMM() {
        try {
            System.out
                    .println("Generando solucion de Sistema de Hormigas Min-Max (SHMM)");
            start();
            AntColonyTSP.ejecucion(5, "Sistema de Hormigas Min-Max (SHMM)",
                    AntColonyTSP.class.getMethod(AntColonyTSP.TSP_AC_ASMM,
                    long.class, long.class), false);
            stop();
            System.out.println("\n\nSolucion Sistema de Hormigas Min-Max generada");
            System.out.println("Tiempo invertido: " + getSeconds() / 60
                    + " minutos.");
        } catch (NoSuchMethodException | SecurityException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TSPView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        TSPView vista = new TSPView();
        File rat195 = new File("rat195.tsp");
        File ts225 = new File("ts225.tsp");
        File u574 = new File("u574.tsp");
        AntColonyTSP f1 = null;
        try {
            f1 = new AntColonyTSP(rat195, "rat195", 20, vista);
            new AntColonyTSP(ts225, "ts225", 20, vista);
            new AntColonyTSP(u574, "u574", 20, vista);
        } catch (IOException e) {
            e.printStackTrace();
        }
        vista.setVisible(true);

        ejecucionSH();
        ejecucionSHMM();
        //f1.maxMinAntSystem(1000000000L, 24524535323L);
                /*
         * AntColonyTSP fichero = (AntColonyTSP) Main.FICHEROS.get(0); start();
         * Integer[] solucion = fichero.maxMinAntSystem(1000 * 60 * 90L);
         * stop();
         * System.out.println("\n\nSoluci�n de Sistema de Hormigas generada");
         * System.out.println("Tiempo invertido: " + getSeconds() +
         * " segundos."); fichero.pintarSolucion(solucion);
         * System.out.println("Soluci�n aplicando BL del mejor:"); Integer[]
         * solucionBL = fichero.blAscensionColinas(solucion);
         * fichero.pintarSolucion(solucionBL); double costeMejor =
         * fichero.recorridoReducido(solucionBL[0], solucionBL[solucionBL.length
         * - 1], solucionBL); System.out.println("Coste sin la �ltima ciudad: "
         * + costeMejor);
         */

    }
}
