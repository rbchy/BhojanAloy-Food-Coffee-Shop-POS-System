package com.bhojanaloy.gui;

import com.bhojanaloy.dao.CategoryDAO;
import com.bhojanaloy.dao.MenuItemDAO;
import com.bhojanaloy.model.Category;
import com.bhojanaloy.model.MenuItem;
import com.bhojanaloy.service.NotificationService;
import com.bhojanaloy.util.UITheme;
import com.bhojanaloy.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/** CRUD screen for menu items (products sold in the shop). */
public class MenuManagementPanel extends JPanel {

    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final NotificationService notificationService = new NotificationService();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Category", "Price", "Cost", "Stock", "Reorder Lvl"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable table = new JTable(model);
    private List<MenuItem> currentItems;

    public MenuManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Menu Item Management"), BorderLayout.NORTH);
        UITheme.styleTable(table);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(UITheme.CREAM);
        JButton addBtn = UITheme.primaryButton("Add Item");
        JButton editBtn = UITheme.secondaryButton("Edit Selected");
        JButton delBtn = UITheme.dangerButton("Deactivate Selected");
        JButton refreshBtn = UITheme.secondaryButton("Refresh");

        addBtn.addActionListener(e -> showItemDialog(null));
        editBtn.addActionListener(e -> {
            MenuItem sel = getSelectedItem();
            if (sel != null) showItemDialog(sel);
        });
        delBtn.addActionListener(e -> {
            MenuItem sel = getSelectedItem();
            if (sel != null && JOptionPane.showConfirmDialog(this, "Deactivate " + sel.getItemName() + "?",
                    "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                menuItemDAO.deleteItem(sel.getItemId());
                refresh();
            }
        });
        refreshBtn.addActionListener(e -> refresh());

        bar.add(addBtn);
        bar.add(editBtn);
        bar.add(delBtn);
        bar.add(refreshBtn);
        return bar;
    }

    private MenuItem getSelectedItem() {
        int row = table.getSelectedRow();
        if (row < 0 || currentItems == null || row >= currentItems.size()) return null;
        return currentItems.get(row);
    }

    private void refresh() {
        currentItems = menuItemDAO.getAll();
        model.setRowCount(0);
        for (MenuItem m : currentItems) {
            model.addRow(new Object[]{m.getItemId(), m.getItemName(), m.getCategoryName(),
                    m.getPrice(), m.getCostPrice(), m.getStockQty(), m.getReorderLevel()});
        }
    }

    private void showItemDialog(MenuItem existing) {
        List<Category> categories = categoryDAO.getAllActive();
        JComboBox<Category> categoryBox = new JComboBox<>(categories.toArray(new Category[0]));
        JTextField nameField = new JTextField(existing != null ? existing.getItemName() : "");
        JTextField descField = new JTextField(existing != null ? existing.getDescription() : "");
        JTextField priceField = new JTextField(existing != null ? existing.getPrice().toString() : "0.00");
        JTextField costField = new JTextField(existing != null ? existing.getCostPrice().toString() : "0.00");
        JTextField stockField = new JTextField(existing != null ? String.valueOf(existing.getStockQty()) : "0");
        JTextField reorderField = new JTextField(existing != null ? String.valueOf(existing.getReorderLevel()) : "10");
        JTextField imagePathField = new JTextField(existing != null && existing.getImagePath() != null ? existing.getImagePath() : "");
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser(new java.io.File("images"));
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imagePathField.setText(chooser.getSelectedFile().getPath());
            }
        });
        JPanel imageRow = new JPanel(new BorderLayout(4, 0));
        imageRow.add(imagePathField, BorderLayout.CENTER);
        imageRow.add(browseBtn, BorderLayout.EAST);

        if (existing != null) {
            for (Category c : categories) if (c.getCategoryId() == existing.getCategoryId()) categoryBox.setSelectedItem(c);
        }

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Category:")); formPanel.add(categoryBox);
        formPanel.add(new JLabel("Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Description:")); formPanel.add(descField);
        formPanel.add(new JLabel("Price:")); formPanel.add(priceField);
        formPanel.add(new JLabel("Cost Price:")); formPanel.add(costField);
        formPanel.add(new JLabel("Stock Qty:")); formPanel.add(stockField);
        formPanel.add(new JLabel("Reorder Level:")); formPanel.add(reorderField);
        formPanel.add(new JLabel("Image Path (optional):")); formPanel.add(imageRow);

        int result = JOptionPane.showConfirmDialog(this, formPanel,
                existing == null ? "Add Menu Item" : "Edit Menu Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        if (!ValidationUtil.isNonEmpty(nameField.getText())) {
            JOptionPane.showMessageDialog(this, "Item name is required.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal price = ValidationUtil.parsePositiveDecimal(priceField.getText());
        if (price == null) {
            JOptionPane.showMessageDialog(this, "Price must be a number greater than 0.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal cost = ValidationUtil.parseNonNegativeDecimal(costField.getText());
        if (cost == null) {
            JOptionPane.showMessageDialog(this, "Cost price must be a non-negative number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer stockQty = ValidationUtil.parseNonNegativeInt(stockField.getText());
        if (stockQty == null) {
            JOptionPane.showMessageDialog(this, "Stock quantity must be a non-negative whole number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer reorderLevel = ValidationUtil.parseNonNegativeInt(reorderField.getText());
        if (reorderLevel == null) {
            JOptionPane.showMessageDialog(this, "Reorder level must be a non-negative whole number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isNewItem = (existing == null);
        MenuItem m = existing != null ? existing : new MenuItem();
        Category selectedCat = (Category) categoryBox.getSelectedItem();
        m.setCategoryId(selectedCat.getCategoryId());
        m.setItemName(nameField.getText().trim());
        m.setDescription(descField.getText().trim());
        m.setPrice(price);
        m.setCostPrice(cost);
        m.setStockQty(stockQty);
        m.setReorderLevel(reorderLevel);
        m.setImagePath(imagePathField.getText().trim().isEmpty() ? null : imagePathField.getText().trim());
        m.setAvailable(true);

        boolean success = existing != null ? menuItemDAO.updateItem(m) : menuItemDAO.addItem(m);
        if (!success) {
            JOptionPane.showMessageDialog(this, "Failed to save item. Check DB connection.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if (isNewItem) {
            // Announce the new item to registered loyalty customers who have an email
            // on file — done in the background so the UI doesn't freeze on save.
            new SwingWorker<Integer, Void>() {
                @Override protected Integer doInBackground() { return notificationService.notifyNewItem(m); }
                @Override protected void done() {
                    try {
                        int count = get();
                        if (count > 0) {
                            JOptionPane.showMessageDialog(MenuManagementPanel.this,
                                    "New item saved, and " + count + " registered customer(s) were emailed about it.",
                                    "Notified Customers", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ignored) { }
                }
            }.execute();
        }
        refresh();
    }
}
