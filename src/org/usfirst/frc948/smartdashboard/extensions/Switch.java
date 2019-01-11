package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.properties.StringProperty;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Switch extends StaticWidget implements MouseListener {

    public static final String NAME = "Switch";
    
    private boolean value = false;
    
    public final StringProperty labelProperty = new StringProperty(this, "Label", "Switch");
    public final StringProperty networkTableProperty = new StringProperty(this, "NetworkTable", "SmartOI");
    public final StringProperty keyProperty = new StringProperty(this, "Key", null);
    
    @Override
    public void init() {
        this.setPreferredSize(new Dimension(200, 100));
        this.setResizable(false);
        this.addMouseListener(this);
    }

    @Override
    public void propertyChanged(Property p) {
        if (p == labelProperty) {
            repaint();
        } else if (p == networkTableProperty) {
            putValue();
        } else if (p == keyProperty) {
            putValue();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawString(labelProperty.getValue(), 60, 55);

        if (value) {
            g.setColor(Color.red);
            g.drawString("ON NOW", 75, 40);
            g.drawRect(0, 0, 199, 99);
        } else {
            g.drawString("OFF NOW", 72, 70);
            g.drawRect(0, 0, 199, 99);
        }
    }
    
    private void putValue() {
        NetworkTable table = NetworkTable.getTable(networkTableProperty.getValue());
        if (table != null) {
            table.putBoolean(keyProperty.getValue(), value);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        value = !value;
        putValue();
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

}
