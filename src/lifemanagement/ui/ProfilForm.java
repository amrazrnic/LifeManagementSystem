package lifemanagement.ui;

import javax.swing.*;

public class ProfilForm {
    private JPanel glavniPanel;
    private JButton dugmeNazad;

    private final AppProzor prozor;

    public ProfilForm(AppProzor prozor) {
        this.prozor = prozor;

        dugmeNazad.addActionListener(e ->
                prozor.prikaziPanel(new GlavniMeniForm(prozor).getGlavniPanel())
        );
    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}
