package domain;

public class Transaction {
    public String id;
    public double amount;
    public String description;
    public boolean recurring;
    public String transDate;
    public String categoryId;
    public String budgetId;
    public String userId;
    public String base64Img;

    public Transaction(){

    };

    public Transaction (String id, double amount, String description, String date, boolean recurring, String categoryId, String base64Img){
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.transDate = date;
        this.recurring = recurring;
        this.categoryId = categoryId;
        this.base64Img = base64Img;
    }

    public Transaction ( double amount, String description, String date, boolean recurring, String categoryId, String base64Img){
        this.amount = amount;
        this.description = description;
        this.transDate = date;
        this.recurring = recurring;
        this.categoryId = categoryId;
        this.base64Img = base64Img;
    }

    public Transaction (double amount, String description, boolean recurring, String categoryId){
        this.amount = amount;
        this.description = description;
        this.recurring = recurring;
        this.categoryId = categoryId;
    }

    //setter and getters for the transaction entity
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
    public String getTransDate(){return transDate;}
    public void setTransDate(String transDate){this.transDate = transDate;}
    public String getCategoryId(){return categoryId;}
    public void setCategoryId(String categoryId){this.categoryId = categoryId;}
    public String getBudgetId(){return budgetId;}
    public void setBudgetId(String budgetId){this.budgetId = budgetId;}
    public String getUserId(){return userId;}
    public void setUserId(String userId){this.userId = userId;}
    public String getBase64Img(){return base64Img;}
    public void setBase64Img(String base64Img){this.base64Img = base64Img;}
}
