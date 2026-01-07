package financeapp;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import javax.swing.SwingUtilities;


public class FinanceTrackerForm {
    private JPanel mainPanel;
    private JTextField amountField;
    private JTextField descriptionField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> categoryCombo;
    private JButton addButton;
    private JTable transactionTable;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;
    private JLabel text1;
    private JLabel text2;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton exportButton;
    private JLabel odaberiLabel;
    private JLabel katetogijaLabel;
    private JButton dugmeNazad;
    private JLabel naslov;
    private TransactionManager manager;
    private String selectedId = null;


    public FinanceTrackerForm() {
        manager = new TransactionManager();
        typeCombo.removeAllItems();
        typeCombo.addItem("Prihod");
        typeCombo.addItem("Rashod");
        categoryCombo.removeAllItems();

        typeCombo.addActionListener(e -> {
            String selectedType = (String) typeCombo.getSelectedItem();

            categoryCombo.removeAllItems();

            if ("Prihod".equals(selectedType)) {
                categoryCombo.addItem("Plata");
                categoryCombo.addItem("Ostalo");
            }

            if ("Rashod".equals(selectedType)) {
                categoryCombo.addItem("Hrana");
                categoryCombo.addItem("Racuni");
                categoryCombo.addItem("Zabava");
                categoryCombo.addItem("Prijevoz");
                categoryCombo.addItem("Ostalo");
            }
        });


        transactionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = transactionTable.getSelectedRow();
                if (row >= 0) {

                    ArrayList<Transaction> list = manager.getAllTransactions();
                    Transaction t = list.get(row);

                    selectedId = t.getId();

                    typeCombo.setSelectedItem(t.getType());
                    categoryCombo.setSelectedItem(t.getCategory());
                    amountField.setText(String.valueOf(t.getAmount()));
                    descriptionField.setText(t.getDescription());
                }
            }
        });

        exportButton.addActionListener(e -> exportData());
        deleteButton.addActionListener(e -> {
            int row = transactionTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(null, "Morate odabrati transakciju!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Jeste li sigurni da želite izbrisati ovu transakciju?",
                    "Potvrda brisanja",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                ArrayList<Transaction> list = manager.getAllTransactions();
                Transaction selected = list.get(row);

                manager.deleteTransaction(selected.getId());

                loadDataIntoTable();
                updateSummary();

                JOptionPane.showMessageDialog(null, "Transakcija izbrisana!");
            }
        });


        updateButton.addActionListener(e -> {
            if (selectedId == null) {
                JOptionPane.showMessageDialog(null, "Morate odabrati transakciju!");
                return;
            }
            try {
                String type = (String) typeCombo.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                String description = descriptionField.getText();

                if (description.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Opis ne može biti prazan!");
                    return;
                }

                String category = (String) categoryCombo.getSelectedItem();

                Transaction updated = new Transaction(
                        selectedId,
                        type,
                        category,
                        amount,
                        description
                );
                manager.updateTransaction(updated);

                loadDataIntoTable();
                updateSummary();

                amountField.setText("");
                descriptionField.setText("");
                selectedId = null;

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Iznos mora biti broj!");
            }
        });

        loadDataIntoTable();
        updateSummary();
        addButton.addActionListener(e -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                String description = descriptionField.getText();
                if (description.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Opis ne može biti prazan!");
                    return;
                }
                //Transaction t = new Transaction(type, amount, description);
                String category = (String) categoryCombo.getSelectedItem();
                Transaction t = new Transaction(type, category, amount, description);

                manager.addTransaction(t);
                loadDataIntoTable();
                updateSummary();
                amountField.setText("");
                descriptionField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null,
                        "Iznos mora biti broj!");
            }
        });
    }

    private void loadDataIntoTable() {
        ArrayList<Transaction> list = manager.getAllTransactions();
        DefaultTableModel model = new DefaultTableModel();

        model.addColumn("Vrsta");
        model.addColumn("Kategorija");
        model.addColumn("Iznos");
        model.addColumn("Opis");

        for (Transaction t : list) {
            model.addRow(new Object[] {
                    t.getType(),
                    t.getCategory(),
                    t.getAmount(),
                    t.getDescription()
            });
        }

        transactionTable.setModel(model);
    }


    private void updateSummary() {
        double income = manager.getTotalIncome();
        double expense = manager.getTotalExpense();
        double balance = income - expense;
        incomeLabel.setText("Prihod: " + income);
        expenseLabel.setText("Rashod: " + expense);
        balanceLabel.setText("Saldo: " + balance);
    }
    public JPanel getMainPanel() {
        return mainPanel;
    }
    private void exportData() {
        try {
            String filePath = System.getProperty("user.home") + "/Desktop/finansije_izvjestaj.txt";

            double income = manager.getTotalIncome();
            double expense = manager.getTotalExpense();
            double balance = income - expense;

            ArrayList<Transaction> list = manager.getAllTransactions();
            java.util.HashMap<String, Double> categoryTotals = new java.util.HashMap<>();

            categoryTotals.put("Plata", 0.0);
            categoryTotals.put("Hrana", 0.0);
            categoryTotals.put("Racuni", 0.0);
            categoryTotals.put("Zabava", 0.0);
            categoryTotals.put("Prijevoz", 0.0);
            categoryTotals.put("Ostalo", 0.0);

            for (Transaction t : list) {
                if (t.getType().equals("Rashod")) {
                    String category = t.getCategory();
                    double amount = t.getAmount();

                    categoryTotals.put(
                            category,
                            categoryTotals.get(category) + amount
                    );
                }
            }

            java.io.PrintWriter writer = new java.io.PrintWriter(filePath);


            writer.println("Ukupni prihod: " + income);
            writer.println("Ukupni rashod: " + expense);
            writer.println("Stanje: " + balance);
            writer.println();

            writer.println("Rashodi po kategorijama:");
            writer.println("Plata: " + categoryTotals.get("Plata"));
            writer.println("Hrana: " + categoryTotals.get("Hrana"));
            writer.println("Racuni: " + categoryTotals.get("Racuni"));
            writer.println("Zabava: " + categoryTotals.get("Zabava"));
            writer.println("Prijevoz: " + categoryTotals.get("Prijevoz"));
            writer.println("Ostalo: " + categoryTotals.get("Ostalo"));

            writer.close();

            JOptionPane.showMessageDialog(
                    null,
                    "Export uspješan!\nDatoteka: finansije_izvjestaj.txt (Desktop)"
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "Greška pri exportu: " + ex.getMessage()
            );
        }
        //dugmeNazad.addActionListener(e -> {
            // zatvori prozor u kojem se nalazi ovaj panel
            //SwingUtilities.getWindowAncestor(mainPanel).dispose();
        //});



    }




}