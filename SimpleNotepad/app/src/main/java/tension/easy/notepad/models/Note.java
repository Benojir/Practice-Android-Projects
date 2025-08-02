package tension.easy.notepad.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "note_table")
public class Note implements Serializable { // Serializable allows passing objects between activities

    @PrimaryKey(autoGenerate = true)
    private int id;

    private final String title;
    private final String content;
    private final long timestamp;

    public Note(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    // --- Getters and Setters ---
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}