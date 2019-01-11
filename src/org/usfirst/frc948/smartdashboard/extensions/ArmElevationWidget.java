package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.Widget;
import edu.wpi.first.smartdashboard.properties.DoubleProperty;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.types.DataType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;


public class ArmElevationWidget extends Widget {

    public static final String NAME = "Arm Elevation Widget";
    public static final DataType[] TYPES = { DataType.NUMBER };
    
    public final DoubleProperty armAngleRangeProperty = new DoubleProperty(this, "Arm Angle Range (degrees)", 80.0);
    
    private double pctElevation = 0.0;
    
    @Override
    public void setValue(Object o) {
        if (o instanceof Double) {
            pctElevation = ((Double) o);
            repaint();
        }
    }

    @Override
    public void init() {
       this.setPreferredSize(new Dimension(350, 350));
    }

    @Override
    public void propertyChanged(Property prprt) {
        
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Rectangle bounds = g.getClipBounds();
        double lineLength = Math.min(bounds.width, bounds.height);
        
        // set draw color
        g.setColor(Color.black);
        
        // draw lower bound (horizontal)
        g.drawLine(0, bounds.height - 1, (int) lineLength, bounds.height - 1);
        
        // convert degrees to radians
        double angleRangeRadians = armAngleRangeProperty.getValue() * Math.PI / 180.0;
        
        // draw upper bound (almost vertical)
        double x2 = lineLength * Math.cos(angleRangeRadians);
        double y2 = lineLength * Math.sin(angleRangeRadians);
        g.drawLine(0, bounds.height - 1, (int)x2, bounds.height - 1 - (int)y2);
        
        // draw arm true position
        x2 = lineLength * Math.cos((1.0 - pctElevation) * angleRangeRadians);
        y2 = lineLength * Math.sin((1.0 - pctElevation) * angleRangeRadians);
        g.drawLine(0, bounds.height - 1, (int)x2, bounds.height - 1 - (int)y2);
    }
    
    private static ArmElevationWidget widget;
    public static void main(String[] args) {
        widget = new ArmElevationWidget();
        WidgetTester.testWidget(widget);
        (new Timer()).schedule(new TimerTask() {
            private double elevation = 0;
            private double direction = 1.0 / 256.0;
            @Override
            public void run() {
                if (elevation < 0.0 || elevation > 1.0) direction *= -1.0;
                elevation += direction;
                widget.setValue(new Double(elevation));
            }
        }, 0, 5);
    }
    
}
