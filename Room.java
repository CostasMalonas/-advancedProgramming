import java.awt.*;

/**
 * @author KMalonas
 */
public abstract class Room extends House {
    private String name;

    public Room(String material, String name) {
        super(material);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void printName() {
        System.out.println("Name: " + this.name);
    }
}
