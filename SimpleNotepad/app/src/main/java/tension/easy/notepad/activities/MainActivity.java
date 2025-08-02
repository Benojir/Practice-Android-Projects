package tension.easy.notepad.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import tension.easy.notepad.database_helpers.NoteViewModel;
import tension.easy.notepad.R;
import tension.easy.notepad.adapters.NoteAdapter;
import tension.easy.notepad.models.Note;

public class MainActivity extends AppCompatActivity {
    private NoteViewModel noteViewModel;
    private NoteAdapter adapter;
    private ActionMode actionMode;
    private LiveData<List<Note>> currentObserver;


    private final ActivityResultLauncher<Intent> noteEditorLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String title = data.getStringExtra(NoteEditorActivity.EXTRA_TITLE);
                    String content = data.getStringExtra(NoteEditorActivity.EXTRA_CONTENT);
                    long timestamp = System.currentTimeMillis();
                    Note note = new Note(title, content, timestamp);

                    int id = data.getIntExtra(NoteEditorActivity.EXTRA_ID, -1);
                    if (id != -1) {
                        note.setId(id);
                        noteViewModel.update(note);
                        Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                    } else {
                        noteViewModel.insert(note);
                        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);
        buttonAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteEditorActivity.class);
            noteEditorLauncher.launch(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        observeNotes(noteViewModel.getAllNotes());

        adapter.setOnItemClickListener(note -> {
            Intent intent = new Intent(MainActivity.this, NoteEditorActivity.class);
            intent.putExtra(NoteEditorActivity.EXTRA_ID, note.getId());
            intent.putExtra(NoteEditorActivity.EXTRA_TITLE, note.getTitle());
            intent.putExtra(NoteEditorActivity.EXTRA_CONTENT, note.getContent());
            noteEditorLauncher.launch(intent);
        });

        adapter.setOnItemLongClickListener(note -> {
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback);
            }
        });
    }

    private void observeNotes(LiveData<List<Note>> notesLiveData) {
        if (currentObserver != null) {
            currentObserver.removeObservers(this);
        }
        currentObserver = notesLiveData;
        currentObserver.observe(this, notes -> adapter.setNotes(notes));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText != null && !newText.trim().isEmpty()) {
                        observeNotes(noteViewModel.searchNotes(newText));
                    } else {
                        observeNotes(noteViewModel.getAllNotes());
                    }
                    return true;
                }
            });
        }

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) { return true; }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                observeNotes(noteViewModel.getAllNotes());
                return true;
            }
        });

        return true;
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_menu, menu);
            mode.setTitle("Select Items");
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            List<Note> selectedNotes = adapter.getSelectedNotes();
            int itemId = item.getItemId();

            if (itemId == R.id.action_delete) {
                noteViewModel.deleteNotes(selectedNotes);
                Toast.makeText(MainActivity.this, "Notes deleted", Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
            } else if (itemId == R.id.action_share) {
                shareNotes(selectedNotes);
                mode.finish();
                return true;
            } else if (itemId == R.id.action_export) {
                exportNotes(selectedNotes);
                mode.finish();
                return true;
            } else if (itemId == R.id.action_select_all) {
                adapter.selectAll();
                mode.setTitle(adapter.getSelectedNotes().size() + " selected");
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            adapter.clearSelection();
        }
    };

    private void shareNotes(List<Note> notes) {
        StringBuilder shareText = new StringBuilder();
        for (Note note : notes) {
            shareText.append(note.getTitle()).append("\n\n").append(note.getContent()).append("\n\n---\n\n");
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "Share notes via"));
    }

    private void exportNotes(List<Note> notes) {
        // NOTE: For API 29+ you should use the Storage Access Framework for better file management.
        // This is a simplified implementation for broad compatibility.
        File exportDir = new File(getExternalFilesDir(null), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        try {
            for (int i = 0; i < notes.size(); i++) {
                Note note = notes.get(i);
                // Sanitize title to create a valid filename
                String fileName = note.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".txt";
                File file = new File(exportDir, fileName);

                FileWriter writer = new FileWriter(file);
                writer.append(note.getTitle()).append("\n\n").append(note.getContent());
                writer.flush();
                writer.close();
            }
            Toast.makeText(this, "Notes exported to " + exportDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error exporting notes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}