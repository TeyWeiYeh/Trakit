package domain;

public class Transaction {
    public String id;
    public double amount;
    public String description;
    public boolean recurring;
    public String categoryId;

    public Transaction(){

    };

    public Transaction (String id, double amount, String description, boolean recurring, String categoryId){
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.recurring = recurring;
        this.categoryId = categoryId;
    }

    public Transaction (double amount, String description, boolean recurring, String categoryId){
        this.amount = amount;
        this.description = description;
        this.recurring = recurring;
        this.categoryId = categoryId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public double getAmount() { return amount; }
    public void setAmount(double amount){ this.amount = amount;}
    public String getDescription(){return description;}
    public void setDescription(String description) { this.description = description;}
    public boolean isRecurring() { return false;}
    public String getCategoryId(){return categoryId;}
    public void setCategoryId(String categoryId){this.categoryId = categoryId;}
}
