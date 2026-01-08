package lifemanagement.ui;

import javax.swing.*;

public class TrackeriMeniForm {
    private JPanel glavniPanel;
    private JButton dugmeSleep;
    private JButton dugmeVoda;
    private JButton dugmeTrening;
    private JButton dugmeIshrana;
    private JButton dugmeNazad;

    private final AppProzor prozor;

    public TrackeriMeniForm(AppProzor prozor) {
        this.prozor = prozor;




        dugmeNazad.addActionListener(e ->
                prozor.prikaziPanel(new GlavniMeniForm(prozor).getGlavniPanel())
        );

        dugmeSleep.addActionListener(e ->
                prozor.prikaziPanel(new SpavanjeTrackerForm(prozor).getGlavniPanel())
        );
        dugmeVoda.addActionListener(e ->
                prozor.prikaziPanel(new VodaTrackerForm(prozor).getGlavniPanel())
        );
        dugmeTrening.addActionListener(e ->
                prozor.prikaziPanel(new TreningTrackerForm(prozor).getGlavniPanel())
        );
        dugmeIshrana.addActionListener(e ->
                prozor.prikaziPanel(new IshranaTrackerForm(prozor).getGlavniPanel())
        );




    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}

