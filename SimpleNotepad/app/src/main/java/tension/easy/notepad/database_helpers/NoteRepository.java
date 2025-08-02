package tension.easy.notepad.database_helpers;

import android.app.Application;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tension.easy.notepad.models.Note;

public class NoteRepository {
    private final NoteDao noteDao;
    private final LiveData<List<Note>> allNotes;

    // Use a dedicated executor for background tasks
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NoteRepository(Application application) {
        NoteDatabase database = NoteDatabase.getInstance(application);
        noteDao = database.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public void insert(Note note) {
        executor.execute(() -> noteDao.insert(note));
    }

    public void update(Note note) {
        executor.execute(() -> noteDao.update(note));
    }

    public void deleteNotes(List<Note> notes) { // For bulk delete
        executor.execute(() -> noteDao.deleteNotes(notes));
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes("%" + query + "%");
    }
}