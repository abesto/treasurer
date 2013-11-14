package net.abesto.treasurer.model;

public class Category extends Model {
    private String name;

    public Category(Long id, String name) {
        super(id);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
