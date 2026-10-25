package com.example.securedigitalvault.ui.dashboard;

/**
 * CategoryItem
 *
 * A plain data holder representing one category card on the dashboard
 * (Passwords, Secure Notes, etc). This is a UI-layer model only — it
 * does not touch the database directly. Once Room is built in Phase 4,
 * DashboardActivity will populate the "count" field from real DAO
 * queries instead of the static placeholder values used for now.
 */
public class CategoryItem {

    private final String id;
    private final String title;
    private final int iconResId;
    private final int iconBackgroundColorResId;
    private final int itemCount;

    public CategoryItem(String id, String title, int iconResId,
                        int iconBackgroundColorResId, int itemCount) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
        this.iconBackgroundColorResId = iconBackgroundColorResId;
        this.itemCount = itemCount;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getIconBackgroundColorResId() {
        return iconBackgroundColorResId;
    }

    public int getItemCount() {
        return itemCount;
    }
}


