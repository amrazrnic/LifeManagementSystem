package lifemanagement.ui;

import javax.swing.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lifemanagement.MongoDBConnection;
import org.bson.Document;


public class RegistracijaForm {
    private JPanel glavniPanel;
    private JButton dugmeNazad;
    private JTextField poljeLogin;
    private JPasswordField poljeLozinka;
    private JComboBox comboTema;
    private JButton dugmeRegistruj;
    private JLabel labelPoruka;

    private final AppProzor prozor;

    public RegistracijaForm(AppProzor prozor) {
        this.prozor = prozor;

        dugmeRegistruj.addActionListener(e -> {
            String login = poljeLogin.getText().trim();
            String lozinka = new String(poljeLozinka.getPassword());
            String tema = (String) comboTema.getSelectedItem();

            if (login.isEmpty() || lozinka.isEmpty()) {
                labelPoruka.setText("Popuni sva polja.");
                return;
            }

            try {
                MongoDatabase baza = MongoDBConnection.getDatabase();
                MongoCollection<Document> korisnici = baza.getCollection("korisnici");

                Document postoji = korisnici.find(new Document("login", login)).first();
                if (postoji != null) {
                    labelPoruka.setText("Korisnik već postoji.");
                    return;
                }

                Document noviKorisnik = new Document("login", login)
                        .append("lozinka", lozinka)
                        .append("tema", tema);

                korisnici.insertOne(noviKorisnik);

                labelPoruka.setText("Registracija uspješna!");
                prozor.prikaziPanel(new LoginForm(prozor).getGlavniPanel());

            } catch (Exception ex) {
                labelPoruka.setText("Greška pri radu s bazom.");
                ex.printStackTrace();
            }
        });


        dugmeNazad.addActionListener(e ->
                prozor.prikaziPanel(new PocetniForm(prozor).getGlavniPanel())
        );
    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}
