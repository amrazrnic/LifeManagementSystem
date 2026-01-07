package lifemanagement.ui;
import lifemanagement.SesijaKorisnika;


import javax.swing.*;

public class GlavniMeniForm {
    private JPanel glavniPanel;
    private JButton dugmeFinanceApp;
    private JButton dugmeViewProfil;
    private JButton dugmeTrackeri;
    private JButton dugmeOdjava;

    private final AppProzor prozor;

    public GlavniMeniForm(AppProzor prozor) {
        this.prozor = prozor;

        dugmeOdjava.addActionListener(e -> {
            SesijaKorisnika.odjava();
            prozor.prikaziPanel(new PocetniForm(prozor).getGlavniPanel());
        });


        dugmeFinanceApp.addActionListener(e -> {
            financeapp.FinanceTrackerForm financeForm = new financeapp.FinanceTrackerForm();

            JFrame frame = new JFrame("Finance App");
            frame.setContentPane(financeForm.getMainPanel());
            frame.setSize(900, 600);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setVisible(true);
        });


        dugmeViewProfil.addActionListener(e ->
                prozor.prikaziPanel(new ProfilForm(prozor).getGlavniPanel())
        );

        dugmeTrackeri.addActionListener(e ->
                prozor.prikaziPanel(new TrackeriMeniForm(prozor).getGlavniPanel())
        );
    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}
