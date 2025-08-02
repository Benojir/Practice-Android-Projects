package tension.easy.notepad.database_helpers;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

import tension.easy.notepad.models.Note;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM note_table WHERE title LIKE :query OR content LIKE :query ORDER BY timestamp DESC")
    LiveData<List<Note>> searchNotes(String query);

    @Delete
    void deleteNotes(List<Note> notes); // For bulk delete
}