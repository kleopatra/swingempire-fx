/*
 * Created on 19.09.2019
 *
 */
package de.swingempire.fx.swing;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * This example, like all Swing examples, exists in a package:
 * in this case, the "start" package.
 * If you are using an IDE, such as NetBeans, this should work 
 * seamlessly.  If you are compiling and running the examples
 * from the command-line, this may be confusing if you aren't
 * used to using named packages.  In most cases,
 * the quick and dirty solution is to delete or comment out
 * the "package" line from all the source files and the code
 * should work as expected.  For an explanation of how to
 * use the Swing examples as-is from the command line, see
 * http://docs.oracle.com/javase/javatutorials/tutorial/uiswing/start/compile.html#package
 */

/*
 * HelloWorldSwing.java requires no other files. 
 */
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;        

/**
 * quick check: 
 * - a consumed space keyPressed doesn't trigger a button's action
 * - same for registering before/after showing
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public class HelloWorldSwing {
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add the ubiquitous "Hello World" label.
        JButton button = new JButton("Hello World");
        
        frame.getContentPane().add(button);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
        registerHandlers(button);
    }

    /**
     * @param button
     */
    protected static void registerHandlers(JButton button) {
        Action a = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("getting action");
                
            }
            
        };
        
        button.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                
            }

            @Override
            public void keyPressed(KeyEvent e) {
                e.consume();
                
            }

            @Override
            public void keyReleased(KeyEvent e) {
                
            }
            
        });
        button.setAction(a);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
