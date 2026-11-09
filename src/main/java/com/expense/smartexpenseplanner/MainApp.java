package com.expense.smartexpenseplanner;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;

public class MainApp extends Application {

    private Stage primaryStage;
    private double budget = 0;

    private final List<Transaction> transactions = new ArrayList<>();

    private Label incomeLabel;
    private Label expenseLabel;
    private Label balanceLabel;
    private Label budgetLabel;

    private TableView<Transaction> transactionTable;
    private PieChart pieChart;
    private ProgressBar budgetProgressBar;
    private Label budgetProgressText;

    static class Transaction {

        String type;
        String category;
        double amount;
        LocalDateTime dateTime;

        Transaction(String type, String category,
                    double amount, LocalDateTime dateTime) {

            this.type = type;
            this.category = category;
            this.amount = amount;
            this.dateTime = dateTime;
        }

        @Override
        public String toString() {
            return type + " | " + category
                    + " | Rs. " + amount
                    + " | " + dateTime;
        }
    }

    @Override
    public void start(Stage stage) {

        primaryStage = stage;

        loadData();

        Label title = new Label("Smart Expense Planner");

        title.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label subtitle = new Label(
                "Manage your money. Track your spending. Plan your future."
        );

        subtitle.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #dce6f5;"
        );

        VBox headerContent = new VBox(6, title, subtitle);

        headerContent.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(headerContent);

        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25, 35, 25, 35));

        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #172554, #263859);"
        );

        incomeLabel = new Label();
        expenseLabel = new Label();
        balanceLabel = new Label();
        budgetLabel = new Label();
        budgetProgressBar = new ProgressBar(0);
        budgetProgressBar.setMaxWidth(Double.MAX_VALUE);
        budgetProgressBar.setPrefHeight(20);

        budgetProgressText = new Label(
                "Set a budget to track your spending"
        );

        Label progressTitle = new Label("Monthly Budget Usage");

        progressTitle.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #172554;"
        );

        budgetProgressText.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #64748b;"
        );

        VBox budgetProgressBox = new VBox(
                12, progressTitle, budgetProgressBar, budgetProgressText
        );

        budgetProgressBox.setPadding(new Insets(20));

        budgetProgressBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 10, 0, 0, 3);"
        );

        VBox incomeCard = createCard("Total Income", incomeLabel);
        VBox expenseCard = createCard("Total Expenses", expenseLabel);
        VBox balanceCard = createCard("Available Balance", balanceLabel);
        VBox budgetCard = createCard("Remaining Budget", budgetLabel);

        HBox cards = new HBox(15,
                incomeCard, expenseCard,
                balanceCard, budgetCard);

        cards.setAlignment(Pos.CENTER);
        cards.setPadding(new Insets(15));

        Button addIncomeButton = new Button("Add Income");
        Button addExpenseButton = new Button("Add Expense");
        Button budgetButton = new Button("Set Budget");
        Button editButton = new Button("Edit Transaction");
        Button deleteButton = new Button("Delete Transaction");

        Button dailyReportButton = new Button("Daily Report");
        Button monthlyReportButton = new Button("Monthly Report");
        Button exportButton = new Button("Export to CSV");

        styleButton(addIncomeButton);
        styleButton(addExpenseButton);
        styleButton(budgetButton);
        styleButton(editButton);
        styleButton(deleteButton);
        styleButton(dailyReportButton);
        styleButton(monthlyReportButton);
        styleButton(exportButton);

        addIncomeButton.setOnAction(e -> addIncome());
        addExpenseButton.setOnAction(e -> addExpense());
        budgetButton.setOnAction(e -> setBudget());
        editButton.setOnAction(e -> editTransaction());
        deleteButton.setOnAction(e -> deleteTransaction());

        dailyReportButton.setOnAction(e -> showDailyReport());
        monthlyReportButton.setOnAction(e -> showMonthlyReport());
        exportButton.setOnAction(e -> exportToCSV());

        HBox buttons1 = new HBox(10,
                addIncomeButton, addExpenseButton, budgetButton);

        HBox buttons2 = new HBox(10,
                editButton, deleteButton);

        HBox buttons3 = new HBox(10,
                dailyReportButton, monthlyReportButton, exportButton);

        buttons1.setAlignment(Pos.CENTER);
        buttons2.setAlignment(Pos.CENTER);
        buttons3.setAlignment(Pos.CENTER);

        transactionTable = new TableView<>();

        transactionTable.setPrefHeight(280);
        transactionTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        TableColumn<Transaction, String> dateColumn =
                new TableColumn<>("Date");

        dateColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().dateTime.toLocalDate().toString()
                )
        );

        TableColumn<Transaction, String> typeColumn =
                new TableColumn<>("Type");

        typeColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().type
                )
        );

        TableColumn<Transaction, String> categoryColumn =
                new TableColumn<>("Category");

        categoryColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().category
                )
        );

        TableColumn<Transaction, String> amountColumn =
                new TableColumn<>("Amount");

        amountColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("Rs. %.2f", data.getValue().amount)
                )
        );

        transactionTable.getColumns().addAll(
                dateColumn,
                typeColumn,
                categoryColumn,
                amountColumn
        );
        typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {

            @Override
            protected void updateItem(String item, boolean empty) {

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {

                    setText(item);

                    if (item.equals("INCOME")) {
                        setStyle(
                                "-fx-text-fill: #16a34a;" +
                                        "-fx-font-weight: bold;"
                        );
                    } else {
                        setStyle(
                                "-fx-text-fill: #dc2626;" +
                                        "-fx-font-weight: bold;"
                        );
                    }
                }
            }
        });
        dateColumn.setSortType(
                TableColumn.SortType.DESCENDING
        );

        transactionTable.getSortOrder().add(dateColumn);

        transactionTable.setStyle(
                "-fx-font-size: 13px;"
        );
        transactionTable.setPlaceholder(
                new Label("No transactions yet. Add income or expenses!")
        );

        transactionTable.setFixedCellSize(42);

        transactionTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        dateColumn.setStyle("-fx-alignment: CENTER;");
        typeColumn.setStyle("-fx-alignment: CENTER;");
        categoryColumn.setStyle("-fx-alignment: CENTER;");
        amountColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        Label transactionTitle = new Label("Recent Transactions");
        transactionTitle.setStyle(
                "-fx-font-size: 19px; -fx-font-weight: bold;");

        TextField searchField = new TextField();
        searchField.setPromptText(
                "Search type, category, amount or date..."
        );

        searchField.setPrefHeight(38);
        searchField.setPrefWidth(250);

        Button searchButton = new Button("Search");
        Button showAllButton = new Button("Show All");

        styleButton(searchButton);
        styleButton(showAllButton);

        searchButton.setOnAction(e -> {

            String keyword = searchField.getText()
                    .trim().toLowerCase();

            List<Transaction> filtered = new ArrayList<>();

            for (Transaction t : transactions) {

                String details =
                        t.type + " " +
                                t.category + " " +
                                t.amount + " " +
                                t.dateTime.toLocalDate();

                if (details.toLowerCase().contains(keyword)) {
                    filtered.add(t);
                }
            }

            transactionTable.setItems(
                    FXCollections.observableArrayList(filtered)
            );
        });
        showAllButton.setOnAction(e -> {
            searchField.clear();
            updateDashboard();
        });

        HBox searchBox = new HBox(10,
                searchField, searchButton, showAllButton);

        searchBox.setAlignment(Pos.CENTER_LEFT);

        VBox transactionBox = new VBox(10,
                transactionTitle, searchBox, transactionTable);
        transactionBox.setPadding(new Insets(20));

        transactionBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 10, 0, 0, 3);"
        );

        transactionTitle.setStyle(
                "-fx-font-size: 19px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #172554;"
        );

        transactionTable.setPrefHeight(230);

        transactionTable.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;" +
                        "-fx-font-size: 14px;"
        );

        pieChart = new PieChart();
        pieChart.setTitle("Expense Breakdown");

        pieChart.setPrefSize(500, 350);

        pieChart.setLegendVisible(true);

        pieChart.setLabelsVisible(true);

        pieChart.setAnimated(true);

        pieChart.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-font-size: 13px;"
        );
        pieChart.setTitle("Expense Categories");
        pieChart.setPrefHeight(300);

        VBox content = new VBox(18,
                cards,
                budgetProgressBox,
                buttons1,
                buttons2,
                pieChart,
                transactionBox,
                buttons3);
        content.setPadding(new Insets(25));

        content.setStyle(
                "-fx-background-color: #f1f5f9;"
        );

        content.setSpacing(22);

        cards.setSpacing(18);
        cards.setPadding(new Insets(5));

        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #eaf0f8;");

        ScrollPane scrollPane = new ScrollPane(content);

        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.setStyle(
                "-fx-background: #eaf0f8;" +
                        "-fx-background-color: #eaf0f8;"
        );
        Label footer = new Label(
                "Smart Expense Planner  |  Manage • Track • Save"
        );

        footer.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #64748b;" +
                        "-fx-font-weight: bold;"
        );

        HBox footerBox = new HBox(footer);

        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(15));

        footerBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1 0 0 0;"
        );
        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(scrollPane);
        root.setBottom(footerBox);

        Scene scene = new Scene(root, 1150, 800);

        stage.setMinWidth(950);
        stage.setMinHeight(650);

        stage.setTitle("Smart Expense Planner | Personal Finance Dashboard");
        stage.setScene(scene);
        stage.show();

        updateDashboard();
    }

    private VBox createCard(String title, Label value) {

        Label heading = new Label(title);
        heading.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #64748b;" +
                        "-fx-font-weight: bold;"
        );

        value.setStyle(
                "-fx-font-size: 25px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #172554;"
        );

        VBox card = new VBox(12, heading, value);

        card.setPadding(new Insets(22));
        card.setPrefWidth(220);
        card.setMinHeight(110);

        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 12, 0, 0, 4);"
        );

        return card;
    }

    private void styleButton(Button button) {

        button.setStyle(
                "-fx-background-color: #2563eb;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 9;" +
                        "-fx-padding: 11 18 11 18;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(event -> {
            button.setStyle(
                    "-fx-background-color: #1d4ed8;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 9;" +
                            "-fx-padding: 11 18 11 18;" +
                            "-fx-cursor: hand;"
            );
        });

        button.setOnMouseExited(event -> {
            button.setStyle(
                    "-fx-background-color: #2563eb;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 9;" +
                            "-fx-padding: 11 18 11 18;" +
                            "-fx-cursor: hand;"
            );
        });
    }

    private void addIncome() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Add Income");
        dialog.setHeaderText("Enter income amount");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(value -> {

            try {
                double amount = Double.parseDouble(value);

                if (amount <= 0) {
                    showMessage("Error", "Enter a positive amount.");
                    return;
                }

                transactions.add(new Transaction(
                        "INCOME", "Income",
                        amount, LocalDateTime.now()));

                updateDashboard();
                saveData();

            } catch (NumberFormatException ex) {
                showMessage("Error", "Enter a valid number.");
            }
        });
    }

    private void addExpense() {

        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle("Add Expense");
        dialog.setHeaderText("Enter expense amount");
        dialog.setContentText("Amount:");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) return;

        try {
            double amount = Double.parseDouble(result.get());

            if (amount <= 0) {
                showMessage("Error", "Enter a positive amount.");
                return;
            }

            List<String> categories = Arrays.asList(
                    "Food", "Travel", "Education",
                    "Shopping", "Entertainment", "Other");

            ChoiceDialog<String> categoryDialog =
                    new ChoiceDialog<>("Food", categories);

            categoryDialog.setTitle("Expense Category");
            categoryDialog.setHeaderText("Select category");

            Optional<String> category = categoryDialog.showAndWait();

            category.ifPresent(selected -> {

                transactions.add(new Transaction(
                        "EXPENSE", selected,
                        amount, LocalDateTime.now()));

                updateDashboard();
                saveData();
            });

        } catch (NumberFormatException ex) {
            showMessage("Error", "Enter a valid number.");
        }
    }

    private void setBudget() {

        TextInputDialog dialog =
                new TextInputDialog(String.valueOf(budget));

        dialog.setTitle("Monthly Budget");
        dialog.setHeaderText("Set your monthly budget");
        dialog.setContentText("Budget amount:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(value -> {

            try {
                double newBudget = Double.parseDouble(value);

                if (newBudget < 0) {
                    showMessage("Error", "Budget cannot be negative.");
                    return;
                }

                budget = newBudget;

                updateDashboard();
                saveData();

            } catch (NumberFormatException ex) {
                showMessage("Error", "Enter a valid amount.");
            }
        });
    }

    private void editTransaction() {

        Transaction selected =
                transactionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Notice", "Please select a transaction first.");
            return;
        }

        TextInputDialog amountDialog =
                new TextInputDialog(String.valueOf(selected.amount));

        amountDialog.setTitle("Edit Transaction");
        amountDialog.setHeaderText("Update transaction amount");
        amountDialog.setContentText("New amount:");

        Optional<String> result = amountDialog.showAndWait();

        if (result.isEmpty()) return;

        try {

            double newAmount = Double.parseDouble(result.get());

            if (newAmount <= 0) {
                showMessage("Error", "Enter a positive amount.");
                return;
            }

            if (selected.type.equals("EXPENSE")) {

                List<String> categories = Arrays.asList(
                        "Food", "Travel", "Education",
                        "Shopping", "Entertainment", "Other"
                );

                ChoiceDialog<String> categoryDialog =
                        new ChoiceDialog<>(selected.category, categories);

                categoryDialog.setTitle("Edit Category");
                categoryDialog.setHeaderText("Select new category");

                Optional<String> category =
                        categoryDialog.showAndWait();

                if (category.isEmpty()) return;

                selected.category = category.get();
            }

            selected.amount = newAmount;

            updateDashboard();
            saveData();

            showMessage("Success", "Transaction updated successfully.");

        } catch (NumberFormatException ex) {

            showMessage("Error", "Please enter a valid amount.");
        }
    }

    private void deleteTransaction() {

        Transaction selected =
                transactionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Notice", "Please select a transaction first.");
            return;
        }

        Alert confirmation =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Delete Transaction");
        confirmation.setHeaderText("Confirm transaction deletion");

        confirmation.setContentText(
                "Are you sure you want to delete this transaction?\n\n" +
                        selected.type + " - " + selected.category +
                        "\nAmount: Rs. " + selected.amount
        );

        Optional<ButtonType> answer = confirmation.showAndWait();

        if (answer.isPresent() &&
                answer.get() == ButtonType.OK) {

            transactions.remove(selected);

            updateDashboard();
            saveData();

            showMessage("Success", "Transaction deleted successfully.");
        }
    }

    private void updateDashboard() {

        double totalIncome = 0;
        double totalExpense = 0;

        Map<String, Double> categoryTotals = new HashMap<>();

        for (Transaction t : transactions) {

            if (t.type.equals("INCOME")) {

                totalIncome += t.amount;

            } else {

                totalExpense += t.amount;

                categoryTotals.put(
                        t.category,
                        categoryTotals.getOrDefault(t.category, 0.0)
                                + t.amount);
            }
        }

        incomeLabel.setText("Rs. " + totalIncome);
        expenseLabel.setText("Rs. " + totalExpense);
        balanceLabel.setText("Rs. " + (totalIncome - totalExpense));
        budgetLabel.setText("Rs. " + (budget - totalExpense));
        if (budget > 0) {

            double progress = totalExpense / budget;

            budgetProgressBar.setProgress(
                    Math.min(progress, 1.0)
            );

            double remaining = budget - totalExpense;

            budgetProgressText.setText(
                    String.format(
                            "%.0f%% used | Rs. %.2f remaining",
                            progress * 100,
                            remaining
                    )
            );

        } else {

            budgetProgressBar.setProgress(0);

            budgetProgressText.setText(
                    "Please set your budget to track spending."
            );
        }

        if (budget > 0 && totalExpense >= budget) {

            budgetLabel.setStyle(
                    "-fx-text-fill: red;" +
                            "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;"
            );

        } else {

            budgetLabel.setStyle(
                    "-fx-text-fill: green;" +
                            "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;"
            );
        }

        transactionTable.setItems(
                FXCollections.observableArrayList(transactions));

        pieChart.getData().clear();

        for (String category : categoryTotals.keySet()) {

            pieChart.getData().add(
                    new PieChart.Data(
                            category, categoryTotals.get(category)));
        }
    }

    private void showDailyReport() {

        TextInputDialog dialog =
                new TextInputDialog(LocalDate.now().toString());

        dialog.setTitle("Daily Report");
        dialog.setHeaderText("Enter date (YYYY-MM-DD)");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) return;

        try {
            LocalDate date = LocalDate.parse(result.get());

            showReport("Daily Report - " + date,
                    t -> t.dateTime.toLocalDate().equals(date));

        } catch (Exception ex) {
            showMessage("Error", "Use date format YYYY-MM-DD.");
        }
    }

    private void showMonthlyReport() {

        TextInputDialog dialog =
                new TextInputDialog(YearMonth.now().toString());

        dialog.setTitle("Monthly Report");
        dialog.setHeaderText("Enter month (YYYY-MM)");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) return;

        try {
            YearMonth month = YearMonth.parse(result.get());

            showReport("Monthly Report - " + month,
                    t -> YearMonth.from(t.dateTime).equals(month));

        } catch (Exception ex) {
            showMessage("Error", "Use month format YYYY-MM.");
        }
    }
    private void showReport(
            String title, Predicate<Transaction> filter) {

        double income = 0;
        double expense = 0;

        StringBuilder details = new StringBuilder();

        for (Transaction t : transactions) {

            if (filter.test(t)) {

                details.append(t).append("\n");

                if (t.type.equals("INCOME")) {
                    income += t.amount;
                } else {
                    expense += t.amount;
                }
            }
        }

        details.insert(0,
                "Total Income: Rs. " + income +
                        "\nTotal Expenses: Rs. " + expense +
                        "\nBalance: Rs. " + (income - expense) +
                        "\n\nTransactions:\n");

        TextArea area = new TextArea(details.toString());
        area.setEditable(false);
        area.setWrapText(true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.getDialogPane().setContent(area);
        alert.getDialogPane().setPrefSize(550, 400);
        alert.showAndWait();
    }

    private void exportToCSV() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Export Transactions");
        fileChooser.setInitialFileName("expense_report.csv");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file == null) return;

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(file))) {

            writer.println("Type,Category,Amount,Date Time");

            for (Transaction t : transactions) {

                writer.println(
                        csvValue(t.type) + "," +
                                csvValue(t.category) + "," +
                                t.amount + "," +
                                csvValue(t.dateTime.toString())
                );
            }

            showMessage("Success", "CSV file exported successfully.");

        } catch (IOException ex) {
            showMessage("Error", "Unable to export CSV file.");
        }
    }

    private String csvValue(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void saveData() {

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter("expense_data.txt"))) {

            writer.println("BUDGET|" + budget);

            for (Transaction t : transactions) {

                writer.println(
                        t.type + "|" +
                                t.amount + "|" +
                                t.category + "|" +
                                t.dateTime
                );
            }

        } catch (IOException ex) {
            showMessage("Error", "Unable to save data.");
        }
    }

    private void loadData() {

        File file = new File("expense_data.txt");

        if (!file.exists()) return;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                try {

                    String[] parts = line.split("\\|");

                    if (parts[0].equals("BUDGET")) {

                        budget = Double.parseDouble(parts[1]);

                    } else if (parts.length >= 4) {

                        String type = parts[0];
                        double amount = Double.parseDouble(parts[1]);
                        String category = parts[2];
                        LocalDateTime dateTime =
                                LocalDateTime.parse(parts[3]);

                        transactions.add(new Transaction(
                                type, category, amount, dateTime));
                    }

                } catch (Exception ex) {
                    System.out.println("Skipped invalid data: " + line);
                }
            }

        } catch (IOException ex) {
            showMessage("Error", "Unable to load saved data.");
        }
    }

    private void showMessage(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    @Override
    public void stop() {
        saveData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}