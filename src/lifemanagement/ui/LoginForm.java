package lifemanagement.ui;

import javax.swing.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lifemanagement.MongoDBConnection;
import org.bson.Document;
import lifemanagement.SesijaKorisnika;


public class LoginForm {
    private JPanel glavniPanel;
    private JTextField poljeLogin;
    private JPasswordField poljeLozinka;
    private JButton dugmePrijava;
    private JButton dugmeNazad;
    private JLabel labelPoruka;

    private final AppProzor prozor;

    public LoginForm(AppProzor prozor) {
        this.prozor = prozor;

        dugmeNazad.addActionListener(e ->
                prozor.prikaziPanel(new PocetniForm(prozor).getGlavniPanel())
        );

        dugmePrijava.addActionListener(e -> {
            String login = poljeLogin.getText().trim();
            String lozinka = new String(poljeLozinka.getPassword());

            if (login.isEmpty() || lozinka.isEmpty()) {
                labelPoruka.setText("Popuni login i lozinku.");
                return;
            }

            try {
                MongoDatabase baza = MongoDBConnection.getDatabase();
                MongoCollection<Document> korisnici = baza.getCollection("korisnici");

                Document korisnik = korisnici.find(
                        new Document("login", login).append("lozinka", lozinka)
                ).first();

                if (korisnik != null) {
                    String tema = korisnik.getString("tema");
                    if (tema == null || tema.isEmpty()) tema = "plava";

                    SesijaKorisnika.loginUlogovanog = login;
                    SesijaKorisnika.temaUlogovanog = tema;

                    labelPoruka.setText("Uspješna prijava!");

                    GlavniMeniForm meni = new GlavniMeniForm(prozor);
                    prozor.prikaziPanel(meni.getGlavniPanel());

                } else {
                    labelPoruka.setText("Pogrešan login ili lozinka.");
                }

            } catch (Exception ex) {
                labelPoruka.setText("Greška pri radu s bazom.");
                ex.printStackTrace();
            }
        });

    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}
