package com.bhojanaloy.gui;

import com.bhojanaloy.dao.MenuItemDAO;
import com.bhojanaloy.dao.RecipeDAO;
import com.bhojanaloy.model.MenuItem;
import com.bhojanaloy.model.Recipe;
import com.bhojanaloy.model.RecipeIngredient;
import com.bhojanaloy.util.ImageUtil;
import com.bhojanaloy.util.UITheme;
import com.bhojanaloy.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows how each menu item is prepared: a photo of the product, ingredients,
 * and step-by-step instructions — with a language dropdown (English / বাংলা /
 * Español / हिंदी / العربية) so staff can read the steps in whichever
 * language they're most comfortable with. Admin/Manager can add, edit, or
 * remove recipes; each recipe's instructions are entered per-language in the
 * Add/Edit dialog.
 */
public class RecipePanel extends JPanel {

    private final RecipeDAO recipeDAO = new RecipeDAO();
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final DefaultListModel<Recipe> listModel = new DefaultListModel<>();
    private final JList<Recipe> recipeList = new JList<>(listModel);

    private final JLabel photoLabel = new JLabel();
    private final JLabel metaLabel = new JLabel();
    private final JComboBox<String> languageBox;
    private final Map<String, String> languages = new LinkedHashMap<>(); // display name -> language code
    private final JEditorPane detailPane = new JEditorPane();

    private Recipe currentRecipe;

    public RecipePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(UITheme.heading("Recipes — How To Make Each Item"), BorderLayout.NORTH);

        languages.put("English", "en");
        languages.put("বাংলা (Bangla)", "bn");
        languages.put("Español (Spanish)", "es");
        languages.put("हिंदी (Hindi)", "hi");
        languages.put("العربية (Arabic)", "ar");
        languageBox = new JComboBox<>(languages.keySet().toArray(new String[0]));
        languageBox.addActionListener(e -> renderDetail());

        recipeList.setFont(UITheme.FONT_LABEL);
        recipeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                currentRecipe = recipeList.getSelectedValue();
                renderDetail();
            }
        });
        JScrollPane listScroll = new JScrollPane(recipeList);
        listScroll.setPreferredSize(new Dimension(260, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, buildDetailPanel());
        split.setDividerLocation(260);
        add(split, BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        loadRecipes();
    }

    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UITheme.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(UITheme.CREAM_DARKER, 1));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBorder(new EmptyBorder(12, 12, 6, 12));
        top.setOpaque(false);

        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setVerticalAlignment(SwingConstants.CENTER);
        photoLabel.setPreferredSize(new Dimension(110, 110));
        photoLabel.setBorder(BorderFactory.createLineBorder(UITheme.CREAM_DARKER, 1));

        JPanel infoCol = new JPanel(new BorderLayout(4, 4));
        infoCol.setOpaque(false);
        metaLabel.setFont(UITheme.FONT_LABEL);
        metaLabel.setForeground(UITheme.TEXT_DARK);
        JPanel languageRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        languageRow.setOpaque(false);
        languageRow.add(new JLabel("Language:"));
        languageBox.setFont(UITheme.FONT_LABEL);
        languageRow.add(languageBox);
        infoCol.add(metaLabel, BorderLayout.NORTH);
        infoCol.add(languageRow, BorderLayout.SOUTH);

        top.add(photoLabel, BorderLayout.WEST);
        top.add(infoCol, BorderLayout.CENTER);

        detailPane.setContentType("text/html");
        detailPane.setEditable(false);
        JScrollPane detailScroll = new JScrollPane(detailPane);
        detailScroll.setBorder(BorderFactory.createEmptyBorder());

        panel.add(top, BorderLayout.NORTH);
        panel.add(detailScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(UITheme.CREAM);
        JButton addBtn = UITheme.primaryButton("Add Recipe");
        JButton editBtn = UITheme.secondaryButton("Edit Selected");
        JButton deleteBtn = UITheme.dangerButton("Delete Selected");
        JButton refreshBtn = UITheme.secondaryButton("Refresh");

        addBtn.addActionListener(e -> showRecipeDialog(null));
        editBtn.addActionListener(e -> {
            if (currentRecipe != null) showRecipeDialog(currentRecipe);
            else JOptionPane.showMessageDialog(this, "Select a recipe first.", "No Selection", JOptionPane.WARNING_MESSAGE);
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadRecipes());

        bar.add(addBtn);
        bar.add(editBtn);
        bar.add(deleteBtn);
        bar.add(refreshBtn);
        return bar;
    }

    private void loadRecipes() {
        listModel.clear();
        for (Recipe r : recipeDAO.getAll()) listModel.addElement(r);
        if (!listModel.isEmpty()) {
            recipeList.setSelectedIndex(0);
        } else {
            currentRecipe = null;
            renderDetail();
        }
    }

    private void renderDetail() {
        if (currentRecipe == null) {
            photoLabel.setIcon(null);
            photoLabel.setText("");
            metaLabel.setText("No recipes found. Click \"Add Recipe\" to create one.");
            detailPane.setText("");
            return;
        }
        Recipe recipe = currentRecipe;

        JLabel imgHolder = ImageUtil.buildItemImageLabel(recipe.getItemImagePath(), recipe.getItemName(), 90);
        photoLabel.setIcon(imgHolder.getIcon());
        photoLabel.setText(imgHolder.getIcon() == null ? imgHolder.getText() : "");
        photoLabel.setFont(imgHolder.getFont());

        metaLabel.setText("<html><b>" + escapeHtml(recipe.getRecipeName()) + "</b> — for " + escapeHtml(recipe.getItemName()) +
                "<br>Prep time: " + recipe.getPrepTimeMin() + " min &nbsp; | &nbsp; Difficulty: " + recipe.getDifficulty() +
                " &nbsp; | &nbsp; Yield: " + recipe.getYieldQty() + " serving(s)</html>");

        String langCode = languages.get((String) languageBox.getSelectedItem());
        String instructions = recipe.getInstructionsFor(langCode);

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI; font-size:11pt; padding:6px;'>");
        sb.append("<h3>Ingredients</h3>");
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            sb.append("<p><i>(none listed)</i></p>");
        } else {
            sb.append("<ul>");
            for (RecipeIngredient ri : ingredients) {
                sb.append("<li>").append(escapeHtml(ri.getIngredientName())).append(": ")
                        .append(ri.getQuantityNeeded()).append(" ").append(escapeHtml(ri.getUnit())).append("</li>");
            }
            sb.append("</ul>");
        }
        sb.append("<h3>Step-by-step Instructions</h3>");
        sb.append(toHtmlSteps(instructions));
        sb.append("</body></html>");
        detailPane.setText(sb.toString());
        detailPane.setCaretPosition(0);
    }

    /** Converts "1. do this\n2. do that" style text into a proper HTML numbered list
     *  (renumbered by the browser/list itself, so it looks the same regardless of
     *  whether the stored text already has 1./2./3. prefixes or not). */
    private String toHtmlSteps(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) return "<p><i>(no instructions yet)</i></p>";
        StringBuilder sb = new StringBuilder("<ol>");
        for (String line : rawText.split("\\n")) {
            String step = line.trim();
            if (step.isEmpty()) continue;
            step = step.replaceFirst("^\\d+[.)]\\s*", "");
            sb.append("<li style='margin-bottom:6px;'>").append(escapeHtml(step)).append("</li>");
        }
        sb.append("</ol>");
        return sb.toString();
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void deleteSelected() {
        if (currentRecipe == null) {
            JOptionPane.showMessageDialog(this, "Select a recipe first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Delete the recipe \"" + currentRecipe.getRecipeName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        if (recipeDAO.deleteRecipe(currentRecipe.getRecipeId())) {
            loadRecipes();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete. Check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRecipeDialog(Recipe existing) {
        List<MenuItem> items = menuItemDAO.getAll();
        JComboBox<MenuItem> itemBox = new JComboBox<>(items.toArray(new MenuItem[0]));
        if (existing != null) {
            for (MenuItem mi : items) if (mi.getItemId() == existing.getItemId()) itemBox.setSelectedItem(mi);
        }

        JTextField nameField = new JTextField(existing != null ? existing.getRecipeName() : "");
        JTextField prepField = new JTextField(existing != null ? String.valueOf(existing.getPrepTimeMin()) : "5");
        JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        if (existing != null && existing.getDifficulty() != null) difficultyBox.setSelectedItem(existing.getDifficulty());
        JTextField yieldField = new JTextField(existing != null ? String.valueOf(existing.getYieldQty()) : "1");

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        formPanel.add(new JLabel("Menu Item:")); formPanel.add(itemBox);
        formPanel.add(new JLabel("Recipe Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Prep Time (minutes):")); formPanel.add(prepField);
        formPanel.add(new JLabel("Difficulty:")); formPanel.add(difficultyBox);
        formPanel.add(new JLabel("Yield (servings):")); formPanel.add(yieldField);

        JTabbedPane instructionTabs = new JTabbedPane();
        JTextArea enArea = new JTextArea(existing != null ? existing.getInstructions() : "", 8, 32);
        JTextArea bnArea = new JTextArea(existing != null ? existing.getInstructionsBn() : "", 8, 32);
        JTextArea esArea = new JTextArea(existing != null ? existing.getInstructionsEs() : "", 8, 32);
        JTextArea hiArea = new JTextArea(existing != null ? existing.getInstructionsHi() : "", 8, 32);
        JTextArea arArea = new JTextArea(existing != null ? existing.getInstructionsAr() : "", 8, 32);
        for (JTextArea a : new JTextArea[]{enArea, bnArea, esArea, hiArea, arArea}) {
            a.setLineWrap(true);
            a.setWrapStyleWord(true);
        }
        instructionTabs.addTab("English", new JScrollPane(enArea));
        instructionTabs.addTab("বাংলা", new JScrollPane(bnArea));
        instructionTabs.addTab("Español", new JScrollPane(esArea));
        instructionTabs.addTab("हिंदी", new JScrollPane(hiArea));
        instructionTabs.addTab("العربية", new JScrollPane(arArea));

        JPanel dialogPanel = new JPanel(new BorderLayout(8, 8));
        dialogPanel.add(formPanel, BorderLayout.NORTH);
        JPanel instructionsWrap = new JPanel(new BorderLayout());
        instructionsWrap.add(new JLabel("Step-by-step Instructions (one step per line — a language left blank falls back to English):"), BorderLayout.NORTH);
        instructionsWrap.add(instructionTabs, BorderLayout.CENTER);
        dialogPanel.add(instructionsWrap, BorderLayout.CENTER);
        dialogPanel.setPreferredSize(new Dimension(520, 480));

        int result = JOptionPane.showConfirmDialog(this, dialogPanel,
                existing == null ? "Add Recipe" : "Edit Recipe", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        MenuItem selectedItem = (MenuItem) itemBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Select a menu item for this recipe.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidationUtil.isNonEmpty(nameField.getText())) {
            JOptionPane.showMessageDialog(this, "Recipe name is required.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer prepTime = ValidationUtil.parseNonNegativeInt(prepField.getText());
        if (prepTime == null) {
            JOptionPane.showMessageDialog(this, "Prep time must be a non-negative whole number.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer yieldQty = ValidationUtil.parseNonNegativeInt(yieldField.getText());
        if (yieldQty == null || yieldQty == 0) {
            JOptionPane.showMessageDialog(this, "Yield must be a whole number of 1 or more.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!ValidationUtil.isNonEmpty(enArea.getText())) {
            JOptionPane.showMessageDialog(this, "English instructions are required (other languages are optional and fall back to English).",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Recipe r = existing != null ? existing : new Recipe();
        r.setItemId(selectedItem.getItemId());
        r.setRecipeName(nameField.getText().trim());
        r.setPrepTimeMin(prepTime);
        r.setDifficulty((String) difficultyBox.getSelectedItem());
        r.setYieldQty(yieldQty);
        r.setInstructions(enArea.getText().trim());
        r.setInstructionsBn(bnArea.getText().trim().isEmpty() ? null : bnArea.getText().trim());
        r.setInstructionsEs(esArea.getText().trim().isEmpty() ? null : esArea.getText().trim());
        r.setInstructionsHi(hiArea.getText().trim().isEmpty() ? null : hiArea.getText().trim());
        r.setInstructionsAr(arArea.getText().trim().isEmpty() ? null : arArea.getText().trim());

        boolean success = existing != null ? recipeDAO.updateRecipe(r) : recipeDAO.addRecipe(r);
        if (!success) {
            JOptionPane.showMessageDialog(this, "Failed to save recipe. Check database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadRecipes();
    }
}