package com.elearning.ui.components;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Video player component using JavaFX Media embedded in Swing
 * Provides play, pause, seek, and volume controls
 */
public class VideoPlayerPanel extends JPanel {
    private final JFXPanel fxPanel;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private final String videoPath;
    private boolean isInitialized = false;

    public VideoPlayerPanel(String videoPath) {
        this.videoPath = videoPath;
        this.fxPanel = new JFXPanel();

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 450));

        add(fxPanel, BorderLayout.CENTER);

        // Initialize JavaFX on the JavaFX Application Thread
        Platform.runLater(this::initFX);
    }

    /**
     * Initialize JavaFX components
     * Must be called on JavaFX Application Thread
     */
    private void initFX() {
        try {
            // Validate video file
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                showError("Video file not found: " + videoPath);
                return;
            }

            // Create Media and MediaPlayer
            String mediaUrl = videoFile.toURI().toString();
            Media media = new Media(mediaUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);

            // Configure MediaView
            mediaView.setPreserveRatio(true);
            mediaView.setSmooth(true);

            // Create UI
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: black;");

            // Media view in center
            BorderPane mediaContainer = new BorderPane();
            mediaContainer.setStyle("-fx-background-color: black;");
            mediaContainer.setCenter(mediaView);
            root.setCenter(mediaContainer);

            // Bind media view size to container size
            mediaView.fitWidthProperty().bind(mediaContainer.widthProperty());
            mediaView.fitHeightProperty().bind(mediaContainer.heightProperty());

            // Controls at bottom
            HBox controls = createControls();
            root.setBottom(controls);

            // Create scene
            Scene scene = new Scene(root);
            fxPanel.setScene(scene);

            isInitialized = true;

            // Handle media errors
            mediaPlayer.setOnError(() -> {
                showError("Media error: " + mediaPlayer.getError().getMessage());
            });

        } catch (Exception e) {
            showError("Failed to load video: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create player controls
     */
    private HBox createControls() {
        HBox controls = new HBox(10);
        controls.setStyle("-fx-background-color: #2c3e50; -fx-padding: 10;");

        // Play/Pause button
        Button playPauseBtn = new Button("▶");
        playPauseBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");
        playPauseBtn.setOnAction(e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseBtn.setText("▶");
            } else {
                mediaPlayer.play();
                playPauseBtn.setText("⏸");
            }
        });

        // Stop button
        Button stopBtn = new Button("⏹");
        stopBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        stopBtn.setOnAction(e -> {
            mediaPlayer.stop();
            playPauseBtn.setText("▶");
        });

        // Time label
        Label timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        // Progress slider
        Slider progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(100);
        progressSlider.setValue(0);
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        progressSlider.setStyle("-fx-background-color: transparent;");

        // Update progress slider and time label
        mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
            if (!progressSlider.isValueChanging()) {
                Duration total = mediaPlayer.getTotalDuration();
                if (total != null && total.toMillis() > 0) {
                    progressSlider.setValue(newTime.toMillis() / total.toMillis() * 100);
                    timeLabel.setText(formatTime(newTime) + " / " + formatTime(total));
                }
            }
        });

        // Seek when slider is moved
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressSlider.isValueChanging()) {
                Duration total = mediaPlayer.getTotalDuration();
                if (total != null) {
                    mediaPlayer.seek(total.multiply(newVal.doubleValue() / 100));
                }
            }
        });

        // Volume slider
        Slider volumeSlider = new Slider(0, 100, 50);
        volumeSlider.setPrefWidth(100);
        volumeSlider.setStyle("-fx-background-color: transparent;");
        Label volumeLabel = new Label("🔊");
        volumeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        controls.getChildren().addAll(
            playPauseBtn,
            stopBtn,
            timeLabel,
            progressSlider,
            spacer,
            volumeLabel,
            volumeSlider
        );

        return controls;
    }

    /**
     * Format duration to MM:SS
     */
    private String formatTime(Duration duration) {
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            BorderPane errorPane = new BorderPane();
            errorPane.setStyle("-fx-background-color: black;");

            Label errorLabel = new Label(message);
            errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 20;");
            errorPane.setCenter(errorLabel);

            Scene scene = new Scene(errorPane);
            fxPanel.setScene(scene);
        });
    }

    /**
     * Play the video
     */
    public void play() {
        if (isInitialized && mediaPlayer != null) {
            Platform.runLater(() -> mediaPlayer.play());
        }
    }

    /**
     * Pause the video
     */
    public void pause() {
        if (isInitialized && mediaPlayer != null) {
            Platform.runLater(() -> mediaPlayer.pause());
        }
    }

    /**
     * Stop the video
     */
    public void stop() {
        if (isInitialized && mediaPlayer != null) {
            Platform.runLater(() -> mediaPlayer.stop());
        }
    }

    /**
     * Release resources
     * IMPORTANT: Call this when disposing the panel
     */
    public void dispose() {
        if (mediaPlayer != null) {
            Platform.runLater(() -> {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            });
        }
    }

    /**
     * Get media player (for advanced control)
     */
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * Check if player is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
