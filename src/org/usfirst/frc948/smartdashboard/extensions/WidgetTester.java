package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.gui.Widget;
import javax.swing.JFrame;

/**
 * 
 * Test harness for static SmartDashboard widgets. Places a widget onto a
 * standalone JFrame instead of running within SmartDashboard.
 * 
 * ---
 * 
 * e.g. usage:
 * 
 *  public static void main(String[] args) {
 *      StaticWidgetTester.testWidget(new RobotPosition());
 *  }
 * 
 */
public class WidgetTester {
    
    public static void testStaticWidget(StaticWidget widget) {
        widget.init();
        JFrame frame = new JFrame();
        frame.setContentPane(widget);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public static void testWidget(Widget widget) {
        widget.init();
        JFrame frame = new JFrame();
        frame.setContentPane(widget);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
