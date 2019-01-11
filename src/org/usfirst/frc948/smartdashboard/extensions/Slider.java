package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.BooleanProperty;
import edu.wpi.first.smartdashboard.properties.IntegerProperty;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.properties.StringProperty;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Slider extends StaticWidget implements ActionListener, ChangeListener {

    public static final String NAME = "Slider";
    
    public final StringProperty labelProperty = new StringProperty(this, "Label", "Slider");
    
    public final IntegerProperty minProperty = new IntegerProperty(this, "Minimum", 0);
    public final IntegerProperty maxProperty = new IntegerProperty(this, "Maximum", 100);
    
    public final BooleanProperty paintTicksProperty = new BooleanProperty(this, "Paint Ticks", false);
    public final IntegerProperty minorTickProperty = new IntegerProperty(this, "Minor Tick Spacing", 0);
    public final IntegerProperty majorTickProperty = new IntegerProperty(this, "Major Tick Spacing", 0);
    
    public final StringProperty networkTableProperty = new StringProperty(this, "NetworkTable", "SmartOI");
    public final StringProperty keyProperty = new StringProperty(this, "Key", null);
    
    private JLabel label;
    private JSlider slider;
    private JTextField valueField;
    
    @Override
    public void init() {
        label = new JLabel(labelProperty.getValue());
        this.add(label);
        
        slider = new JSlider(JSlider.HORIZONTAL, minProperty.getValue(), maxProperty.getValue(), (minProperty.getValue() + maxProperty.getValue()) / 2);
        slider.setPaintLabels(true);
        slider.addChangeListener(this);
        this.add(slider);
        
        valueField = new JTextField(5);
        valueField.addActionListener(this);
        valueField.setText(String.valueOf(slider.getValue()));
        this.add(valueField);
        
        System.out.println(this.getPreferredSize());
    }
    
    private void putValue() {
        NetworkTable table = NetworkTable.getTable(networkTableProperty.getValue());
        if (table != null && table.isConnected()) {
            try {
                table.putNumber(keyProperty.getValue(), slider.getValue());
            } catch (Exception e) {}
        }
    }

    @Override
    public void propertyChanged(Property p) {
        if (p == labelProperty) {
            label.setText(labelProperty.getValue());
        } else if (p == minProperty) {
            slider.setMinimum(minProperty.getValue());
        } else if (p == maxProperty) {
            slider.setMaximum(maxProperty.getValue());
        } else if (p == paintTicksProperty) {
            slider.setPaintTicks(paintTicksProperty.getValue());
        } else if (p == minorTickProperty) {
            slider.setMinorTickSpacing(minorTickProperty.getValue());
        } else if (p == majorTickProperty) {
            slider.setMajorTickSpacing(majorTickProperty.getValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == valueField) {
            String text = valueField.getText();
            int value;
            try {
                value = Integer.valueOf(text);
            } catch (NumberFormatException ex) {
                valueField.setText(String.valueOf(slider.getValue()));
                return;
            }
            
            if (value >= slider.getMinimum() && value <= slider.getMaximum()) {
                slider.setValue(value);
            } else {
                valueField.setText(String.valueOf(slider.getValue()));
            }
            
            putValue();
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == slider) {
            valueField.setText(String.valueOf(slider.getValue()));
            putValue();
        }
    }
    
    public static void main(String[] args) {
        WidgetTester.testStaticWidget(new Slider());
    }

}
