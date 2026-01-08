package lifemanagement.ui;

import javax.swing.*;
import lifemanagement.SesijaKorisnika;
import lifemanagement.ui.TemaUtil;



public class AppProzor extends JFrame {

    public AppProzor() {

        super("Life Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setResizable(false);


        prikaziPanel(new PocetniForm(this).getGlavniPanel());
    }

    public void prikaziPanel(JPanel panel) {
        TemaUtil.primijeniTemu(panel, SesijaKorisnika.temaUlogovanog);
        setContentPane(panel);
        revalidate();
        repaint();
    }


}

