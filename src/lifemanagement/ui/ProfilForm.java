package lifemanagement.ui;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lifemanagement.MongoDBConnection;
import lifemanagement.SesijaKorisnika;
import org.bson.Document;

import javax.swing.*;

public class ProfilForm {
    private JPanel glavniPanel;
    private JTextField poljeLogin;
    private JTextField poljeIme;
    private JTextField poljePrezime;
    private JTextField poljeEmail;
    private JButton dugmeSpasi;
    private JButton dugmeNazad;
    private JLabel labelPoruka;
    private JButton dugmeObrisiPodatke;
    private JButton dugmeObrisiRacun;

    private final AppProzor prozor;

    public ProfilForm(AppProzor prozor) {
        this.prozor = prozor;

        // Nazad -> Glavni meni
        dugmeNazad.addActionListener(e ->
                prozor.prikaziPanel(new GlavniMeniForm(prozor).getGlavniPanel())
        );

        dugmeSpasi.addActionListener(e -> spasiPromjene());
        dugmeObrisiPodatke.addActionListener(e -> obrisiPodatke());
        dugmeObrisiRacun.addActionListener(e -> obrisiRacun());



        ucitajProfil();
    }

    private void ucitajProfil() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelPoruka.setText("Nisi ulogovan/a.");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> users = baza.getCollection("korisnici");

            Document korisnik = users.find(new Document("login", login)).first();
            if (korisnik == null) {
                labelPoruka.setText("Korisnik nije pronađen u bazi.");
                return;
            }

            poljeLogin.setText(login);
            poljeIme.setText(korisnik.getString("ime") != null ? korisnik.getString("ime") : "");
            poljePrezime.setText(korisnik.getString("prezime") != null ? korisnik.getString("prezime") : "");
            poljeEmail.setText(korisnik.getString("email") != null ? korisnik.getString("email") : "");

            labelPoruka.setText("");

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri učitavanju profila.");
            ex.printStackTrace();
        }
    }

    private void spasiPromjene() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelPoruka.setText("Nisi ulogovan/a.");
            return;
        }

        String ime = poljeIme.getText().trim();
        String prezime = poljePrezime.getText().trim();
        String email = poljeEmail.getText().trim();

        if (ime.isEmpty() || prezime.isEmpty()) {
            labelPoruka.setText("Ime i prezime ne mogu biti prazni.");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> users = baza.getCollection("korisnici");

            Document filter = new Document("login", login);

            Document update = new Document("$set", new Document()
                    .append("ime", ime)
                    .append("prezime", prezime)
                    .append("email", email)
            );

            users.updateOne(filter, update);
            labelPoruka.setText("Profil sačuvan.");

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri spremanju profila.");
            ex.printStackTrace();
        }
    }
    private void obrisiPodatke() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            JOptionPane.showMessageDialog(null, "Nisi ulogovan/a.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Da li želiš obrisati sačuvane podatke profila?\n(Ime, prezime i email će biti obrisani.)",
                "Potvrda brisanja",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> users = baza.getCollection("korisnici"); // ako je kod tebe drugačije ime, ostavi tvoje

            Document filter = new Document("login", login);

            Document update = new Document("$set", new Document()
                    .append("ime", "")
                    .append("prezime", "")
                    .append("email", "")
            );

            users.updateOne(filter, update);

            // očisti i na formi
            poljeIme.setText("");
            poljePrezime.setText("");
            poljeEmail.setText("");

            JOptionPane.showMessageDialog(null, "Podaci su obrisani.", "Uspjeh", JOptionPane.INFORMATION_MESSAGE);
            labelPoruka.setText("Podaci obrisani.");


        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Greška pri brisanju podataka.", "Greška", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    private void obrisiRacun() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            JOptionPane.showMessageDialog(null, "Nisi ulogovan/a.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                null,
                "DA LI SI SIGURNA?\nBrisanjem računa brišu se i svi tvoji podaci (trackeri).\nOva akcija se ne može vratiti.",
                "Obriši račun",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();

            baza.getCollection("korisnici").deleteOne(new Document("login", login));

            baza.getCollection("voda_unosi").deleteMany(new Document("login", login));
            baza.getCollection("spavanje_unosi").deleteMany(new Document("login", login));
            baza.getCollection("trening_dani").deleteMany(new Document("login", login));
            baza.getCollection("trening_unosi").deleteMany(new Document("login", login));

            SesijaKorisnika.loginUlogovanog = null;

            JOptionPane.showMessageDialog(null, "Račun je obrisan.", "Uspjeh", JOptionPane.INFORMATION_MESSAGE);

            prozor.prikaziPanel(new PocetniForm(prozor).getGlavniPanel());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Greška pri brisanju računa: " + ex.getMessage(),
                    "Greška", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }



    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}

