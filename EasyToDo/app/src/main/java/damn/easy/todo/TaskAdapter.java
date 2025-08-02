package damn.easy.todo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskHolder> {
    private OnItemClickListener listener;

    public TaskAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.isCompleted() == newItem.isCompleted() &&
                    oldItem.getPriority() == newItem.getPriority() &&
                    (oldItem.getDueDate() != null ? oldItem.getDueDate().equals(newItem.getDueDate()) : newItem.getDueDate() == null);
        }
    };

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item, parent, false);
        return new TaskHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        Task currentTask = getItem(position);
        holder.textViewTitle.setText(currentTask.getTitle());
        // Remove previous listeners to avoid recursive calls
        holder.checkBoxCompleted.setOnCheckedChangeListener(null);
        holder.checkBoxCompleted.setChecked(currentTask.isCompleted());

        if (currentTask.isCompleted()) {
            holder.textViewTitle.setPaintFlags(holder.textViewTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textViewTitle.setPaintFlags(holder.textViewTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        if (currentTask.getDueDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.textViewDueDate.setText("Due: " + sdf.format(currentTask.getDueDate()));
            holder.textViewDueDate.setVisibility(View.VISIBLE);
        } else {
            holder.textViewDueDate.setVisibility(View.GONE);
        }

        switch (currentTask.getPriority()) {
            case 1:
                holder.priorityIndicator.setBackgroundColor(Color.GREEN);
                break;
            case 2:
                holder.priorityIndicator.setBackgroundColor(Color.YELLOW);
                break;
            case 3:
                holder.priorityIndicator.setBackgroundColor(Color.RED);
                break;
        }

        holder.checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int pos = holder.getAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_POSITION) {
                Task task = getItem(pos);
                task.setCompleted(isChecked);
                listener.onCheckChange(task);

                // Force update the item to trigger DiffUtil
                notifyItemChanged(pos);
            }
        });
    }

    public Task getTaskAt(int position) {
        return getItem(position);
    }

    class TaskHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewDueDate;
        private final CheckBox checkBoxCompleted;
        private final View priorityIndicator;

        public TaskHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDueDate = itemView.findViewById(R.id.text_view_due_date);
            checkBoxCompleted = itemView.findViewById(R.id.checkbox_completed);
            priorityIndicator = itemView.findViewById(R.id.priority_indicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });

            checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    Task task = getItem(position);
                    task.setCompleted(isChecked);
                    listener.onCheckChange(task);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Task task);
        void onCheckChange(Task task);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
