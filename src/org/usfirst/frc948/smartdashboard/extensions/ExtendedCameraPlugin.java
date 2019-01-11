package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.wpijavacv.WPIPolygon;
import java.util.List;

/**
 * 
 * Interface for plug-ins to the ExtendedCamera widget.
 * 
 */
public interface ExtendedCameraPlugin {
    
    /**
     * This method is invoked when the plugin is added to an instance of the
     * ExtendedCamera widget and should return a list of filter "presets" that
     * the plugin uses. For instance, if the robot has a green ring light, the
     * plugin for that year's game should return a preset with RGB filter for
     * green light.
     * 
     * @return a list of desired filters
     */
    List<ExtendedCamera.FilterSet> getFilterPresets();
    
    /**
     * This method is invoked with every new frame and should return true if
     * the plugin wants the ExtendedCamera widget to save the current frame.
     * This is usually the case if something of interest has just occurred
     * (e.g. the driver has just pressed the "Vision Aiming" button, or the
     * robot code has just "seen" a hot goal) so that the image processing
     * results can be verified manually.
     * 
     * @return true if the next processing sequence should be written to the
     *         hard disk, or false otherwise
     */
    boolean saveNext();
    
    /**
     * This method is invoked approximately ten times every second with the 
     * list of polygons found in the current image. The plug-in should process 
     * the list of polygons and send data to the robot.
     * 
     * @param polygons an unmodifiable list of WPIPolygon objects found in the
     *                 current image
     */
    void newPolygons(List<WPIPolygon> polygons);
         
}
