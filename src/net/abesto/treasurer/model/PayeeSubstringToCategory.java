package net.abesto.treasurer.model;

public class PayeeSubstringToCategory extends Model {
    private String substring;
    private Long categoryId;

    public PayeeSubstringToCategory(String substring, Long categoryId) {
        this.substring = substring;
        this.categoryId = categoryId;
    }

    public String getSubstring() {
        return substring;
    }

    public void setSubstring(String s) {
        this.substring = s;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
