package app;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KMalonas
 */
public class ClassRoomManager {
    private List<Classroom> classrooms;

    public ClassRoomManager() {
        this.classrooms = new ArrayList<>();
    }

    public void addClassRoom(Classroom classroom) {
        this.classrooms.add(classroom);
    }

    public List<Classroom> getClassrooms() {
        return this.classrooms;
    }
}
