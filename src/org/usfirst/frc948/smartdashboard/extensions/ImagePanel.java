package org.usfirst.frc948.smartdashboard.extensions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.EventObject;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 * SmartDashboard widget for displaying a BufferedImage object.
 * 
 * This widget can also display the RGB values of the pixel being hovered over.
 * 
 * Lastly, the user can click-and-drag across the image to scan the image for an 
 * RGB range, which is communicated via a ColorRangeEvent to all registered 
 * ImagePanelListener objects.
 */
public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener {

    private BufferedImage image = null;
    private Point anchorPoint = null;
    private Point mousePosition = null;
    private final boolean showColorOnHover;
    private final LinkedList<ImagePanelListener> listeners = new LinkedList();

    public ImagePanel(int width, int height) {
        this(width, height, false);
    }

    public ImagePanel(int width, int height, boolean showColorOnHover) {
        this.setPreferredSize(new Dimension(width, height));
        this.setMaximumSize(new Dimension(width, height));
        this.showColorOnHover = showColorOnHover;
        if (showColorOnHover) {
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        }
    }

    public void addImagePanelListener(ImagePanelListener listener) {
        listeners.add(listener);
    }

    public void removeImagePanelListener(ImagePanelListener listener) {
        listeners.remove(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image == null) {
            // if there's no image, draw a blue box and display the text "NO IMAGE"
            g.setColor(Color.cyan);
            g.fillRect(0, 0, this.getPreferredSize().width, this.getPreferredSize().height);
            g.setColor(Color.black);
            g.setFont(g.getFont().deriveFont(g.getFont().getSize() * 3.0f));
            g.drawString("NO IMAGE", 20, 100);
        } else {
            // draw the image
            g.drawImage(image, 0, 0, null);
        }
        
        if (mousePosition != null) {
            if (anchorPoint != null) {
                // if user is doing click-and-drag, draw box around selected area
                g.setColor(Color.red);
                g.drawRect(
                        Math.min(anchorPoint.x, mousePosition.x),
                        Math.min(anchorPoint.y, mousePosition.y),
                        Math.abs(mousePosition.x - anchorPoint.x),
                        Math.abs(mousePosition.y - anchorPoint.y));
            } else if (showColorOnHover && image != null) {
                // if user not going click-and-drag and RGB display is enabled,
                //   display RGB of hovered pixel
                ((Graphics2D) g).setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(Color.red);
                g.setFont(new Font("Courier New", Font.BOLD, 20));
                Color c = new Color(image.getRGB(mousePosition.x, mousePosition.y));
                g.drawString(String.format("%3d %3d %3d", c.getRed(), c.getGreen(), c.getBlue()), 10, 30);
                
                // draw small cross around cursor position
                g.drawLine(mousePosition.x - 4, mousePosition.y, mousePosition.x - 1, mousePosition.y);
                g.drawLine(mousePosition.x + 1, mousePosition.y, mousePosition.x + 4, mousePosition.y);
                g.drawLine(mousePosition.x, mousePosition.y - 4, mousePosition.x, mousePosition.y - 1);
                g.drawLine(mousePosition.x, mousePosition.y + 1, mousePosition.x, mousePosition.y + 4);
            }
        }
    }

    public void setImage(BufferedImage image) {
        // resize to accomodate new image
        if (image != null && (image.getWidth() != this.getWidth() || image.getHeight() != this.getHeight())) {
            this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        this.image = image;

        this.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.anchorPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.image != null && this.anchorPoint != null) {
            
            // once user releases mouse after click-and-drag, scan selected 
            //   area, noting min/max of each R, G, and B
            
            int[] sample = image.getRGB(
                    Math.min(anchorPoint.x, mousePosition.x),
                    Math.min(anchorPoint.y, mousePosition.y),
                    Math.abs(mousePosition.x - anchorPoint.x),
                    Math.abs(mousePosition.y - anchorPoint.y),
                    null,
                    0,
                    Math.abs(mousePosition.x - anchorPoint.x));

            int R_min = 255, G_min = 255, B_min = 255;
            int R_max = 0, G_max = 0, B_max = 0;

            for (int pixel : sample) {
                int R = (pixel >> 16) & 0xFF;
                int G = (pixel >> 8) & 0xFF;
                int B = pixel & 0xFF;

                if (R < R_min) R_min = R; 
                if (R > R_max) R_max = R; 

                if (G < G_min) G_min = G; 
                if (G > G_max) G_max = G; 

                if (B < B_min) B_min = B; 
                if (B > B_max) B_max = B; 
            }

            ColorRangeEvent colorRangeEvent = new ColorRangeEvent(this, new ColorRange(R_min, G_min, B_min, R_max, G_max, B_max));
            for (ImagePanelListener listener : listeners)
                listener.colorRangeSelected(colorRangeEvent);
        }

        this.anchorPoint = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.anchorPoint = null;
        this.mousePosition = null;
        this.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mousePosition = e.getPoint();
        this.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        this.mousePosition = e.getPoint();
        this.repaint();
    }

    public static interface ImagePanelListener {
        public void colorRangeSelected(ColorRangeEvent e);
    }

    public static class ColorRangeEvent extends EventObject {
        public final ColorRange range;
        public ColorRangeEvent(Object source, ColorRange range) {
            super(source);
            this.range = range;
        }
    }
}
