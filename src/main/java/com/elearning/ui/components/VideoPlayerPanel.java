package com.elearning.ui.components;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Video player component using JavaFX Media embedded in Swing
 * Provides play, pause, seek, and volume controls
 */
public class VideoPlayerPanel extends JPanel {
    private JFXPanel fxPanel;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private String videoPath;
    private boolean isInitialized = false;
    private volatile boolean disposed = false;

    // Static initializer to configure JavaFX Platform
    static {
        // Prevent JavaFX Platform from exiting when all windows are closed
        // This is crucial for multiple JFXPanel instances
        try {
            Platform.setImplicitExit(false);
            System.out.println("JavaFX Platform implicit exit disabled");
        } catch (IllegalStateException e) {
            // Platform might already be initialized
            System.out.println("JavaFX Platform already initialized: " + e.getMessage());
        }
    }

    public VideoPlayerPanel(String videoPath) {
        System.out.println("=== VideoPlayerPanel constructor called for: " + videoPath);
        this.videoPath = videoPath;

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 450));

        System.out.println("Creating JFXPanel...");
        // Create and initialize JFXPanel
        // This implicitly initializes JavaFX toolkit if needed
        fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        System.out.println("JFXPanel created and added");

        // Add a hierarchy listener to know when the component is actually displayed
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing() && !isInitialized && !disposed) {
                    System.out.println("VideoPlayerPanel is now showing, triggering initialization");
                    scheduleInitialization();
                }
            }
        });

        // Also try immediate initialization
        System.out.println("Attempting immediate initialization...");
        scheduleInitialization();
    }

    /**
     * Schedule initialization on JavaFX thread with debug logging
     */
    private void scheduleInitialization() {
        System.out.println("scheduleInitialization() called for: " + videoPath);

        // Create a CountDownLatch to track initialization
        final CountDownLatch initLatch = new CountDownLatch(1);

        // Schedule initialization on JavaFX thread
        try {
            System.out.println("Calling Platform.runLater...");
            Platform.runLater(() -> {
                System.out.println("*** Platform.runLater EXECUTING for: " + videoPath + " ***");
                try {
                    if (!isInitialized && !disposed) {
                        initFX();
                    } else {
                        System.out.println("Skipping initFX - already initialized or disposed");
                    }
                } finally {
                    initLatch.countDown();
                }
            });
            System.out.println("Platform.runLater CALLED successfully");
        } catch (Exception e) {
            System.err.println("!!! ERROR calling Platform.runLater: " + e.getMessage());
            e.printStackTrace();
            initLatch.countDown();
        }

        // Monitor initialization in background thread
        new Thread(() -> {
            try {
                if (initLatch.await(5, TimeUnit.SECONDS)) {
                    System.out.println("initFX completed for: " + videoPath);
                } else {
                    System.err.println("!!! WARNING: initFX did NOT execute within 5 seconds for: " + videoPath);
                    System.err.println("!!! Platform.runLater may not be working. isInitialized=" + isInitialized + ", disposed=" + disposed);
                }
            } catch (InterruptedException e) {
                System.err.println("Wait interrupted");
            }
        }, "InitMonitor-" + videoPath).start();
    }

    /**
     * Initialize JavaFX components
     * Must be called on JavaFX Application Thread
     */
    private void initFX() {
        System.out.println("initFX for VideoPlayerPanel: " + videoPath);

        // Check if we're on the JavaFX Application Thread
        if (!Platform.isFxApplicationThread()) {
            System.err.println("WARNING: initFX called from non-FX thread, rescheduling...");
            Platform.runLater(this::initFX);
            return;
        }

        // Check if already disposed
        if (disposed) {
            System.out.println("VideoPlayerPanel already disposed, skipping initFX for: " + videoPath);
            return;
        }

        try {
            System.out.println("VideoPlayerPanel.initFX() starting for: " + videoPath);

            // Validate video file
            File videoFile = new File(videoPath);
            if (!videoFile.exists()) {
                System.err.println("Video file not found: " + videoPath);
                showError("Video file not found: " + videoPath);
                return;
            }

            System.out.println("Video file exists, creating Media object...");

            // Check again if disposed during file validation
            if (disposed) {
                System.out.println("VideoPlayerPanel disposed during initialization for: " + videoPath);
                return;
            }

            // Create Media and MediaPlayer
            String mediaUrl = videoFile.toURI().toString();
            System.out.println("Media URL: " + mediaUrl);
            Media media = new Media(mediaUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);

            System.out.println("MediaPlayer created successfully");

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
            System.out.println("VideoPlayerPanel initialized successfully, ready to play");

            // Handle media errors
            mediaPlayer.setOnError(() -> {
                String errorMsg = "Media error: " + mediaPlayer.getError().getMessage();
                System.err.println(errorMsg);
                showError(errorMsg);
            });

        } catch (Exception e) {
            System.err.println("Failed to initialize VideoPlayerPanel: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to load video: " + e.getMessage());
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
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseBtn.setText("▶");
            } else if (mediaPlayer != null) {
                mediaPlayer.play();
                playPauseBtn.setText("⏸");
            }
        });

        // Stop button
        Button stopBtn = new Button("⏹");
        stopBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                playPauseBtn.setText("▶");
            }
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
            if (!progressSlider.isValueChanging() && mediaPlayer != null) {
                Duration total = mediaPlayer.getTotalDuration();
                if (total != null && total.toMillis() > 0) {
                    progressSlider.setValue(newTime.toMillis() / total.toMillis() * 100);
                    timeLabel.setText(formatTime(newTime) + " / " + formatTime(total));
                }
            }
        });

        // Seek when slider is moved
        progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressSlider.isValueChanging() && mediaPlayer != null) {
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
     * Load a new video source without recreating the entire player
     * This reuses the existing MediaView and just swaps the MediaPlayer
     * @return true if video loaded successfully, false otherwise
     */
    public boolean loadVideo(String newVideoPath) {
        System.out.println("VideoPlayerPanel.loadVideo() called for: " + newVideoPath);

        if (disposed) {
            System.err.println("Cannot load video - panel is disposed");
            return false;
        }

        // Wait for initialization to complete (max 5 seconds)
        int attempts = 0;
        while (!isInitialized && attempts < 50) {
            try {
                Thread.sleep(100);
                attempts++;
            } catch (InterruptedException e) {
                System.err.println("Wait for initialization interrupted");
                return false;
            }
        }

        if (!isInitialized) {
            System.err.println("Cannot load video - panel initialization timed out");
            return false;
        }

        this.videoPath = newVideoPath;

        final boolean[] success = {false};
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                // Validate new video file
                File videoFile = new File(newVideoPath);
                if (!videoFile.exists()) {
                    System.err.println("Video file not found: " + newVideoPath);
                    showError("Video file not found: " + newVideoPath);
                    latch.countDown();
                    return;
                }

                // Dispose old MediaPlayer
                if (mediaPlayer != null) {
                    System.out.println("Disposing old MediaPlayer");
                    mediaPlayer.stop();
                    mediaPlayer.dispose();
                }

                // Create new MediaPlayer with new video
                String mediaUrl = videoFile.toURI().toString();
                System.out.println("Loading new video: " + mediaUrl);
                Media media = new Media(mediaUrl);
                mediaPlayer = new MediaPlayer(media);

                // Update the MediaView to use the new MediaPlayer
                mediaView.setMediaPlayer(mediaPlayer);

                // Handle media errors
                mediaPlayer.setOnError(() -> {
                    String errorMsg = "Media error: " + mediaPlayer.getError().getMessage();
                    System.err.println(errorMsg);
                    showError(errorMsg);
                });

                System.out.println("Video loaded successfully");
                success[0] = true;

            } catch (Exception e) {
                System.err.println("Failed to load video: " + e.getMessage());
                e.printStackTrace();
                showError("Failed to load video: " + e.getMessage());
                success[0] = false;
            } finally {
                latch.countDown();
            }
        });

        // Wait for video loading to complete
        try {
            latch.await(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("Video loading wait interrupted");
            return false;
        }

        return success[0];
    }

    /**
     * Release resources
     * IMPORTANT: Call this when disposing the panel
     */
    public void dispose() {
        System.out.println("VideoPlayerPanel.dispose() called for: " + videoPath);

        // Mark as disposed to prevent any pending initFX from running
        disposed = true;
        isInitialized = false;

        if (mediaPlayer != null) {
            final MediaPlayer playerToDispose = mediaPlayer;
            final JFXPanel panelToClean = fxPanel;
            mediaPlayer = null;

            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    System.out.println("Disposing MediaPlayer on FX thread");
                    playerToDispose.stop();
                    playerToDispose.dispose();

                    // Clear MediaView reference
                    if (mediaView != null) {
                        mediaView.setMediaPlayer(null);
                        mediaView = null;
                    }

                    // Also clear the scene
                    if (panelToClean != null) {
                        panelToClean.setScene(null);
                        System.out.println("JFXPanel scene cleared");
                    }

                    System.out.println("MediaPlayer disposed successfully");
                } catch (Exception e) {
                    System.err.println("Error during MediaPlayer disposal: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });

            // Wait for disposal to complete
            try {
                latch.await(1000, TimeUnit.MILLISECONDS);
                System.out.println("Disposal completed");
            } catch (InterruptedException e) {
                System.err.println("Disposal wait interrupted");
            }
        } else if (fxPanel != null) {
            // Even if no media player, still clear the scene
            Platform.runLater(() -> {
                try {
                    fxPanel.setScene(null);
                    System.out.println("JFXPanel scene cleared (no media player)");
                } catch (Exception e) {
                    System.err.println("Error clearing scene: " + e.getMessage());
                }
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
