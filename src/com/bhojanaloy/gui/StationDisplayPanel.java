package com.bhojanaloy.gui;

import com.bhojanaloy.dao.CategoryDAO;
import com.bhojanaloy.dao.OrderDAO;
import com.bhojanaloy.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Kitchen/Station Display: shows every pending order item grouped by the
 * prep counter that makes it (Coffee Bar, Bakery, Sandwich Station, ...),
 * so staff at each station know exactly what to prepare next. Staff advance
 * each item through PENDING -> PREPARING -> READY -> SERVED. Auto-refreshes
 * every 5 seconds so new orders from the POS/Online screens appear live.
 */
public class StationDisplayPanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final JComboBox<String> stationFilter = new JComboBox<>();
    private final JPanel queueContainer = new JPanel();
    private Timer refreshTimer;

    public StationDisplayPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.CREAM);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(buildHeader(), BorderLayout.NORTH);

        queueContainer.setLayout(new BoxLayout(queueContainer, BoxLayout.Y_AXIS));
        queueContainer.setBackground(UITheme.CREAM);
        JScrollPane scrollPane = new JScrollPane(queueContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadStations();
        refreshQueue();

        refreshTimer = new Timer(5000, e -> refreshQueue());
        refreshTimer.start();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        header.setBackground(UITheme.CREAM);
        header.add(UITheme.heading("Kitchen / Station Display"));
        header.add(new JLabel("     Station:"));
        stationFilter.addActionListener(e -> refreshQueue());
        header.add(stationFilter);
        JButton refreshBtn = UITheme.secondaryButton("Refresh Now");
        refreshBtn.addActionListener(e -> refreshQueue());
        header.add(refreshBtn);
        return header;
    }

    private void loadStations() {
        stationFilter.removeAllItems();
        stationFilter.addItem("All Stations");
        for (String station : categoryDAO.getDistinctStations()) {
            stationFilter.addItem(station);
        }
    }

    private void refreshQueue() {
        String selected = (String) stationFilter.getSelectedItem();
        String filter = (selected == null || selected.equals("All Stations")) ? null : selected;
        List<Map<String, Object>> queue = orderDAO.getStationQueue(filter);

        queueContainer.removeAll();
        if (queue.isEmpty()) {
            JLabel empty = new JLabel("No pending items — all caught up! ✅");
            empty.setFont(UITheme.FONT_HEADING);
            empty.setForeground(UITheme.COFFEE_LIGHT);
            empty.setBorder(new EmptyBorder(30, 10, 10, 10));
            queueContainer.add(empty);
        } else {
            for (Map<String, Object> row : queue) {
                queueContainer.add(buildTicketCard(row));
                queueContainer.add(Box.createVerticalStrut(8));
            }
        }
        queueContainer.revalidate();
        queueContainer.repaint();
    }

    private JPanel buildTicketCard(Map<String, Object> row) {
        String prepStatus = String.valueOf(row.get("prep_status"));
        Color accent = statusColor(prepStatus);

        JPanel card = new JPanel(new BorderLayout(10, 4));
        card.setBackground(UITheme.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 6, 0, 0, accent),
                new EmptyBorder(10, 14, 10, 14)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        String orderType = String.valueOf(row.get("order_type"));
        String orderSource = String.valueOf(row.get("order_source"));
        JLabel info = new JLabel(String.format("<html><b>%s x%s</b> &nbsp;—&nbsp; Order %s &nbsp;(%s / %s, %s)</html>",
                row.get("item_name"), row.get("quantity"), row.get("order_number"),
                row.get("station_name"), orderType, orderSource));
        info.setFont(UITheme.FONT_LABEL_B);

        JLabel statusLabel = new JLabel(prepStatus);
        statusLabel.setFont(UITheme.FONT_LABEL);
        statusLabel.setForeground(accent);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(info);
        left.add(statusLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);

        int orderItemId = ((Number) row.get("order_item_id")).intValue();

        switch (prepStatus) {
            case "PENDING":
                JButton startBtn = UITheme.secondaryButton("Start Preparing");
                startBtn.addActionListener(e -> advance(orderItemId, "PREPARING"));
                actions.add(startBtn);
                break;
            case "PREPARING":
                JButton readyBtn = UITheme.primaryButton("Mark Ready");
                readyBtn.addActionListener(e -> advance(orderItemId, "READY"));
                actions.add(readyBtn);
                break;
            case "READY":
                JButton servedBtn = UITheme.successButton("Served / Picked Up");
                servedBtn.addActionListener(e -> advance(orderItemId, "SERVED"));
                actions.add(servedBtn);
                break;
            default:
                break;
        }

        card.add(left, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private Color statusColor(String status) {
        switch (status) {
            case "PENDING": return UITheme.ACCENT_RED;
            case "PREPARING": return UITheme.ACCENT_ORANGE;
            case "READY": return UITheme.ACCENT_GREEN;
            default: return UITheme.COFFEE_LIGHT;
        }
    }

    private void advance(int orderItemId, String newStatus) {
        orderDAO.updateItemPrepStatus(orderItemId, newStatus);
        refreshQueue();
    }
}
