package pl.poznan.put;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

class Window{
    JFrame frame;
    JMenuBar menu_bar;
    JMenu menu1;
    JMenu menu2;

    Window(){
        //Creating the Frame
        this.frame = new JFrame("CanPhony");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 200);

        //Creating the MenuBar and adding components
        this.menu_bar = new JMenuBar();
        this.menu1 = new JMenu("File");
        this.menu2 = new JMenu("Help");
        this.menu_bar.add(menu1);
        this.menu_bar.add(menu2);
        JMenuItem m1_item1 = new JMenuItem("Open");
        JMenuItem m1_item2 = new JMenuItem("Save as");
        menu1.add(m1_item1);
        menu1.add(m1_item2);
    }

    void login(){
        //Cleaning frame
        this.frame.getContentPane().removeAll();
        this.frame.repaint();
        
        //Creating the panel for components
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JLabel login_label = new JLabel("Login");
        login_label.setHorizontalAlignment(SwingConstants.RIGHT);
        JTextField login_field = new JTextField(16);
        login_label.setLabelFor(login_field);

        JLabel pass_label = new JLabel("Password");
        JPasswordField pass_field = new JPasswordField(16);
        pass_label.setLabelFor(pass_field);

        JButton display_pass_button = new JButton("Display Password");
        display_pass_button.setLayout((new FlowLayout(FlowLayout.LEFT)));
        display_pass_button.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        String password = new String( pass_field.getPassword());
                        JOptionPane.showMessageDialog(frame,
                                "Password: " + password);
                    }
                });

        JButton login_button = new JButton("Login");

        panel.add(login_label); // Components Added using Flow Layout
        panel.add(login_field);
        panel.add(pass_label);
        panel.add(pass_field);
        panel.add(display_pass_button);
        panel.add(login_button);


        //Adding Components to the frame.
        this.frame.getContentPane().add(BorderLayout.CENTER, panel);
        this.frame.getContentPane().add(BorderLayout.NORTH, menu_bar);
        this.frame.setVisible(true);
    }

    void connection(){

        //Cleaning frame
        this.frame.getContentPane().removeAll();
        this.frame.repaint();

        //Creating the panel for components
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton display_pass_button = new JButton("Connect");
        JButton login_button = new JButton("Disconnect");

        panel.add(display_pass_button);
        panel.add(login_button);

        //Adding Components to the frame.
        this.frame.getContentPane().add(BorderLayout.CENTER, panel);
        this.frame.getContentPane().add(BorderLayout.NORTH, this.menu_bar);
        this.frame.setVisible(true);
    }
}

class GUI {
    public static void main(String args[]) throws InterruptedException {
        Window gui = new Window();
        gui.login();
        TimeUnit.SECONDS.sleep(5);
        gui.connection();
    }
}
