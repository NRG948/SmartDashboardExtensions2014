package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.robot.Robot;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * 
 * SmartDashboard widget for displaying position and heading of robot.
 * 
 */
public class RobotPosition extends StaticWidget implements ITableListener, MouseListener {
    
    public static final String NAME = "Robot Position";
    
    private static final int PREFERRED_WIDGET_HEIGHT = 500;
    
    private static final double FIELD_WIDTH  = 24.67;  // in feet
    private static final double FIELD_LENGTH = 54.00;
    
    private static final double ROBOT_WIDTH = 26.0 / 12.0;
    private static final double ROBOT_LENGTH = 30.0 / 12.0;
    
    private static final double TRUSS_WIDTH = 1.0;
    private static final double LOW_GOAL_SIDE = 2.0 + 8.5 / 12.0;  // 2' 8.5"
    private static final double HIGH_GOAL_WIDTH = 11.5;
    
    private final FieldPanel fieldPane = this.new FieldPanel();
    private final JToggleButton toggleDrive = new JToggleButton("Click-Drive");
    
    private ITable table;
    
    private double robotX = FIELD_WIDTH / 2.0;
    private double robotY = FIELD_LENGTH / 2.0;
    private double robotHeading = 0;
    
    private Point destination = null;
    
    public RobotPosition() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        this.add(fieldPane);
        this.add(toggleDrive);
        
        fieldPane.addMouseListener(this);
    }
    
    @Override
    public void init() {
        table = Robot.getTable("RobotPosition");
        table.addTableListener(this);
    }

    @Override
    public void propertyChanged(Property prprt) {
        
    }
    
    @Override
    public void valueChanged(ITable source, String key, Object value, boolean isNew) {
        if (source == table) {
            switch (key) {
                case "robotX":
                    robotX = table.getNumber("robotX");
                    fieldPane.repaint();
                    break;
                case "robotY":
                    robotY = table.getNumber("robotY");
                    fieldPane.repaint();
                    break;
                case "robotHeading":
                    robotHeading = table.getNumber("robotHeading");
                    fieldPane.repaint();
                    break;
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (toggleDrive.isSelected()) {
            destination = e.getPoint();
            
            double destX = (double) destination.x * FIELD_WIDTH / (double) fieldPane.getWidth();
            double destY = (double) (fieldPane.getHeight() - destination.y) * FIELD_LENGTH / (double) fieldPane.getHeight();

            double dX = destX - robotX;
            double dY = destY - robotY;

            double distance = Math.sqrt(dX * dX + dY * dY);
            double theta = 90.0 - Math.atan2(dY, dX) * 180.0 / Math.PI - robotHeading;

            while (theta > 180.0) theta -= 360.0;
            while (theta < -180.0) theta += 360.0;
            
            StringBuilder sb = new StringBuilder();
            if (theta != 0) {
                sb.append("T<");
                sb.append(String.format("%.3f", theta));
                sb.append('>');
            }
            if (distance != 0) {
                sb.append("D<");
                sb.append(String.format("%.3f", distance));
                sb.append('>');
            }
            
            if (sb.length() > 0) {
                table.putString("path", sb.toString());
            }
        } else destination = null;
        
        fieldPane.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
    
    private class FieldPanel extends JPanel {
        
        public FieldPanel() {
            setPreferredSize(new Dimension((int) (FIELD_WIDTH * PREFERRED_WIDGET_HEIGHT / FIELD_LENGTH), (int) (FIELD_LENGTH * PREFERRED_WIDGET_HEIGHT / FIELD_LENGTH)));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // fill with white
            g.setColor(Color.white);
            g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);

            // draw black border
            g.setColor(Color.black);
            g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);

            // draw truss
            g.drawRect(0, (int) ((FIELD_LENGTH - TRUSS_WIDTH) / 2.0 / FIELD_LENGTH * this.getHeight()), this.getWidth() - 1, (int) (TRUSS_WIDTH / FIELD_LENGTH * this.getHeight()));

            // draw high goals
            g.setColor(new Color(34, 177, 76, 200));
            int goalWidth = (int) (HIGH_GOAL_WIDTH * this.getWidth() / FIELD_WIDTH);
            int goalHeight = (int) (1.0 / FIELD_LENGTH * this.getHeight());
            g.fillRect((int) ((FIELD_WIDTH / 2.0 - 0.5 - HIGH_GOAL_WIDTH) * this.getWidth() / FIELD_WIDTH), 0, goalWidth, goalHeight);
            g.fillRect((int) ((FIELD_WIDTH / 2.0 + 0.5) * this.getWidth() / FIELD_WIDTH), 0, goalWidth, goalHeight);
            g.fillRect((int) ((FIELD_WIDTH / 2.0 - 0.5 - HIGH_GOAL_WIDTH) * this.getWidth() / FIELD_WIDTH), this.getHeight() - goalHeight, goalWidth, goalHeight);
            g.fillRect((int) ((FIELD_WIDTH / 2.0 + 0.5) * this.getWidth() / FIELD_WIDTH), this.getHeight() - goalHeight, goalWidth, goalHeight);
            
            // draw low goals
            g.setColor(Color.black);
            goalWidth = (int) (LOW_GOAL_SIDE * this.getWidth() / FIELD_WIDTH);
            goalHeight = (int) (LOW_GOAL_SIDE * this.getHeight() / FIELD_LENGTH);
            g.drawRect(0,                           0,                             goalWidth, goalHeight);
            g.drawRect(this.getWidth() - goalWidth, 0,                             goalWidth, goalHeight);
            g.drawRect(0,                           this.getHeight() - goalHeight, goalWidth, goalHeight);
            g.drawRect(this.getWidth() - goalWidth, this.getHeight() - goalHeight, goalWidth, goalHeight);

            // separate sections of the field
            g.drawLine(0, this.getHeight() / 3, this.getWidth() - 1, this.getHeight() / 3);
            g.drawLine(0, this.getHeight() * 2 / 3, this.getWidth() - 1, this.getHeight() * 2 / 3);
            
            if (destination != null) {
                g.setColor(Color.blue);
                g.drawOval(destination.x - 6, destination.y - 6, 13, 13);
                g.fillOval(destination.x - 3, destination.y - 3, 7, 7);
            }

            try {
                // convert to pixels
                double x = robotX / FIELD_WIDTH * this.getWidth();
                double y = this.getHeight() - robotY / FIELD_LENGTH * this.getHeight();

                // calculate width and height (length) of robot
                double width = ROBOT_WIDTH * this.getWidth() / FIELD_WIDTH;
                double height = ROBOT_LENGTH * this.getHeight() / FIELD_LENGTH;

                // convert heading to angle in radians
                double robotAngle = (90.0 - robotHeading) * Math.PI / 180.0;

                // draw rectangle for robot
                double diagonalLength = Math.sqrt(width * width + height * height);
                double halfDiagonal = diagonalLength / 2.0;
                double smallAngle = Math.atan2(width / 2.0, height / 2.0);
                double bigAngle = Math.PI / 2.0 - smallAngle;
                double x1 = x + halfDiagonal * Math.cos(robotAngle - smallAngle);
                double x2 = x + halfDiagonal * Math.cos(robotAngle + smallAngle);
                double x3 = x + halfDiagonal * Math.cos(robotAngle + smallAngle + 2 * bigAngle);
                double x4 = x + halfDiagonal * Math.cos(robotAngle + 3 * smallAngle + 2 * bigAngle);
                double y1 = y - halfDiagonal * Math.sin(robotAngle - smallAngle);
                double y2 = y - halfDiagonal * Math.sin(robotAngle + smallAngle);
                double y3 = y - halfDiagonal * Math.sin(robotAngle + smallAngle + 2 * bigAngle);
                double y4 = y - halfDiagonal * Math.sin(robotAngle + 3 * smallAngle + 2 * bigAngle);

                g.setColor(Color.black);
                g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
                g.drawLine((int) x2, (int) y2, (int) x3, (int) y3);
                g.drawLine((int) x3, (int) y3, (int) x4, (int) y4);
                g.drawLine((int) x4, (int) y4, (int) x1, (int) y1);

                double lineLength = Math.sqrt(fieldPane.getWidth() * fieldPane.getWidth() + fieldPane.getHeight() * fieldPane.getHeight());
                g.setColor(Color.red);
                g.drawLine((int) x, (int) y, (int) (x + lineLength * Math.cos(robotAngle)), (int) (y - lineLength * Math.sin(robotAngle)));
            } catch (Exception e) {
                // print exception on widget
                g.setColor(Color.red);
                g.drawString(e.toString(), 0, 40);
            }
        }
        
    }
    
    public static void main(String[] args) {
        WidgetTester.testStaticWidget(new RobotPosition());
        
        // increment the robot heading by 5 degrees every second (for testing)
        (new Timer()).schedule(new TimerTask() {
            private double heading = 0;
            @Override
            public void run() {
                Robot.getTable("RobotPosition").putNumber("robotHeading", heading += 0.5);
            }
        }, 0, 5);
    }
    
}
