package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.DashboardPrefs;
import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.IPAddressProperty;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.wpijavacv.WPIBinaryImage;
import edu.wpi.first.wpijavacv.WPICamera;
import edu.wpi.first.wpijavacv.WPIColorImage;
import edu.wpi.first.wpijavacv.WPIContour;
import edu.wpi.first.wpijavacv.WPIGrayscaleImage;
import edu.wpi.first.wpijavacv.WPIImage;
import edu.wpi.first.wpijavacv.WPIPoint;
import edu.wpi.first.wpijavacv.WPIPolygon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.usfirst.frc948.smartdashboard.extensions.ImagePanel.ImagePanelListener;

/**
 * SmartDashboard widget for advanced vision processing on the DS laptop.
 * Core camera functionality based off of 
 */
public class ExtendedCamera extends StaticWidget implements ImagePanelListener, ActionListener {

    // name of widget, as it appears on the SmartDashboard
    public static final String NAME = "Extended Camera";
    
    
    // hold reference to robot camera
    private WPICamera cam;
    
    // thread to fetch image from camera
    private final BGThread bgThread = new BGThread();
    
    // thread to run garbage collection
    private final GCThread gcThread = new GCThread();
    
    // property for IP address of camera (10.XX.YY.11 by default)
    public final IPAddressProperty ipProperty = new IPAddressProperty(
                this,
                "Camera IP Address",
                new int[] {
                    10,
                    DashboardPrefs.getInstance().team.getValue().intValue() / 100,
                    DashboardPrefs.getInstance().team.getValue().intValue() % 100,
                    11
                }
            );
    
    private BufferedImage rawImage = null;
    private BufferedImage processedImage = null;
    
    private final SimpleDateFormat imageFileStampFormat = new SimpleDateFormat("yyyyMMddkkmmss");
    
    // ImagePanel for displaying raw camera feed
    private final ImagePanel rawImagePane = new ImagePanel(320, 240, true);
    
    // ImagePanel for displaying filtered image
    private final ImagePanel processedImagePane = new ImagePanel(320, 240);
    
    private final JPanel settingsPane = new JPanel();
    
    private final JComboBox<FilterSet> presetComboBox = new JComboBox();
    private final JButton applyPresetButton = new JButton("Apply");
    
    private final JSpinner rMinSpinner            = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
    private final JSpinner gMinSpinner            = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
    private final JSpinner bMinSpinner            = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
    private final JSpinner rMaxSpinner            = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
    private final JSpinner gMaxSpinner            = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
    private final JSpinner bMaxSpinner            = new JSpinner(new SpinnerNumberModel(255, 0, 255, 1));
    private final JSpinner erosionSpinner         = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
    private final JSpinner dilationSpinner        = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
    private final JSpinner erosion2Spinner        = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
    private final JSpinner dilation2Spinner       = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
    private final JSpinner percentAccuracySpinner = new JSpinner(new SpinnerNumberModel(100, 0, 100, 0.5));
    private final JSpinner polyMinAreaSpinner     = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
    
    private final JButton saveButton = new JButton("Save Image");
    private boolean saveNext = false;
    
    private static final LinkedList<ExtendedCamera> listeners = new LinkedList();
    private static final LinkedList<ExtendedCameraPlugin> plugins = new LinkedList();
    
    public ExtendedCamera() {
        settingsPane.setLayout(new BoxLayout(settingsPane, BoxLayout.Y_AXIS));

        JPanel pane;
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("Presets:"));
        pane.add(presetComboBox);
        pane.add(applyPresetButton);
        settingsPane.add(pane);
        applyPresetButton.addActionListener(this);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("r"));
        pane.add(rMinSpinner);
        pane.add(new JLabel("to"));
        pane.add(rMaxSpinner);
        settingsPane.add(pane);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("g"));
        pane.add(gMinSpinner);
        pane.add(new JLabel("to"));
        pane.add(gMaxSpinner);
        settingsPane.add(pane);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("b"));
        pane.add(bMinSpinner);
        pane.add(new JLabel("to"));
        pane.add(bMaxSpinner);
        settingsPane.add(pane);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("erosion iterations"));
        pane.add(erosionSpinner);
        settingsPane.add(pane);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("dilation iterations"));
        pane.add(dilationSpinner);
        settingsPane.add(pane);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("2nd erosion iterations"));
        pane.add(erosion2Spinner);
        settingsPane.add(pane);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("2nd dilation iterations"));
        pane.add(dilation2Spinner);
        settingsPane.add(pane);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("% perimeter accuracy"));
        pane.add(percentAccuracySpinner);
        settingsPane.add(pane);
        
        pane = new JPanel();
        pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
        pane.add(new JLabel("minimum polygon area"));
        pane.add(polyMinAreaSpinner);
        settingsPane.add(pane);
        
        settingsPane.add(saveButton);
        saveButton.addActionListener(this);
        
        this.setLayout(new BorderLayout());
        
        this.add(rawImagePane, BorderLayout.LINE_START);
        this.add(settingsPane, BorderLayout.CENTER);
        this.add(processedImagePane, BorderLayout.LINE_END);
        
        rawImagePane.addImagePanelListener(this);
        
        JSpinner[] spinners = {
            rMinSpinner,
            gMinSpinner,
            bMinSpinner,
            rMaxSpinner,
            gMaxSpinner,
            bMaxSpinner,
            erosionSpinner,
            dilationSpinner,
            erosion2Spinner,
            dilation2Spinner,
            percentAccuracySpinner,
            polyMinAreaSpinner
        };
        
        // enables use of mouse scroll to adjust spinners
        for (final JSpinner spinner : spinners) {
            spinner.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                        if (e.getWheelRotation() < 0)
                            spinner.setValue(spinner.getNextValue());
                        if (e.getWheelRotation() > 0)
                            spinner.setValue(spinner.getPreviousValue());
                    }
                }
            });
        }
    }
    
    public static void addPlugin(ExtendedCameraPlugin plugin) {
        plugins.add(plugin);
        
        for (ExtendedCamera listener : listeners)
            listener.newPlugin(plugin);
    }
    
    @Override
    public void init() {
        this.bgThread.start();
        this.gcThread.start();
        revalidate();
        repaint();
        this.setResizable(false);
        
        listeners.add(this);
    }

    @Override
    public void propertyChanged(Property property) {
        if (property == this.ipProperty) {
            if (this.cam != null) {
                this.cam.dispose();
            }
            try {
                this.cam = new WPICamera(this.ipProperty.getSaveValue());
            } catch (Exception e) {
                e.printStackTrace(System.err);
                this.rawImage = null;
                this.processedImage = null;
                revalidate();
                repaint();
            }
        }
    }

    @Override
    public void disconnect() {
        this.bgThread.destroy();
        this.gcThread.destroy();
        if (this.cam != null) {
            this.cam.dispose();
        }
        super.disconnect();
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.rawImagePane.setImage(this.rawImage);
        this.processedImagePane.setImage(this.processedImage);
        
        super.paintComponent(g);
    }

    private void processImage(WPIColorImage rawImage) {
        if (rawImage == null) {
            this.rawImage = null;
            this.processedImage = null;
            if (saveNext) {
                saveNext = false;
            }
            return;
        }
        
        for (ExtendedCameraPlugin listener : plugins) {
            if (listener.saveNext()) saveNext = true;
        }
        
        int imageIndex = 0;
        boolean save = saveNext;
        saveNext = false;
        
        this.rawImage = rawImage.getBufferedImage();
        this.processedImage = new BufferedImage(rawImage.getWidth(), rawImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        
        final String timeStamp = imageFileStampFormat.format(new Date());
        
        if (save) {
            try {
                ImageIO.write(this.rawImage, "png", new File("image" + timeStamp + "-" + imageIndex + ".png"));
                imageIndex++;
            } catch (IOException ex) {
                
            }
        }
        
        WPIGrayscaleImage imageR = rawImage.getRedChannel();
        WPIGrayscaleImage imageG = rawImage.getGreenChannel();
        WPIGrayscaleImage imageB = rawImage.getBlueChannel();
        
        WPIBinaryImage imageRmin = imageR.getThreshold((Integer)rMinSpinner.getValue() - 1);
        WPIBinaryImage imageRmax = imageR.getThresholdInverted((Integer)rMaxSpinner.getValue());
        WPIBinaryImage imageGmin = imageG.getThreshold((Integer)gMinSpinner.getValue() - 1);
        WPIBinaryImage imageGmax = imageG.getThresholdInverted((Integer)gMaxSpinner.getValue());
        WPIBinaryImage imageBmin = imageB.getThreshold((Integer)bMinSpinner.getValue() - 1);
        WPIBinaryImage imageBmax = imageB.getThresholdInverted((Integer)bMaxSpinner.getValue());
        
        WPIBinaryImage imageFiltered = imageRmin.getAnd(imageRmax).getAnd(imageGmin).getAnd(imageGmax).getAnd(imageBmin).getAnd(imageBmax);
        
        if (save) {
            try {
                ImageIO.write(imageFiltered.getBufferedImage(), "png", new File("image" + timeStamp + "-" + imageIndex + ".png"));
                imageIndex++;
            } catch (IOException ex) {
                
            }
        }
        
        int erosions = (Integer)erosionSpinner.getValue();
        if (erosions > 0) {
            imageFiltered.erode(erosions);
            if (save) {
                try {
                    ImageIO.write(imageFiltered.getBufferedImage(), "png", new File("image" + timeStamp + "-" + imageIndex + ".png"));
                    imageIndex++;
                } catch (IOException ex) {

                }
            }
        }

        int dilations = (Integer) dilationSpinner.getValue();
        if (dilations > 0) {
            imageFiltered.dilate(dilations);
            if (save) {
                try {
                    ImageIO.write(imageFiltered.getBufferedImage(), "png", new File("image" + timeStamp + "-" + imageIndex + ".png"));
                    imageIndex++;
                } catch (IOException ex) {

                }
            }
        }

        erosions = (Integer) erosion2Spinner.getValue();
        if (erosions > 0) {
            imageFiltered.erode(erosions);
            if (save) {
                try {
                    ImageIO.write(imageFiltered.getBufferedImage(), "png", new File("image" + timeStamp + "-" + imageIndex + ".png"));
                    imageIndex++;
                } catch (IOException ex) {

                }
            }
        }

        dilations = (Integer) dilation2Spinner.getValue();
        if (dilations > 0) {
            imageFiltered.dilate(dilations);
            if (save) {
                try {
                    ImageIO.write(imageFiltered.getBufferedImage(), "png", new File("image" + timeStamp + "-" + imageIndex + ".png"));
                    imageIndex++;
                } catch (IOException ex) {

                }
            }
        }
        
        Graphics2D processedImageDraw = this.processedImage.createGraphics();
        processedImageDraw.drawImage(imageFiltered.getBufferedImage(), 0, 0, null);
        
        WPIContour[] contours = imageFiltered.findContours();
        LinkedList<WPIPolygon> polygons = new LinkedList();
        for (WPIContour contour : contours) {
            WPIPolygon poly = contour.approxPolygon(100.0 - (Double)percentAccuracySpinner.getValue());
            int area = poly.getArea();
            if (area > (Integer)polyMinAreaSpinner.getValue()) {
                WPIPoint[] points = poly.getPoints();
                int[] x = new int[points.length + 1]; x[points.length] = points[0].getX();
                int[] y = new int[points.length + 1]; y[points.length] = points[0].getY();
                
                double cmx = 0, cmy = 0;
                for (int i = points.length - 1; i >= 0; i--) {
                    x[i] = points[i].getX();
                    y[i] = points[i].getY();
                    cmx += (x[i] + x[i + 1]) * (x[i] * y[i + 1] - x[i + 1] * y[i]);
                    cmy += (y[i] + y[i + 1]) * (x[i] * y[i + 1] - x[i + 1] * y[i]);
                }
                
                cmx = Math.abs(cmx / (6.0 * area));
                cmy = Math.abs(cmy / (6.0 * area));
                
                processedImageDraw.setColor(Color.yellow);
                processedImageDraw.drawRect(poly.getX(), poly.getY(), poly.getWidth(), poly.getHeight());
                
                processedImageDraw.setColor(Color.red);
                processedImageDraw.drawPolygon(x, y, points.length);
                
                processedImageDraw.setColor(Color.magenta);
                processedImageDraw.fillOval((int)Math.round(cmx) - 3, (int)Math.round(cmy) - 3, 7, 7);
                
                polygons.add(poly);
            }
        }
        
        if (save) {
            try {
                ImageIO.write(this.processedImage, "png", new File("image" + timeStamp + "-" + imageIndex + ".png"));
                imageIndex++;
            } catch (IOException ex) {

            }
        }
        
        for (ExtendedCameraPlugin listener : plugins) {
            listener.newPolygons(Collections.unmodifiableList(polygons));
        }
    }

    @Override
    public void colorRangeSelected(ImagePanel.ColorRangeEvent e) {
        setColorRange(e.range);
    }

    public void newPlugin(ExtendedCameraPlugin plugin) {
        for (FilterSet filterSet : plugin.getFilterPresets())
            presetComboBox.addItem(filterSet);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == applyPresetButton) {
            FilterSet filterSet = (FilterSet) presetComboBox.getSelectedItem();
            if (filterSet != null) {
                applyFilterSet(filterSet);
            }
        } else if (e.getSource() == saveButton) {
            saveNext = true;
//            saveButton.setEnabled(false);
        }
    }
    
    private void applyFilterSet(FilterSet filterSet) {
        setColorRange(filterSet.colorRange);
    }
    
    private void setColorRange(ColorRange range) {
        rMinSpinner.setValue(Integer.valueOf(range.R_min));
        rMaxSpinner.setValue(Integer.valueOf(range.R_max));
        
        gMinSpinner.setValue(Integer.valueOf(range.G_min));
        gMaxSpinner.setValue(Integer.valueOf(range.G_max));
        
        bMinSpinner.setValue(Integer.valueOf(range.B_min));
        bMaxSpinner.setValue(Integer.valueOf(range.B_max));
    }

    private class BGThread extends Thread {

        boolean destroyed = false;

        public BGThread() {
            super();
        }

        @Override
        public void run() {
            while (!this.destroyed) {
                if (ExtendedCamera.this.cam == null) {
                    ExtendedCamera.this.cam = new WPICamera(ExtendedCamera.this.ipProperty.getSaveValue());
                }
                try {
                    // attempt to grab image; timeout after 5.0 seconds
                    WPIImage image = ExtendedCamera.this.cam.getNewImage(5.0D);
                    
                    // process the image
                    ExtendedCamera.this.processImage((WPIColorImage) image);
                    
                    // repaint
                    ExtendedCamera.this.repaint();
                    
                    // sleep thread - don't want to hog resources
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException ex) {
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                    ExtendedCamera.this.cam.dispose();
                    ExtendedCamera.this.cam = null;
                    ExtendedCamera.this.rawImage = null;
                    ExtendedCamera.this.processedImage = null;
                    ExtendedCamera.this.repaint();
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }

        @Override
        public void destroy() {
            this.destroyed = true;
        }
    }

    private static class GCThread extends Thread {

        private boolean destroyed = false;

        @Override
        public void run() {
            while (!this.destroyed) {
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException ex) {
                }
                System.gc();
            }
        }

        @Override
        public void destroy() {
            this.destroyed = true;
            interrupt();
        }
    }
    
    public static class FilterSet {
        
        public final String name;
        public final ColorRange colorRange;
        
        public FilterSet(String name, ColorRange colorRange) {
            this.name = name;
            this.colorRange = colorRange;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
    }
    
}
