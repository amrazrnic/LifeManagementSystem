package financeapp;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public class TransactionManager {

    private final MongoCollection<Document> collection;

    public TransactionManager() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        collection = db.getCollection("transactions");

        if (collection == null) {
            throw new IllegalStateException("MongoDB collection 'transactions' is null!");
        }
    }
    public void addTransaction(Transaction t) {
        collection.insertOne(t.toDocument());
    }
    public ArrayList<Transaction> getAllTransactions() {

        ArrayList<Transaction> list = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();

        while (cursor.hasNext()) {

            Document d = cursor.next();
            String type = d.getString("Vrsta");
            String category = d.getString("Kategorija");
            Double amount = d.getDouble("Iznos");
            String description = d.getString("Opis");
            ObjectId oid = d.getObjectId("_id");
            String id = (oid != null) ? oid.toHexString() : null;


            if (type != null && amount != null && description != null) {
                list.add(new Transaction(id, type, category, amount, description));
            }
        }
        return list;
    }
    public double getTotalIncome() {
        double total = 0;
        for (Transaction t : getAllTransactions()) {
            if ("Prihod".equals(t.getType()) || "Income".equalsIgnoreCase(t.getType())) {
                total += t.getAmount();
            }
        }
        return total;
    }
    public double getTotalExpense() {
        double total = 0;
        for (Transaction t : getAllTransactions()) {
            if ("Rashod".equals(t.getType()) || "Expense".equalsIgnoreCase(t.getType())) {
                total += t.getAmount();
            }
        }
        return total;
    }
    public void updateTransaction(Transaction t) {
        collection.updateOne(
                new Document("_id", new ObjectId(t.getId())),
                new Document("$set",
                        new Document("Vrsta", t.getType())
                                .append("Iznos", t.getAmount())
                                .append("Opis", t.getDescription())
                )
        );
    }
    public void deleteTransaction(String id) {
        collection.deleteOne(new Document("_id", new ObjectId(id)));
    }
}
