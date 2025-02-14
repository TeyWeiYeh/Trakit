package domain;

public class Budget {
    public String id;
    public String name;
    public String start_date;
    public String end_date;
    public double limit;
    public Budget() {

    };
    public Budget(String id, String name, String start_date, String end_date, double limit){
        this.id = id;
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.limit = limit;
    }
    public Budget(double limit, String name, String start_date, String end_date){
        this.name = name;
        this.start_date = start_date;
        this.end_date = end_date;
        this.limit = limit;
    }
    //setter and getters for the budget entity
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStartDate(){return start_date;}
    public void setStartDate(String start_date){this.start_date = start_date;}
    public String getEndDate(){return end_date;}
    public void setEndDate(String end_date){this.end_date = end_date;}
    public double getLimit() { return limit; }
    public void setLimit(double limit){ this.limit = limit;}
}
