package domain;

import java.util.UUID;

public class Category {
    public String id;
    public String name;
    public enum Type {
        INCOME,
        EXPENSE
    }

    private Type type;

    public Category(String id, String name, Type type){
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Category(String name, Type type){
        this.name = name;
        this.type = type;
    }

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
    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }
}
