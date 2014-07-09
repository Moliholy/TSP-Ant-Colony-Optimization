/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.molina.view;

import com.molina.tsp.AntColonyTSP;
import com.molina.tsp.Ciudad;
import java.nio.FloatBuffer;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

/**
 *
 * @author Molina
 */
public class TSPView extends javax.swing.JFrame {

    private Integer[] local;
    private Integer[] mejor;
    private AntColonyTSP fichero;
    private GLU glu;

    private void pintarPuntos(GLAutoDrawable drawable, float red, float green, float blue) {
        GL gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        float[] color = {red, green, blue};
        gl.glColor3fv(color, 0);
        Ciudad[] ciudades = fichero.getCiudades();
        double minX = fichero.getMinX(), minY = fichero.getMinY();
        double factorX = fichero.getMaxX() - minX, factorY = fichero.getMaxY() - minY;
        gl.glBegin(GL.GL_POINTS);
        for (int i = 0; i < fichero.numCiudades(); i++)
            gl.glVertex2d((ciudades[i].getX() - minX) / factorX, (ciudades[i].getY() - minY) / factorY);
        gl.glEnd();
    }

    private void pintarSolucion(GLAutoDrawable drawable, Integer[] solucion, float red, float green, float blue, float lineWidth) {
        GL gl = drawable.getGL();
        gl.glLineWidth(lineWidth);
        float[] color = {red, green, blue};
        gl.glColor3fv(color, 0);
        Ciudad[] ciudades = fichero.getCiudades();
        double minX = fichero.getMinX(), minY = fichero.getMinY();
        double factorX = fichero.getMaxX() - minX, factorY = fichero.getMaxY() - minY;
        gl.glBegin(GL.GL_LINES);
        for (int i = 0; i < solucion.length - 1; i++) {
            gl.glVertex2d((ciudades[solucion[i] - 1].getX() - minX) / factorX, (ciudades[solucion[i] - 1].getY() - minY) / factorY);
            gl.glVertex2d((ciudades[solucion[i + 1] - 1].getX() - minX) / factorX, (ciudades[solucion[i + 1] - 1].getY() - minY) / factorY);
        }
        gl.glEnd();
    }

    /**
     * Creates new form TSPView
     */
    public TSPView() {
        initComponents();
        glu = new GLU();
        local = null;
        mejor = null;
        fichero = null;
        canvas.addGLEventListener(new GLEventListener() {
            @Override
            public void init(GLAutoDrawable drawable) {
                GL gl = drawable.getGL();
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glLoadIdentity();
                glu.gluOrtho2D(-0.05, 1.05, -0.05, 1.05);
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glLoadIdentity();

                gl.glEnable(GL.GL_POINT_SMOOTH);
                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                gl.glPointSize(6);
            }

            @Override
            public void display(GLAutoDrawable drawable) {
                if (local != null && mejor != null && fichero != null)
                    pintarPuntos(drawable, 1, 1, 1);
                pintarSolucion(drawable, local, 1, 0, 0, 0.5f);
                pintarSolucion(drawable, mejor, 0, 1, 0, 2);
            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            }

            @Override
            public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel = new javax.media.opengl.GLJPanel();
        canvas = new javax.media.opengl.GLCanvas();
        jLabel1 = new javax.swing.JLabel();
        textMejorSolucion = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TSP using Ant Colony Algorithms");

        panel.setMinimumSize(new java.awt.Dimension(600, 600));

        canvas.setName(""); // NOI18N
        canvas.setRealized(true);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(canvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel1.setText("Coste de la mejor soluci�n:");

        textMejorSolucion.setEditable(false);
        textMejorSolucion.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        textMejorSolucion.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(161, 161, 161)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(textMejorSolucion, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textMejorSolucion, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.media.opengl.GLCanvas canvas;
    private javax.swing.JLabel jLabel1;
    private javax.media.opengl.GLJPanel panel;
    private javax.swing.JTextField textMejorSolucion;
    // End of variables declaration//GEN-END:variables

    public void printSolutions(AntColonyTSP fichero, Integer[] local, Integer[] mejor, double costeMejor) {
        this.fichero = fichero;
        this.local = local;
        this.mejor = mejor;
        textMejorSolucion.setText(Double.toString(costeMejor));
        canvas.display();
    }
}