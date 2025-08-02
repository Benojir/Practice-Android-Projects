package tension.easy.notepad.database_helpers;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

import tension.easy.notepad.models.Note;

public class NoteViewModel extends AndroidViewModel {
    private final NoteRepository repository;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public void insert(Note note) { repository.insert(note); }
    public void update(Note note) { repository.update(note); }
    public void deleteNotes(List<Note> notes) { repository.deleteNotes(notes); } // Bulk delete
    public LiveData<List<Note>> getAllNotes() { return allNotes; }
    public LiveData<List<Note>> searchNotes(String query) { return repository.searchNotes(query); }
}