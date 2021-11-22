/*
 * Created on 22.11.2021
 *
 */
package de.swingempire.fx.swing;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.sun.javafx.application.PlatformImpl;


/**
 * https://stackoverflow.com/q/70063888/203657
 * window is re-scaled on start up of platform
 */
public class SwingSizing {

    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton btn = new JButton();

        btn.addActionListener(e -> {
            System.out.println("before size: " + frame.getSize());
            //This is the culprit
            PlatformImpl.startup(() -> System.out.println("javaFX started"));
        });

        JButton log = new JButton();
        log.addActionListener(e -> {
            System.out.println("after size: " + frame.getSize());
            
        });
        JPanel panel = new JPanel();

        JLabel label = new JLabel("Some character for testing");

        panel.add(btn);
        panel.add(log);
        panel.add(label);

        frame.getContentPane().add(panel);

        // Display the window.
        frame.pack();
        frame.setVisible(true);

    }

}
