package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 
 * Widget to draw path for the robot.
 * Takes a mouse-drawn path and reduces the number of vertices using the 
 * Ramer–Douglas–Peucker algorithm.
 * 
 */
public class RouteMap extends StaticWidget {

    public static final String NAME = "Route Map";
    
    private final ConsolePane consolePane = new ConsolePane();
    private final MapPane mapPane = new MapPane(20);
    
    public RouteMap() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(mapPane);
        this.add(consolePane);
        this.validate();
    }
    
    @Override
    public void init() {
        
    }

    @Override
    public void propertyChanged(Property prprt) { }
    
    private class MapPane extends JPanel implements MouseListener, MouseMotionListener {
        
        private final int scale;
        
        private boolean isDrawing = false;
        
        private LinkedList<Point> path = new LinkedList();
        
        public MapPane(int scale) {
            this.scale = scale;
            this.setPreferredSize(new Dimension(54 * scale, 25 * scale));
            this.setMaximumSize(new Dimension(54 * scale, 25 * scale));
            this.setMinimumSize(new Dimension(54 * scale, 25 * scale));
            this.validate();
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.white);
            g.fillRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            g.setColor(Color.black);
            g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            
            g.setColor(Color.red);
            for (int i = 0; i < path.size() - 1; i++) {
                Point p1 = path.get(i);
                Point p2 = path.get(i + 1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }

            g.setColor(Color.blue);
            List<Point> reduced = reduceWithTolerance(path, 2 * scale);
            for (int i = 0; i < reduced.size() - 1; i++) {
                Point p1 = reduced.get(i);
                Point p2 = reduced.get(i + 1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            
        }

        @Override
        public void mousePressed(MouseEvent e) {
            RouteMap.this.consolePane.println("mousePressed");
            path.clear();
            isDrawing = true;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            RouteMap.this.consolePane.println("mouseReleased");
            isDrawing = false;
            repaint();
            
            RouteMap.this.newMap(reduceWithTolerance(path, 2 * scale));
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            
        }

        @Override
        public void mouseExited(MouseEvent e) {
            RouteMap.this.consolePane.println("mouseExited");
            isDrawing = false;
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            RouteMap.this.consolePane.println("mouseDragged");
            if (isDrawing) {
                path.add(e.getPoint());
                repaint();
                RouteMap.this.consolePane.println(String.valueOf(path.size()) + ' ' + e.getPoint().toString());
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            
        }
        
    }
    
    private void newMap(List<Point> path) {
        for (Point p : path)
            consolePane.print(String.format("(%d, %d)->", p.x, p.y));
        consolePane.println("");
    }
    
    private class ConsolePane extends JPanel {
        private final JTextArea textArea = new JTextArea(10, 100);
        private final JScrollPane logScroll = new JScrollPane(textArea);
        public ConsolePane() {
            textArea.setEditable(false);
            
            logScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            this.add(logScroll);
            this.validate();
        }
        public void print(String s) { textArea.append(s); }
        public void println(String s) { textArea.append('\n' + s); scrollToBottom(); }
        private void scrollToBottom() { textArea.setCaretPosition(textArea.getDocument().getLength()); }
    }
    
    public static void main(String[] args) {
        WidgetTester.testStaticWidget(new RouteMap());
    }

    private static List<Point> reduceWithTolerance(List<Point> shape, double tolerance) {
        int n = shape.size();
        
        // if a shape has 2 or less points it cannot be reduced
        if (tolerance <= 0 || n < 3)
            return shape;

        boolean[] marked = new boolean[n]; //vertex indexes to keep will be marked as "true"
        for (int i = 1; i < n - 1; i++)
            marked[i] = false;
        
        // automatically add the first and last point to the returned shape
        marked[0] = marked[n - 1] = true;

        // the first and last points in the original shape are
        // used as the entry point to the algorithm.
        douglasPeuckerReduction(
                shape, // original shape
                marked, // reduced shape
                tolerance, // tolerance
                0, // index of first point
                n - 1 // index of last point
        );

        // all done, return the reduced shape
        LinkedList<Point> newShape = new LinkedList(); // the new shape to return
        for (int i = 0; i < n; i++)
            if (marked[i])
                newShape.add(shape.get(i));
        
        return newShape;
    }

    private static void douglasPeuckerReduction(List<Point> shape, boolean[] marked,
            double tolerance, int firstIdx, int lastIdx) {
        if (lastIdx <= firstIdx + 1) {
            // overlapping indexes, just return
            return;
        }

        // loop over the points between the first and last points
        // and find the point that is the farthest away
        double maxDistance = 0.0;
        int indexFarthest = 0;

        Point firstPoint = shape.get(firstIdx);
        Point lastPoint = shape.get(lastIdx);

        for (int idx = firstIdx + 1; idx < lastIdx; idx++) {
            Point point = shape.get(idx);

            double distance = orthogonalDistance(point, firstPoint, lastPoint);

            // keep the point with the greatest distance
            if (distance > maxDistance) {
                maxDistance = distance;
                indexFarthest = idx;
            }
        }

        if (maxDistance > tolerance) {
            //The farthest point is outside the tolerance: it is marked and the algorithm continues. 
            marked[indexFarthest] = true;

            // reduce the shape between the starting point to newly found point
            douglasPeuckerReduction(shape, marked, tolerance, firstIdx, indexFarthest);

            // reduce the shape between the newly found point and the finishing point
            douglasPeuckerReduction(shape, marked, tolerance, indexFarthest, lastIdx);
        }
        //else: the farthest point is within the tolerance, the whole segment is discarded.
    }

    private static double orthogonalDistance(Point point, Point lineStart, Point lineEnd) {
        double area = Math.abs(
                (1.0 * lineStart.x * lineEnd.y
                + 1.0 * lineEnd.x * point.y
                + 1.0 * point.x * lineStart.y
                - 1.0 * lineEnd.x * lineStart.y
                - 1.0 * point.x * lineEnd.y
                - 1.0 * lineStart.x * point.y) / 2.0
        );

        double bottom = Math.hypot(
                lineStart.x - lineEnd.x,
                lineStart.y - lineEnd.y
        );

        return (area / bottom * 2.0);
    }
    
}
