/*
 * Created on 15.06.2022
 *
 */
package de.swingempire.fx.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/**
 * https://stackoverflow.com/q/72629396/203657
 */
public class SwingTableEditWithCombo extends JFrame {

    public SwingTableEditWithCombo() {

        String[] columns = {"1", "2", "3"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 1);
        JTable table = new JTable(tableModel);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setRowSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFocusable(false);

        for (int i = 0; i < 3; i++) {
            JComboBox cb = new JComboBox();
            cb.setFocusable(false);
            cb.addItem("a");
            cb.addItem("b");
            cb.addItem("c");
            table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(cb));
        }
        JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(500,500));
        jp.setBackground(Color.red);
        jp.add(new JScrollPane(table));
        jp.add(new JTextField("something to focus"));
        this.add(jp);
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                TableCellEditor editor = table.getCellEditor();
                if (editor != null) {
                    boolean stopped = editor.stopCellEditing();
                    if (!stopped) {
                        editor.cancelCellEditing();
                    }
                    e.consume();
                }
            }
            
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingTableEditWithCombo jt = new SwingTableEditWithCombo();
        jt.setVisible(true);
    }
}