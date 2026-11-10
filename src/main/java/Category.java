// Keep prescription rules with the category instead of adding medication subclasses.
public enum Category {
    OTC("OTC", false),
    PRESCRIPTION("PRESCRIPTION", true),
    CONTROLLED("CONTROLLED", true);

    private final String label;
    private final boolean prescriberRequired;

    Category(String label, boolean prescriberRequired) {
        this.label = label;
        this.prescriberRequired = prescriberRequired;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPrescriberRequired() {
        return prescriberRequired;
    }

    public static Category fromString(String categoryStr) {
        if (categoryStr == null || categoryStr.isBlank()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        String normalized = categoryStr.trim().toUpperCase();
        for (Category c : values()) {
            if (c.name().equals(normalized) || c.label.equalsIgnoreCase(normalized)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown medication category: " + categoryStr);
    }

    @Override
    public String toString() {
        return label;
    }
}
