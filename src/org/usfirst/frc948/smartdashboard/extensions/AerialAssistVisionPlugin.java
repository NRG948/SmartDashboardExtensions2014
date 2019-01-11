package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.DoubleProperty;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.robot.Robot;
import edu.wpi.first.wpijavacv.WPIPolygon;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

public class AerialAssistVisionPlugin extends StaticWidget implements ExtendedCameraPlugin {

    // camera vertical FOV, in degrees
    public final DoubleProperty verticalFovProperty = new DoubleProperty(this, "Vertical FOV", 43.32);
    
    // target is 2'8", or 32"; convert to feet
    private static final double TARGET_HEIGHT_FEET = 32.0 / 12.0;
    
    // 320x240
    private static final double IMAGE_HEIGHT_PXL = 240.0;
    
    private final JLabel hotLabel;
    private final JLabel distanceLabel;
    
    public AerialAssistVisionPlugin() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        hotLabel = new JLabel("Not Hot");
        distanceLabel = new JLabel("Distance");
        this.add(distanceLabel);
        this.add(hotLabel);
    }
    
    @Override
    public void init() {
        ExtendedCamera.addPlugin(this);
        this.revalidate();
    }

    @Override
    public List<ExtendedCamera.FilterSet> getFilterPresets() {
        ColorRange range = new ColorRange(0, 150, 0, 150, 255, 150);
        ExtendedCamera.FilterSet filterSet = new ExtendedCamera.FilterSet("Aerial Assist (Green)", range);
        
        LinkedList<ExtendedCamera.FilterSet> list = new LinkedList();
        list.add(filterSet);
        
        return list;
    }
    
    @Override
    public void newPolygons(List<WPIPolygon> polygons) {
        ITable table = (NetworkTable) Robot.getTable("VisionTable");
        if (table == null) return;
        
        boolean isHot = polygons.size() >= 2;
        table.putBoolean("isHot", isHot);
        hotLabel.setText(isHot ? "HOT" : "Not Hot");
        
        ArrayList<WPIPolygon> candidateVerticalTargets = new ArrayList(polygons.size());
        for (WPIPolygon polygon : polygons)
            // approximately vertical - at least twice as tall as width
            if ((double) polygon.getHeight() / (double) polygon.getWidth() >= 2.0)
                candidateVerticalTargets.add(polygon);
        
        if (candidateVerticalTargets.isEmpty()) {
            // no vertical target found
            table.putBoolean("vertTargetExists", false);
            distanceLabel.setText("No vertical target");
            repaint();
            return;
        }
        
        // sort by size, descending
        Collections.sort(candidateVerticalTargets, new Comparator<WPIPolygon>() {
            @Override
            public int compare(WPIPolygon o1, WPIPolygon o2) {
                return o2.getArea() - o1.getArea();
            }
        });
        
        WPIPolygon verticalTarget = candidateVerticalTargets.get(0);
        double targetHeightPxl = verticalTarget.getHeight() / Math.cos(27.0 * Math.PI / 180.0);
        double imageHeightFeet = (TARGET_HEIGHT_FEET / targetHeightPxl) * IMAGE_HEIGHT_PXL;
        double distance = imageHeightFeet / 2.0 / Math.tan(verticalFovProperty.getValue() / 2.0 * Math.PI / 180.0);
        
        table.putBoolean("vertTargetExists", true);
        table.putNumber("distanceToTarget", distance);
        
        distanceLabel.setText("Distance:" + distance);
        
        repaint();
    }
    
    @Override
    public void propertyChanged(Property prprt) {
    }

    @Override
    public boolean saveNext() {
        ITable table = (NetworkTable) Robot.getTable("VisionTable");
        if (table == null) return false;
        
        if (table.getBoolean("saveNext", false)) {
            table.putBoolean("saveNext", false);
            return true;
        }
        
        return false;
    }
    
}
