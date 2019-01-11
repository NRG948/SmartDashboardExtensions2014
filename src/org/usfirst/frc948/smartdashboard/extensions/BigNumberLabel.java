package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.Widget;
import edu.wpi.first.smartdashboard.properties.BooleanProperty;
import edu.wpi.first.smartdashboard.properties.ColorProperty;
import edu.wpi.first.smartdashboard.properties.IntegerProperty;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.types.DataType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

/**
 *
 * @author irving
 */
public class BigNumberLabel extends Widget {
    
    public static final String NAME = "Big Number Label";
    public static final DataType[] TYPES = { DataType.NUMBER };
    
    public final IntegerProperty textSize = new IntegerProperty(this, "Text Size", 36);
    public final ColorProperty textColor = new ColorProperty(this, "Text Color", Color.black);
    public final BooleanProperty round = new BooleanProperty(this, "Round value?", false);
    public final IntegerProperty roundValue = new IntegerProperty(this, "Round to nearest 10^n", 0);
    
    private double number;
    
    @Override
    public void setValue(Object o) {
        if (o instanceof Double) {
            number = ((Double) o);
            repaint();
        }
    }

    @Override
    public void init() {
        number = 0;
        setPreferredSize(new Dimension(36, 36));
    }

    @Override
    public void propertyChanged(Property prprt) {
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(textColor.getValue());
        g.setFont(new Font("Consolas", Font.BOLD, textSize.getValue()));
        
        // if no rounding needed, just dump out value
        if (!round.getValue()) {
            g.drawString(String.valueOf(number), 0, this.getHeight());
            return;
        }
        
        int roundExp = this.roundValue.getValue().intValue();
        
        if (roundExp == 0) {
            // nearest 10^0=1, aka nearest integer
            g.drawString(this.getFieldName() + ": " + String.valueOf(Math.round(number)), 0, this.getHeight());
        } else if (roundExp > 0) {
            // positive n -> trailing zeroes
            g.drawString(this.getFieldName() + ": " +  String.valueOf(decRound(number, roundExp)), 0, this.getHeight());
        } else {
            // negative n -> digits after decimal
            g.drawString(this.getFieldName() + ": " +  String.valueOf(decRound(number, roundExp)), 0, this.getHeight());
        }
    }
    
    // rounds value to nDigits digits after the decimal
    private static double decRound(double value, int nDigits) {
        double x = Math.pow(10, nDigits);
        return Math.round(value * x) / x;
    }
    
}
