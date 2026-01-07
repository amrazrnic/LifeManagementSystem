package lifemanagement.ui;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lifemanagement.MongoDBConnection;
import lifemanagement.SesijaKorisnika;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class SpavanjeTrackerForm {
    private JPanel glavniPanel;
    private JTextField poljeDatum;
    private JTextField poljeSati;
    private JButton dugmeDodaj;
    private JButton dugmeObrisi;
    private JButton dugmeProsjek;
    private JButton dugmeNazad;
    private JTable tabelaSpavanje;
    private JLabel labelPoruka;
    private JLabel labelProsjek;

    private final AppProzor prozor;

    public SpavanjeTrackerForm(AppProzor prozor) {
        this.prozor = prozor;

        dugmeNazad.addActionListener(e ->
                prozor.prikaziPanel(new TrackeriMeniForm(prozor).getGlavniPanel())
        );

        dugmeDodaj.addActionListener(e -> dodajUnos());
        dugmeObrisi.addActionListener(e -> obrisiUnos());
        dugmeProsjek.addActionListener(e -> izracunajProsjek());

        ucitajUTabelu();
        izracunajProsjek();
    }

    private void dodajUnos() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelPoruka.setText("Nisi ulogovan/a.");
            return;
        }

        String datum = poljeDatum.getText().trim();
        String satiTekst = poljeSati.getText().trim();

        if (datum.isEmpty() || satiTekst.isEmpty()) {
            labelPoruka.setText("Unesi datum i broj sati.");
            return;
        }

        double sati;
        try {
            sati = Double.parseDouble(satiTekst);
        } catch (NumberFormatException ex) {
            labelPoruka.setText("Sati moraju biti broj (npr. 7.5).");
            return;
        }

        if (sati < 0 || sati > 24) {
            labelPoruka.setText("Sati moraju biti između 0 i 24.");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("spavanje_unosi");

            Document doc = new Document("login", login)
                    .append("datum", datum)
                    .append("sati", sati);

            kolekcija.insertOne(doc);

            poljeDatum.setText("");
            poljeSati.setText("");

            labelPoruka.setText("Unos dodan.");
            ucitajUTabelu();
            izracunajProsjek();

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri upisu u bazu.");
            ex.printStackTrace();
        }
    }

    private void obrisiUnos() {
        int red = tabelaSpavanje.getSelectedRow();
        if (red < 0) {
            labelPoruka.setText("Odaberi unos u tabeli.");
            return;
        }

        String id = tabelaSpavanje.getModel().getValueAt(red, 0).toString();

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("spavanje_unosi");

            kolekcija.deleteOne(new Document("_id", new ObjectId(id)));

            labelPoruka.setText("Unos obrisan.");
            ucitajUTabelu();
            izracunajProsjek();

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri brisanju.");
            ex.printStackTrace();
        }
    }

    private void ucitajUTabelu() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) return;

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("spavanje_unosi");

            ArrayList<Document> lista = kolekcija.find(new Document("login", login))
                    .into(new ArrayList<>());

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Datum");
            model.addColumn("Sati");

            for (Document d : lista) {
                ObjectId oid = d.getObjectId("_id");
                String datum = d.getString("datum");

                Double sati = null;
                Object s = d.get("sati");
                if (s instanceof Number) sati = ((Number) s).doubleValue();

                model.addRow(new Object[]{
                        oid != null ? oid.toHexString() : "",
                        datum != null ? datum : "",
                        sati != null ? sati : 0
                });
            }

            tabelaSpavanje.setModel(model);
            tabelaSpavanje.getColumnModel().getColumn(0).setMinWidth(0);
            tabelaSpavanje.getColumnModel().getColumn(0).setMaxWidth(0);
            tabelaSpavanje.getColumnModel().getColumn(0).setPreferredWidth(0);

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri učitavanju.");
            ex.printStackTrace();
        }
    }

    private void izracunajProsjek() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelProsjek.setText("Prosjek: -");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("spavanje_unosi");

            ArrayList<Document> lista = kolekcija.find(new Document("login", login))
                    .into(new ArrayList<>());

            if (lista.isEmpty()) {
                labelProsjek.setText("Prosjek: nema unosa");
                return;
            }

            double suma = 0;
            int broj = 0;

            for (Document d : lista) {
                Object s = d.get("sati");
                if (s instanceof Number) {
                    suma += ((Number) s).doubleValue();
                    broj++;
                }
            }

            if (broj == 0) {
                labelProsjek.setText("Prosjek: nema validnih unosa");
                return;
            }

            double prosjek = suma / broj;
            labelProsjek.setText("Prosjek: " + String.format("%.2f", prosjek) + " h");

        } catch (Exception ex) {
            labelProsjek.setText("Prosjek: greška");
            ex.printStackTrace();
        }
    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}
