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

public class TreningTrackerForm {

    private JPanel glavniPanel;
    private JTextField poljeDatum;
    private JCheckBox checkOdradjen;

    private JButton dugmeDodaj;
    private JButton dugmeObrisi;
    private JButton dugmeNazad;

    private JTable tabelaTrening;

    private JLabel labelPoruka;
    private JLabel labelSedmica;

    private final AppProzor prozor;

    private static final DateTimeFormatter FORMAT_DATUMA =
            DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public TreningTrackerForm(AppProzor prozor) {
        this.prozor = prozor;

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
        boolean odradjen = checkOdradjen.isSelected();

        if (datum.isEmpty()) {
            labelPoruka.setText("Unesi datum.");
            return;
        }

        if (!datum.matches("\\d{2}\\.\\d{2}\\.\\d{4}\\.")) {
            labelPoruka.setText("Datum mora biti u formatu DD.MM.YYYY.");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija =
                    baza.getCollection("trening_dani");

            Document filter = new Document("login", login)
                    .append("datum", datum);

            Document novi = new Document("login", login)
                    .append("datum", datum)
                    .append("odradjen", odradjen);

            kolekcija.deleteOne(filter);
            kolekcija.insertOne(novi);

            poljeDatum.setText("");
            checkOdradjen.setSelected(false);

            labelPoruka.setText("Unos sačuvan.");
            ucitajUTabelu();
            azurirajSedmicu();

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri upisu u bazu.");
            ex.printStackTrace();
        }
    }

    private void obrisiUnos() {
        int red = tabelaTrening.getSelectedRow();
        if (red < 0) {
            labelPoruka.setText("Odaberi unos.");
            return;
        }

        String id = tabelaTrening.getModel()
                .getValueAt(red, 0).toString();

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija =
                    baza.getCollection("trening_dani");

            kolekcija.deleteOne(new Document("_id",
                    new ObjectId(id)));

            labelPoruka.setText("Unos obrisan.");
            ucitajUTabelu();
            azurirajSedmicu();

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
            MongoCollection<Document> kolekcija =
                    baza.getCollection("trening_dani");

            ArrayList<Document> lista =
                    kolekcija.find(new Document("login", login))
                            .into(new ArrayList<>());

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("ID");
            model.addColumn("Datum");
            model.addColumn("Trening");

            for (Document d : lista) {
                model.addRow(new Object[]{
                        d.getObjectId("_id").toHexString(),
                        d.getString("datum"),
                        d.getBoolean("odradjen", false)
                                ? "ODRAĐEN"
                                : "NIJE"
                });
            }

            tabelaTrening.setModel(model);

            tabelaTrening.getColumnModel().getColumn(0).setMinWidth(0);
            tabelaTrening.getColumnModel().getColumn(0).setMaxWidth(0);

        } catch (Exception ex) {
            labelPoruka.setText("Greška pri učitavanju.");
            ex.printStackTrace();
        }
    }

    private void azurirajSedmicu() {
        String login = SesijaKorisnika.loginUlogovanog;
        if (login == null) {
            labelSedmica.setText("Sedmica: -");
            return;
        }

        try {
            MongoDatabase baza = MongoDBConnection.getDatabase();
            MongoCollection<Document> kolekcija =
                    baza.getCollection("trening_dani");

            ArrayList<Document> lista =
                    kolekcija.find(new Document("login", login))
                            .into(new ArrayList<>());

            LocalDate danas = LocalDate.now();
            WeekFields wf = WeekFields.of(Locale.getDefault());

            int sedmica = danas.get(wf.weekOfWeekBasedYear());
            int godina = danas.get(wf.weekBasedYear());

            int odradjeno = 0;

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
                        && d.getBoolean("odradjen", false)) {

                    odradjeno++;
                }
            }

            labelSedmica.setText(
                    "Ove sedmice: " + odradjeno + "/7 treninga"
            );


        } catch (Exception ex) {
            labelSedmica.setText("Sedmica: greška");
            ex.printStackTrace();
        }
    }

    public JPanel getGlavniPanel() {
        return glavniPanel;
    }
}
