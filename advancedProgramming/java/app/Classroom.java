package app;

/**
 * @author KMalonas
 */
public class Classroom {
    public int population;
    protected int squareMeters;
    private boolean hasHeating;

    public Classroom(int population, int squareMeters, boolean hasHeating) {
        this.population = population;
        this.squareMeters = squareMeters;
        this.hasHeating = hasHeating;
    }

    public Classroom() {
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getSquareMeters() {
        return squareMeters;
    }

    public void setSquareMeters(int squareMeters) {
        this.squareMeters = squareMeters;
    }

    public boolean isHasHeating() {
        return hasHeating;
    }

    public void setHasHeating(boolean hasHeating) {
        this.hasHeating = hasHeating;
    }

    @Override public String toString() {
        return "app.Classroom{" +
                "population=" + population +
                ", squareMeters=" + squareMeters +
                ", hasHeating=" + hasHeating +
                '}';
    }
}
