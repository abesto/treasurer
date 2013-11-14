package net.abesto.treasurer.model;

abstract public class Model {
    private Long id = null;

    public Model() {}

    public Model(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (this.id != null) {
            throw new RuntimeException("ID cannot be changed once it's been set.");
        }
        this.id = id;
    }

    public boolean isIdSet() {
        return id != null;
    }
}
