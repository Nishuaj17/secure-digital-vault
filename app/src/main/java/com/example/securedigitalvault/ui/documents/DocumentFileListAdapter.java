package com.example.securedigitalvault.ui.documents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securedigitalvault.R;
import com.example.securedigitalvault.data.local.entity.DocumentFileEntity;

import java.util.ArrayList;
import java.util.List;

public class DocumentFileListAdapter extends RecyclerView.Adapter<DocumentFileListAdapter.DocViewHolder> {

    public interface OnDocumentFileActionListener {
        void onRowClick(DocumentFileEntity document);
        void onDeleteClick(DocumentFileEntity document);
    }

    private List<DocumentFileEntity> documentList = new ArrayList<>();
    private final OnDocumentFileActionListener listener;

    public DocumentFileListAdapter(OnDocumentFileActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<DocumentFileEntity> newList) {
        this.documentList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_document_file_row, parent, false);
        return new DocViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocViewHolder holder, int position) {
        DocumentFileEntity item = documentList.get(position);
        String readableSize = formatSize(item.fileSizeBytes);

        holder.fileNameText.setText(item.fileName);
        holder.fileMetaText.setText(item.fileType + " • " + readableSize);

        holder.itemView.setOnClickListener(v -> listener.onRowClick(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }

    static class DocViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameText;
        TextView fileMetaText;
        ImageButton deleteButton;

        DocViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameText = itemView.findViewById(R.id.rowFileName);
            fileMetaText = itemView.findViewById(R.id.rowFileMeta);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}


