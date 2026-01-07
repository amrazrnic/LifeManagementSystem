package lifemanagement.ui;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lifemanagement.MongoDBConnection;
import lifemanagement.SesijaKorisnika;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class VodaTrackerForm {

    private JPanel glavniPanel;
    private JTextField poljeDatum;
    private JTextField poljeMl;

    private JButton dugmeDodaj;
    private JButton dugmeObrisi;
    private JButton dugmeNazad;

    private JTable tabelaVoda;

    private JLabel labelPoruka;
    private JLabel labelDanasUkupno;
    private JLabel labelCilj;

    private static final int CILJ_ML = 2000;

    private final AppProzor prozor;

    public VodaTrackerForm(AppProzor prozor) {
        this.prozor = prozor;

        dugmeNazad.addActionListener(e ->
                prozor.prikaziPanel(new TrackeriMeniForm(prozor).getGlavniPanel())
        );

        dugmeDodaj.addActionListener(e -> dodajUnos());
        dugmeObrisi.addActionListener(e -> obrisiUnos());

        ucitajUTabelu();
        azurirajDanasICilj();
    }

    private void dodajUnos() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelPoruka.setText("Nisi ulogovan/a.");
            return;
        }

        String datum = poljeDatum.getText().trim();
        String mlTekst = poljeMl.getText().trim();

        if (datum.isEmpty() || mlTekst.isEmpty()) {
            labelPoruka.setText("Unesi datum i količinu (ml).");
            return;
        }

        // DD.MM.YYYY.
        if (!datum.matches("\\d{2}\\.\\d{2}\\.\\d{4}\\.")) {
            labelPoruka.setText("Datum mora biti u formatu DD.MM.YYYY.");
            return;
        }

        int ml;
        try {
            ml = Integer.parseInt(mlTekst);
        } catch (NumberFormatException ex) {
            labelPoruka.setText("Količina mora biti broj (npr. 500).");
            return;
        }

        if (ml <= 0 || ml > 10000) {
            labelPoruka.setText("Količina mora biti između 1 i 10000 ml.");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("voda_unosi");

            Document doc = new Document("login", login)
                    .append("datum", datum)
                    .append("ml", ml);

            kolekcija.insertOne(doc);

            poljeDatum.setText("");
            poljeMl.setText("");

            labelPoruka.setText("Unos dodan.");
            ucitajUTabelu();
            azurirajDanasICilj();

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri upisu u bazu.");
            ex.printStackTrace();
        }
    }

    private void obrisiUnos() {
        int red = tabelaVoda.getSelectedRow();
        if (red < 0) {
            labelPoruka.setText("Odaberi unos u tabeli.");
            return;
        }

        String id = tabelaVoda.getModel().getValueAt(red, 0).toString();

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("voda_unosi");

            kolekcija.deleteOne(new Document("_id", new ObjectId(id)));

            labelPoruka.setText("Unos obrisan.");
            ucitajUTabelu();
            azurirajDanasICilj();

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
            MongoCollection<Document> kolekcija = baza.getCollection("voda_unosi");

            ArrayList<Document> lista = kolekcija.find(new Document("login", login))
                    .into(new ArrayList<>());

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Datum");
            model.addColumn("Količina (ml)");
            model.addColumn("Količina (L)");

            for (Document d : lista) {
                ObjectId oid = d.getObjectId("_id");
                String datum = d.getString("datum");

                int ml = 0;
                Object vrijednost = d.get("ml");
                if (vrijednost instanceof Number) {
                    ml = ((Number) vrijednost).intValue();
                }

                double litara = ml / 1000.0;

                model.addRow(new Object[]{
                        oid != null ? oid.toHexString() : "",
                        datum != null ? datum : "",
                        ml,
                        String.format("%.2f", litara)
                });
            }

            tabelaVoda.setModel(model);
            tabelaVoda.getColumnModel().getColumn(0).setMinWidth(0);
            tabelaVoda.getColumnModel().getColumn(0).setMaxWidth(0);
            tabelaVoda.getColumnModel().getColumn(0).setPreferredWidth(0);

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri učitavanju.");
            ex.printStackTrace();
        }
    }

    private void azurirajDanasICilj() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelDanasUkupno.setText("Danas: -");
            labelCilj.setText("Cilj: -");
            return;
        }

        String danas = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("voda_unosi");

            ArrayList<Document> lista = kolekcija.find(
                    new Document("login", login).append("datum", danas)
            ).into(new ArrayList<>());

            int ukupnoMl = 0;
            for (Document d : lista) {
                Object vrijednost = d.get("ml");
                if (vrijednost instanceof Number) {
                    ukupnoMl += ((Number) vrijednost).intValue();
                }
            }

            double litara = ukupnoMl / 1000.0;
            labelDanasUkupno.setText(
                    "Danas (" + danas + "): " + ukupnoMl + " ml (" +
                            String.format("%.2f", litara) + " L)"
            );

            if (ukupnoMl >= CILJ_ML) {
                labelCilj.setText("Cilj: " + CILJ_ML + " ml ✅ ISPUNJEN");
            } else {
                int fali = CILJ_ML - ukupnoMl;
                labelCilj.setText("Cilj: " + CILJ_ML + " ml  fali " + fali + " ml");
            }

        } catch (Exception ex) {
            labelDanasUkupno.setText("Danas: greška");
            labelCilj.setText("Cilj: greška");
            ex.printStackTrace();
        }
    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}
