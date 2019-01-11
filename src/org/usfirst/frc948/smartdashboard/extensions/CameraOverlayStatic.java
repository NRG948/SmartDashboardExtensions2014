package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.robot.Robot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

/**
 * Overlay for camera widget for FRC 2013 Ultimate Ascent.
 * Draws a box around targets found by image processing code (runs on robot).
 */
public class CameraOverlayStatic extends StaticWidget implements ITableListener {
    
    public static final String NAME = "Vision Target Overlay (v.4/24/2013)";
    
    private ITable table;
    
    @Override
    public void init() {
        this.setPreferredSize(new Dimension(320, 240));
        this.setResizable(false);
        
        table = (NetworkTable) Robot.getTable("VisionTarget");
        table.addTableListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        try {
            // draw border
            g.setColor(Color.black);
            g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            
            // draw "center" line
            int lineX;
            try {
                lineX = (int) ((table.getNumber("targetCenter") + 1) * 160);
            } catch (Exception ex) {
                lineX = 0;
            }
            g.setColor(Color.white);
            g.drawLine(lineX, 0, lineX, 240);

            // consistent font
            g.setFont(new Font("Consolas", Font.BOLD, 12));

            if (table == null) {
                g.setColor(Color.red);
                g.drawString("NO DATA", 10, 200);
                return;
            }

            if (!table.getBoolean("updated")) {
                return;
            }

            int boxCount = (int) table.getNumber("targetsFound");

            if (boxCount < 1) {
                g.setColor(Color.black);
                g.drawString("NO TARGET", 10, 200);
                return;
            }

            for (int count = 0; count < boxCount; count++) {
                String prefix = "box" + count;

                int R = (int) table.getNumber(prefix + "lineR");
                int G = (int) table.getNumber(prefix + "lineG");
                int B = (int) table.getNumber(prefix + "lineB");

                int top = (int) table.getNumber(prefix + "top");
                int left = (int) table.getNumber(prefix + "left");
                int width = (int) table.getNumber(prefix + "width");
                int height = (int) table.getNumber(prefix + "height");
                
                int cmx = (int) table.getNumber(prefix + "cmx");

                g.setColor(new Color(R, G, B));
                g.drawRect(left, top, width, height);
                g.drawLine(cmx, top, cmx, top + height);
                
                double normalized_cmx = (2.0 * cmx / 320.0) - 1.0;
                double fov = 67.0 / 2.0;
                double center = 0.15;
                g.setColor(Color.green);
                g.drawString(String.valueOf(round(fov * (normalized_cmx - center), 2)), left, top + height + 20);
            }
        } catch (Exception ex) {
            g.setColor(Color.red);
            g.drawString(ex.getClass().getName(), 10, 200);
            g.drawString(ex.getLocalizedMessage(), 10, 220);
        }
    }

    @Override
    public void valueChanged(ITable source, String key, Object value, boolean isNew) {
        if ("updated".equals(key)) {
            repaint();
        }
    }

    @Override
    public void propertyChanged(Property prprt) {
        
    }
    
    private static double round(double val, int digits) {
        double x = Math.pow(10, digits);
        return Math.round(val * x) / x;
    }

}
