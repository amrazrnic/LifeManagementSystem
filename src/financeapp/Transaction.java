package financeapp;

import org.bson.Document;

public class Transaction {
    private String type;
    private double amount;
    private String description;
    private String id;
    private String category;


    public Transaction(String id, String type, String category, double amount, String description) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }


    public Transaction(String type, String category, double amount, String description) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }


    public Document toDocument() {
        return new Document("Vrsta", type)
                .append("Kategorija", category)
                .append("Iznos", amount)
                .append("Opis", description);
    }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getId() { return id; }
    public String getCategory() { return category; }



}
