package damn.easy.todo;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "task_table")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private boolean isCompleted;
    private int priority; // 1 for low, 2 for medium, 3 for high
    private Date dueDate;

    public Task(String title, boolean isCompleted, int priority, Date dueDate) {
        this.title = title;
        this.isCompleted = isCompleted;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
}
