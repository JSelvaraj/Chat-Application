import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UsernameDelegate {
    private JTextField usernameTextfield;
    private JPanel panel1;
    private JButton enterButton;
    private JTextPane textPane1;

    ServiceMain model = new ServiceMain();

    public UsernameDelegate() {
        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.setUsername(usernameTextfield.getText());
            }
        });
    }
}
