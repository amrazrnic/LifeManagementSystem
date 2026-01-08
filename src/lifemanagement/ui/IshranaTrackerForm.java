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
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Locale;

public class IshranaTrackerForm {
    private JPanel glavniPanel;
    private JTextField poljeDatum;
    private JCheckBox checkZdravo;

    private JButton dugmeDodaj;
    private JButton dugmeObrisi;
    private JButton dugmeNazad;

    private JTable tabelaIshrana;

    private JLabel labelSedmica;
    private JLabel labelPoruka;

    private final AppProzor prozor;

    private static final DateTimeFormatter FORMAT_DATUMA =
            DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public IshranaTrackerForm(AppProzor prozor) {
        this.prozor = prozor;

        labelPoruka.setText("");

        dugmeNazad.addActionListener(e ->
                prozor.prikaziPanel(new TrackeriMeniForm(prozor).getGlavniPanel())
        );

        dugmeDodaj.addActionListener(e -> dodajUnos());
        dugmeObrisi.addActionListener(e -> obrisiUnos());

        ucitajUTabelu();
        azurirajSedmicu();
    }

    private void dodajUnos() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelPoruka.setText("Nisi ulogovan/a.");
            return;
        }

        String datum = poljeDatum.getText().trim();
        boolean zdravo = checkZdravo.isSelected();

        if (datum.isEmpty()) {
            labelPoruka.setText("Unesi datum.");
            return;
        }

        // Format: DD.MM.YYYY.
        if (!datum.matches("\\d{2}\\.\\d{2}\\.\\d{4}\\.")) {
            labelPoruka.setText("Datum mora biti u formatu DD.MM.YYYY.");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("ishrana_dani");

            Document filter = new Document("login", login).append("datum", datum);
            Document novi = new Document("login", login)
                    .append("datum", datum)
                    .append("zdravo", zdravo);

            kolekcija.deleteOne(filter);
            kolekcija.insertOne(novi);

            poljeDatum.setText("");
            checkZdravo.setSelected(false);

            labelPoruka.setText("Unos sačuvan.");
            ucitajUTabelu();
            azurirajSedmicu();

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri upisu u bazu.");
            ex.printStackTrace();
        }
    }

    private void obrisiUnos() {
        int red = tabelaIshrana.getSelectedRow();
        if (red < 0) {
            labelPoruka.setText("Odaberi unos u tabeli.");
            return;
        }

        String id = tabelaIshrana.getModel().getValueAt(red, 0).toString();

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("ishrana_dani");

            kolekcija.deleteOne(new Document("_id", new ObjectId(id)));

            labelPoruka.setText("Unos obrisan.");
            ucitajUTabelu();
            azurirajSedmicu();

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri brisanju unosa.");
            ex.printStackTrace();
        }
    }

    private void ucitajUTabelu() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) return;

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("ishrana_dani");

            ArrayList<Document> lista = kolekcija.find(new Document("login", login))
                    .into(new ArrayList<>());

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Datum");
            model.addColumn("Ishrana");

            for (Document d : lista) {
                model.addRow(new Object[]{
                        d.getObjectId("_id").toHexString(),
                        d.getString("datum"),
                        d.getBoolean("zdravo", false) ? "ZDRAVO" : "NIJE"
                });
            }

            tabelaIshrana.setModel(model);

            // sakrij ID kolonu
            tabelaIshrana.getColumnModel().getColumn(0).setMinWidth(0);
            tabelaIshrana.getColumnModel().getColumn(0).setMaxWidth(0);
            tabelaIshrana.getColumnModel().getColumn(0).setPreferredWidth(0);

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri učitavanju tabele.");
            ex.printStackTrace();
        }
    }

    private void azurirajSedmicu() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelSedmica.setText("Ove sedmice: -/7 zdravo");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija = baza.getCollection("ishrana_dani");

            ArrayList<Document> lista = kolekcija.find(new Document("login", login))
                    .into(new ArrayList<>());

            LocalDate danas = LocalDate.now();
            WeekFields wf = WeekFields.of(Locale.getDefault());
            int sedmica = danas.get(wf.weekOfWeekBasedYear());
            int godina = danas.get(wf.weekBasedYear());

            int zdravoDana = 0;

            for (Document d : lista) {
                String datumTekst = d.getString("datum");
                if (datumTekst == null) continue;

                LocalDate datum;
                try {
                    datum = LocalDate.parse(datumTekst, FORMAT_DATUMA);
                } catch (Exception e) {
                    continue;
                }

                if (datum.get(wf.weekOfWeekBasedYear()) == sedmica
                        && datum.get(wf.weekBasedYear()) == godina
                        && d.getBoolean("zdravo", false)) {
                    zdravoDana++;
                }
            }

            labelSedmica.setText("Ove sedmice: " + zdravoDana + "/7 zdravo");

        } catch (Exception ex) {
            labelSedmica.setText("Ove sedmice: greška");
            ex.printStackTrace();
        }
    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}

