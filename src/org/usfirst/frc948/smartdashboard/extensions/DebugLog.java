package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.smartdashboard.robot.Robot;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

public class DebugLog extends StaticWidget implements ITableListener, ActionListener {
    
    public static final String NAME = "Debug Log";
    
    private final JTextArea log;
    private final JScrollPane logScroll;
    private final JToggleButton btnAutoScroll;
    private final JButton btnClear;
    private final JButton btnSave;
    
    private final SimpleDateFormat headingFormat = new SimpleDateFormat("MMMM dd, yyyy - hh:mm aa");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd - kk:mm:ss.SSS");
    private final SimpleDateFormat fileTimestampFormat = new SimpleDateFormat("yyyyMMddkkmmss");
    
    public DebugLog() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        log = new JTextArea();
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        log.setEditable(false);
        log.setVisible(true);
        
        logScroll = new JScrollPane(log);
        logScroll.setPreferredSize(new Dimension(400, 300));
        logScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        logScroll.setVisible(true);
        this.add(logScroll);
        
        btnAutoScroll = new JToggleButton("Auto Scroll");
        btnAutoScroll.setVisible(true);
        btnAutoScroll.setSelected(true);
        
        btnClear = new JButton("Clear");
        btnClear.addActionListener(this);
        
        btnSave = new JButton("Write to File");
        btnSave.addActionListener(this);
        
        JPanel pane = new JPanel();
        pane.add(btnAutoScroll);
        pane.add(btnClear);
        pane.add(btnSave);
        
        this.add(pane);
    }
    
    private void addInitLine() {
        log.append("INITIALIZING --- " + headingFormat.format(new Date()));
    }
    
    @Override
    public void init() {
        Robot.getTable("Debug").addTableListener(this);
        addInitLine();
    }
    
    @Override
    public void propertyChanged(Property prprt) {
        
    }
    
    public static void main(String[] args) {
        WidgetTester.testStaticWidget(new DebugLog());
        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                Robot.getTable("Debug").putString("log", "Hello, world!");
            }
        }, 0, 500);
    }

    @Override
    public void valueChanged(ITable source, String key, Object value, boolean isNew) {
        if ("log".equals(key) && !value.toString().isEmpty()) {
            log(value.toString());
            source.putString(key, "");
        }
    }
    
    private void log(String s) {
        if (s != null && !s.isEmpty()) {
            log.append("\n[" + dateFormat.format(new Date()) + "] " + s);
            if (btnAutoScroll.isSelected()) {
                JScrollBar vertical = logScroll.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnClear) {
            log.setText("");
            addInitLine();
        } else if (e.getSource() == btnSave) {
            File outFile = new File("debug-" + fileTimestampFormat.format(new Date()) + ".log");
            try {
                FileWriter outWriter = new FileWriter(outFile);
                String s = log.getText();
                outWriter.write(s);
                outWriter.append('\n');
                outWriter.flush();
                outWriter.close();
            } catch (IOException ex) {
                log(ex.toString());
            }
        }
    }
    
}
