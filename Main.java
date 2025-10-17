package com.example.taskhabitmanager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        loader.setController(new Controller());
        VBox root = loader.load();
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Task & Habit Manager (Desktop)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static class Controller {
        // REST base URL
        private final String baseUrl = "http://localhost:8080/api";

        private final HttpClient http = HttpClient.newBuilder().build();
        private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        @FXML
        private TableView<TaskRow> tasksTable;
        @FXML
        private TableColumn<TaskRow, Long> taskIdCol;
        @FXML
        private TableColumn<TaskRow, String> taskTitleCol;
        @FXML
        private TableColumn<TaskRow, String> taskDescCol;
        @FXML
        private TableColumn<TaskRow, LocalDate> taskDueCol;
        @FXML
        private TableColumn<TaskRow, Boolean> taskCompletedCol;
        @FXML
        private Button refreshTasksBtn;
        @FXML
        private Button addTaskBtn;
        @FXML
        private Button deleteTaskBtn;
        @FXML
        private Button markCompleteBtn;

        @FXML
        private TableView<HabitRow> habitsTable;
        @FXML
        private TableColumn<HabitRow, Long> habitIdCol;
        @FXML
        private TableColumn<HabitRow, String> habitNameCol;
        @FXML
        private TableColumn<HabitRow, String> habitDayCol;
        @FXML
        private Button refreshHabitsBtn;
        @FXML
        private Button addHabitBtn;
        @FXML
        private Button deleteHabitBtn;

        private final ObservableList<TaskRow> tasks = FXCollections.observableArrayList();
        private final ObservableList<HabitRow> habits = FXCollections.observableArrayList();

        public Controller() {
        }

        @FXML
        public void initialize() {
            // Tasks table config
            taskIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            taskTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            taskDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
            taskDueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
            taskCompletedCol.setCellValueFactory(new PropertyValueFactory<>("completed"));
            taskCompletedCol.setCellFactory(tc -> new CheckBoxTableCell<>());

            tasksTable.setItems(tasks);

            // Habits table config
            habitIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            habitNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            habitDayCol.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
            habitsTable.setItems(habits);

            // Button actions
            refreshTasksBtn.setOnAction(e -> loadTasks());
            refreshHabitsBtn.setOnAction(e -> loadHabits());

            addTaskBtn.setOnAction(e -> showAddTaskDialog());
            deleteTaskBtn.setOnAction(e -> deleteSelectedTask());
            markCompleteBtn.setOnAction(e -> markSelectedTaskComplete());

            addHabitBtn.setOnAction(e -> showAddHabitDialog());
            deleteHabitBtn.setOnAction(e -> deleteSelectedHabit());

            // Initial load
            loadTasks();
            loadHabits();
        }

        private void loadTasks() {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/tasks"))
                    .GET()
                    .build();
            http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(body -> {
                        try {
                            List<TaskDTO> list = mapper.readValue(body, new TypeReference<>() {});
                            tasks.clear();
                            for (TaskDTO t : list) tasks.add(new TaskRow(t));
                        } catch (Exception ex) {
                            showError("Failed to parse tasks: " + ex.getMessage());
                        }
                    })
                    .exceptionally(ex -> {
                        showError("Failed to load tasks: " + ex.getMessage());
                        return null;
                    });
        }

        private void loadHabits() {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/habits"))
                    .GET()
                    .build();
            http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(body -> {
                        try {
                            List<HabitDTO> list = mapper.readValue(body, new TypeReference<>() {});
                            habits.clear();
                            for (HabitDTO h : list) habits.add(new HabitRow(h));
                        } catch (Exception ex) {
                            showError("Failed to parse habits: " + ex.getMessage());
                        }
                    })
                    .exceptionally(ex -> {
                        showError("Failed to load habits: " + ex.getMessage());
                        return null;
                    });
        }

        private void showAddTaskDialog() {
            Dialog<TaskDTO> dialog = new Dialog<>();
            dialog.setTitle("Add Task");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            TextField titleField = new TextField();
            TextField descField = new TextField();
            DatePicker duePicker = new DatePicker();
            VBox box = new VBox(8, new Label("Title:"), titleField, new Label("Description:"), descField, new Label("Due Date:"), duePicker);
            dialog.getDialogPane().setContent(box);
            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    TaskDTO t = new TaskDTO();
                    t.title = titleField.getText();
                    t.description = descField.getText();
                    t.dueDate = duePicker.getValue();
                    t.completed = false;
                    return t;
                }
                return null;
            });
            dialog.showAndWait().ifPresent(this::createTask);
        }

        private void createTask(TaskDTO t) {
            try {
                String json = mapper.writeValueAsString(t);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/tasks"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(resp -> loadTasks())
                        .exceptionally(ex -> { showError("Failed to create task: " + ex.getMessage()); return null; });
            } catch (Exception ex) {
                showError("Failed to create task: " + ex.getMessage());
            }
        }

        private void deleteSelectedTask() {
            TaskRow sel = tasksTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/tasks/" + sel.getId()))
                    .DELETE()
                    .build();
            http.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(r -> loadTasks())
                    .exceptionally(ex -> { showError("Delete failed: " + ex.getMessage()); return null; });
        }

        private void markSelectedTaskComplete() {
            TaskRow sel = tasksTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            boolean newVal = !sel.isCompleted();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/tasks/" + sel.getId() + "/complete?completed=" + newVal))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            http.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(r -> loadTasks())
                    .exceptionally(ex -> { showError("Mark complete failed: " + ex.getMessage()); return null; });
        }

        private void showAddHabitDialog() {
            Dialog<HabitDTO> dialog = new Dialog<>();
            dialog.setTitle("Add Habit");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            TextField nameField = new TextField();
            TextField dayField = new TextField();
            VBox box = new VBox(8, new Label("Name:"), nameField, new Label("Day(s) of Week (e.g., MONDAY or MONDAY,TUESDAY):"), dayField);
            dialog.getDialogPane().setContent(box);
            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    HabitDTO h = new HabitDTO();
                    h.name = nameField.getText();
                    h.dayOfWeek = dayField.getText();
                    return h;
                }
                return null;
            });
            dialog.showAndWait().ifPresent(this::createHabit);
        }

        private void createHabit(HabitDTO h) {
            try {
                String json = mapper.writeValueAsString(h);
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/habits"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();
                http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(resp -> loadHabits())
                        .exceptionally(ex -> { showError("Failed to create habit: " + ex.getMessage()); return null; });
            } catch (Exception ex) {
                showError("Failed to create habit: " + ex.getMessage());
            }
        }

        private void deleteSelectedHabit() {
            HabitRow sel = habitsTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/habits/" + sel.getId()))
                    .DELETE()
                    .build();
            http.sendAsync(req, HttpResponse.BodyHandlers.discarding())
                    .thenAccept(r -> loadHabits())
                    .exceptionally(ex -> { showError("Delete failed: " + ex.getMessage()); return null; });
        }

        private void showError(String message) {
            System.err.println(message);
            // Also show a dialog (must run on FX thread)
            javafx.application.Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
                a.showAndWait();
            });
        }

        // DTOs and row wrappers
        public static class TaskDTO {
            public Long id;
            public String title;
            public String description;
            public LocalDate dueDate;
            public boolean completed;
        }

        public static class HabitDTO {
            public Long id;
            public String name;
            public String dayOfWeek;
        }

        public static class TaskRow {
            private final LongProperty id = new SimpleLongProperty();
            private final StringProperty title = new SimpleStringProperty();
            private final StringProperty description = new SimpleStringProperty();
            private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>();
            private final BooleanProperty completed = new SimpleBooleanProperty();

            public TaskRow(TaskDTO dto) {
                if (dto.id != null) id.set(dto.id);
                title.set(dto.title);
                description.set(dto.description);
                dueDate.set(dto.dueDate);
                completed.set(dto.completed);
            }

            public long getId() { return id.get(); }
            public LongProperty idProperty() { return id; }

            public String getTitle() { return title.get(); }
            public StringProperty titleProperty() { return title; }

            public String getDescription() { return description.get(); }
            public StringProperty descriptionProperty() { return description; }

            public LocalDate getDueDate() { return dueDate.get(); }
            public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }

            public boolean isCompleted() { return completed.get(); }
            public BooleanProperty completedProperty() { return completed; }
            public void setCompleted(boolean v) { completed.set(v); }
        }

        public static class HabitRow {
            private final LongProperty id = new SimpleLongProperty();
            private final StringProperty name = new SimpleStringProperty();
            private final StringProperty dayOfWeek = new SimpleStringProperty();

            public HabitRow(HabitDTO dto) {
                if (dto.id != null) id.set(dto.id);
                name.set(dto.name);
                dayOfWeek.set(dto.dayOfWeek);
            }

            public long getId() { return id.get(); }
            public String getName() { return name.get(); }
            public String getDayOfWeek() { return dayOfWeek; }

            public LongProperty idProperty() { return id; }
            public StringProperty nameProperty() { return name; }
            public StringProperty dayOfWeekProperty() { return dayOfWeek; }
        }
    }
}
