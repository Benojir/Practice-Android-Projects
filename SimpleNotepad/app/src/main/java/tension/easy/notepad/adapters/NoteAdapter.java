package tension.easy.notepad.adapters;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import tension.easy.notepad.R;
import tension.easy.notepad.models.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes = new ArrayList<>();
    private final List<Note> selectedNotes = new ArrayList<>();
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private boolean isMultiSelectMode = false;

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.textViewTitle.setText(currentNote.getTitle());
        holder.textViewContent.setText(currentNote.getContent());

        if (selectedNotes.contains(currentNote)) {
            holder.noteLayout.setBackgroundColor(Color.LTGRAY);
            holder.textViewTitle.setTextColor(Color.BLACK);
            holder.textViewContent.setTextColor(Color.BLACK);
        } else {
            holder.noteLayout.setBackgroundColor(Color.TRANSPARENT);
            // if dark mode then set texts color white
            int nightModeFlags = holder.itemView.getContext()
                    .getResources()
                    .getConfiguration()
                    .uiMode & Configuration.UI_MODE_NIGHT_MASK;

            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                holder.textViewTitle.setTextColor(Color.WHITE);
                holder.textViewContent.setTextColor(Color.WHITE);
            } else {
                holder.textViewTitle.setTextColor(Color.BLACK);
                holder.textViewContent.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public List<Note> getSelectedNotes() {
        return selectedNotes;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelection() {
        isMultiSelectMode = false;
        selectedNotes.clear();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectAll() {
        if (selectedNotes.size() < notes.size()) {
            selectedNotes.clear();
            selectedNotes.addAll(notes);
        } else {
            selectedNotes.clear();
        }
        notifyDataSetChanged();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewContent;
        private final LinearLayout noteLayout;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewContent = itemView.findViewById(R.id.text_view_content);
            noteLayout = itemView.findViewById(R.id.note_layout);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    if (isMultiSelectMode) {
                        toggleSelection(notes.get(position));
                    } else {
                        listener.onItemClick(notes.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (longClickListener != null && position != RecyclerView.NO_POSITION) {
                    if (!isMultiSelectMode) {
                        isMultiSelectMode = true;
                        longClickListener.onItemLongClick(notes.get(position));
                    }
                    toggleSelection(notes.get(position));
                    return true;
                }
                return false;
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void toggleSelection(Note note) {
        if (selectedNotes.contains(note)) {
            selectedNotes.remove(note);
        } else {
            selectedNotes.add(note);
        }
        notifyDataSetChanged();
        if (selectedNotes.isEmpty()) {
            isMultiSelectMode = false;
            // You might need a callback here to finish the ActionMode
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }
}