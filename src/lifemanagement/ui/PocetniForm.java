package lifemanagement.ui;

import javax.swing.*;

public class PocetniForm {
    private JPanel glavniPanel;
    private JButton dugmeLogin;
    private JButton dugmeRegistracija;

    private final AppProzor prozor;

    public PocetniForm(AppProzor prozor) {
        this.prozor = prozor;

        dugmeLogin.addActionListener(e ->
                prozor.prikaziPanel(new LoginForm(prozor).getGlavniPanel())
        );

        dugmeRegistracija.addActionListener(e ->
                prozor.prikaziPanel(new RegistracijaForm(prozor).getGlavniPanel())
        );
    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}
