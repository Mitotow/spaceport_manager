package fr.antoma.spaceport_manager.model;

public enum BaggageType {
    CABIN("Cabine", 12),
    HOLD("Soute", 32),
    CRYOGENIC("Cryogénique", 80),
    HAZARDOUS("Matière dangereuse", 25);

    private final String label;
    private final double maxWeightKg;

    BaggageType(String label, double maxWeightKg) {
        this.label = label;
        this.maxWeightKg = maxWeightKg;
    }

    public double maxWeightKg() {
        return maxWeightKg;
    }

    @Override
    public String toString() {
        return label;
    }
}
