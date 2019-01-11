package org.usfirst.frc948.smartdashboard.extensions;

import edu.wpi.first.smartdashboard.gui.StaticWidget;
import edu.wpi.first.smartdashboard.properties.Property;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterWidget extends StaticWidget implements ActionListener, ITableListener {
    
    public static final String NAME = "Twitter";
    
        // These credentials are tied directly to the NRG948 Twitter account.
        // Please be careful!
        
    private static final String consumerKey = "avGmLpbBunnAqZpqzrFJlw";
    private static final String secretKey = "lqdRIXc5h2Y14ACPuzNCR2xKhUF713Hj00eRlgK0u4";
    
    private static final String accessKey = "1512480638-G5EGHItCAohMqO7nKVla5im7kLywVwT5I5Um8Ej";
    private static final String accessSecret = "XR1svvSEwCkmFW5mXuatcA8q0KmSgpwPd1Il43AHpEcSx";
    
    private final JTextArea txtTweet = new JTextArea("We just won our match! #victorytweet #nrg948", 3, 38);
    private final JButton btnChangeTweet = new JButton("Change...");
    
    private Twitter twitter = null;
    private AccessToken accessToken = null;
    
    public TwitterWidget() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        txtTweet.setEditable(false);
        txtTweet.setBackground(null);
        txtTweet.setLineWrap(true);
        txtTweet.setWrapStyleWord(true);
        this.add(txtTweet);
        this.add(btnChangeTweet);
        
        this.setResizable(false);
        
        btnChangeTweet.addActionListener(this);
    }
    
    @Override
    public void init() {
        NetworkTable.getTable("Twitter").addTableListener(this);
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(secretKey);

        twitter = new TwitterFactory(cb.build()).getInstance();
        accessToken = new AccessToken(accessKey, accessSecret);
        
        twitter.setOAuthAccessToken(accessToken);
    }

    @Override
    public void propertyChanged(Property property) {
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnChangeTweet) {
            String s = JOptionPane.showInputDialog("Enter a tweet:");
            if (s != null && !s.isEmpty() && s.length() <= 140) {
                txtTweet.setText(s);
            }
        }
    }
    
    private boolean tweet(String tweet) {
        try {
            twitter.updateStatus(tweet);
        } catch (TwitterException ex) {
            ex.printStackTrace(System.out);
            return false;
        }
        
        return true;
    }
    
    public static void main(String[] args) {
        WidgetTester.testStaticWidget(new TwitterWidget());
    }

    @Override
    public void valueChanged(ITable source, String key, Object value, boolean isNew) {
        if ("victory".equals(key) && (Boolean)value) {
            tweet(txtTweet.getText());
            source.putBoolean("victory", false);
        } else if ("tweet".equals(key) && value != null && value instanceof String) {
            String s = (String) value;
            if (!s.isEmpty()) {
                tweet(s);
                source.putString("tweet", "");
            }
        }
    }
    
}
