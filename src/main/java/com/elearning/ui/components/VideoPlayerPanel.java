package com.elearning.ui.components;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;

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
    private Stage fullscreenStage = null;
    private BorderPane originalParent = null;
    private boolean isFullscreen = false;
    private HBox fullscreenControlsRef = null;
    private Timeline controlsHideTimer = null;

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
        Button playPauseBtn = new Button("\u25B6");
        playPauseBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #3498db; -fx-text-fill: white;");
        playPauseBtn.setOnAction(e -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseBtn.setText("\u23F8");
            } else if (mediaPlayer != null) {
                mediaPlayer.play();
                playPauseBtn.setText("\u23F8");
            }
        });

        // Stop button
        Button stopBtn = new Button("\u23F9");
        stopBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                playPauseBtn.setText("\u25B6");
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
        Label volumeLabel = new Label("\uD83D\uDD0A");
        volumeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));

        // Fullscreen button
        Button fullscreenBtn = new Button("\u26F6");
        fullscreenBtn.setStyle("-fx-font-size: 16px; -fx-background-color: #9b59b6; -fx-text-fill: white;");
        fullscreenBtn.setTooltip(new Tooltip("Fullscreen (F11 or Esc to exit)"));
        fullscreenBtn.setOnAction(e -> toggleFullscreen());

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
                volumeSlider,
                fullscreenBtn
        );

        return controls;
    }

    /**
     * Create fullscreen controls (similar to regular controls but with exit fullscreen button)
     */
    private HBox createFullscreenControls() {
        HBox controls = new HBox(10);
        controls.setStyle("-fx-background-color: rgba(44, 62, 80, 0.8); -fx-padding: 15;");

        // Play/Pause button
        Button playPauseBtn = new Button("\u25B6");
        playPauseBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-width: 50; -fx-pref-height: 40;");
        playPauseBtn.setOnAction(e -> {
            if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseBtn.setText("\u25B6");
            } else if (mediaPlayer != null) {
                mediaPlayer.play();
                playPauseBtn.setText("\u23F8");
            }
        });

        // Stop button
        Button stopBtn = new Button("\u23F9");
        stopBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-pref-width: 50; -fx-pref-height: 40;");
        stopBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                playPauseBtn.setText("\u25B6");
            }
        });

        // Time label
        Label timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // Progress slider
        Slider progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(100);
        progressSlider.setValue(0);
        progressSlider.setPrefWidth(300);
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
        volumeSlider.setPrefWidth(120);
        volumeSlider.setStyle("-fx-background-color: transparent;");
        Label volumeLabel = new Label("\uD83D\uDD0A");
        volumeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        mediaPlayer.volumeProperty().bind(volumeSlider.valueProperty().divide(100));

        // Exit fullscreen button
        Button exitFullscreenBtn = new Button("\u26F6");
        exitFullscreenBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #e67e22; -fx-text-fill: white; -fx-pref-width: 50; -fx-pref-height: 40;");
        exitFullscreenBtn.setTooltip(new Tooltip("Exit Fullscreen (ESC)"));
        exitFullscreenBtn.setOnAction(e -> exitFullscreen());

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
                volumeSlider,
                exitFullscreenBtn
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
     * Toggle fullscreen mode
     */
    private void toggleFullscreen() {
        if (mediaView == null || mediaPlayer == null) {
            return;
        }

        if (isFullscreen) {
            exitFullscreen();
        } else {
            enterFullscreen();
        }
    }

    /**
     * Enter fullscreen mode
     */
    private void enterFullscreen() {
        if (isFullscreen || mediaView == null) {
            return;
        }

        Platform.runLater(() -> {
            try {
                // Store reference to original parent
                originalParent = (BorderPane) mediaView.getParent();
                
                // Remove media view from current parent
                if (originalParent != null) {
                    originalParent.setCenter(null);
                }

                // Create fullscreen stage
                fullscreenStage = new Stage();
                fullscreenStage.initStyle(StageStyle.UNDECORATED);
                fullscreenStage.setFullScreen(true);
                fullscreenStage.setTitle("Video Player - Fullscreen");

                // Create fullscreen layout
                BorderPane fullscreenRoot = new BorderPane();
                fullscreenRoot.setStyle("-fx-background-color: black;");
                
                // Create media container for fullscreen
                BorderPane mediaContainer = new BorderPane();
                mediaContainer.setStyle("-fx-background-color: black;");
                mediaContainer.setCenter(mediaView);
                
                // Add media container to fullscreen layout
                fullscreenRoot.setCenter(mediaContainer);
                
                // Create fullscreen controls
                HBox fullscreenControls = createFullscreenControls();
                fullscreenControlsRef = fullscreenControls;
                fullscreenRoot.setBottom(fullscreenControls);
                
                // Bind media view size to media container
                mediaView.fitWidthProperty().bind(mediaContainer.widthProperty());
                mediaView.fitHeightProperty().bind(mediaContainer.heightProperty());

                // Create fullscreen scene
                Scene fullscreenScene = new Scene(fullscreenRoot);
                fullscreenStage.setScene(fullscreenScene);

                // Add key handlers for fullscreen exit
                fullscreenScene.setOnKeyPressed(this::handleFullscreenKeyPress);
                
                // Add mouse movement handler for auto-hide controls
                fullscreenScene.setOnMouseMoved(this::handleMouseMovement);
                fullscreenScene.setOnMouseClicked(this::handleMouseClick);
                
                // Start auto-hide timer
                startControlsHideTimer();

                // Handle stage close
                fullscreenStage.setOnCloseRequest(e -> exitFullscreen());

                // Show fullscreen stage
                fullscreenStage.show();
                isFullscreen = true;

                System.out.println("Entered fullscreen mode");

            } catch (Exception e) {
                System.err.println("Error entering fullscreen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Exit fullscreen mode
     */
    private void exitFullscreen() {
        if (!isFullscreen || fullscreenStage == null) {
            return;
        }

        Platform.runLater(() -> {
            try {
                // Stop controls hide timer
                if (controlsHideTimer != null) {
                    controlsHideTimer.stop();
                    controlsHideTimer = null;
                }
                
                // Remove media view from fullscreen stage
                BorderPane fullscreenRoot = (BorderPane) fullscreenStage.getScene().getRoot();
                fullscreenRoot.setCenter(null);

                // Close fullscreen stage
                fullscreenStage.close();
                fullscreenStage = null;
                fullscreenControlsRef = null;

                // Restore media view to original parent
                if (originalParent != null && mediaView != null) {
                    originalParent.setCenter(mediaView);
                    
                    // Restore original bindings
                    mediaView.fitWidthProperty().bind(originalParent.widthProperty());
                    mediaView.fitHeightProperty().bind(originalParent.heightProperty());
                }

                isFullscreen = false;
                System.out.println("Exited fullscreen mode");

            } catch (Exception e) {
                System.err.println("Error exiting fullscreen: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Handle key presses in fullscreen mode
     */
    private void handleFullscreenKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.F11) {
            exitFullscreen();
        } else if (event.getCode() == KeyCode.SPACE) {
            // Toggle play/pause with spacebar
            if (mediaPlayer != null) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.play();
                }
            }
        }
        
        // Show controls on any key press
        showControls();
    }

    /**
     * Handle mouse movement in fullscreen mode
     */
    private void handleMouseMovement(MouseEvent event) {
        showControls();
    }

    /**
     * Handle mouse clicks in fullscreen mode
     */
    private void handleMouseClick(MouseEvent event) {
        showControls();
    }

    /**
     * Show fullscreen controls and restart hide timer
     */
    private void showControls() {
        if (fullscreenControlsRef != null && isFullscreen) {
            // Stop any existing hide timer
            if (controlsHideTimer != null) {
                controlsHideTimer.stop();
            }
            
            // Show controls
            fullscreenControlsRef.setVisible(true);
            fullscreenControlsRef.setOpacity(1.0);
            
            // Start new hide timer
            startControlsHideTimer();
        }
    }

    /**
     * Start timer to auto-hide controls after 3 seconds of inactivity
     */
    private void startControlsHideTimer() {
        if (controlsHideTimer != null) {
            controlsHideTimer.stop();
        }
        
        controlsHideTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> hideControls()));
        controlsHideTimer.play();
    }

    /**
     * Hide fullscreen controls with fade animation
     */
    private void hideControls() {
        if (fullscreenControlsRef != null && isFullscreen) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), fullscreenControlsRef);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> fullscreenControlsRef.setVisible(false));
            fadeOut.play();
        }
    }

    /**
     * Check if currently in fullscreen mode
     */
    public boolean isFullscreen() {
        return isFullscreen;
    }

    /**
     * Release resources
     * IMPORTANT: Call this when disposing the panel
     */
    public void dispose() {
        System.out.println("VideoPlayerPanel.dispose() called for: " + videoPath);

        // Exit fullscreen if currently in fullscreen mode
        if (isFullscreen) {
            exitFullscreen();
        }

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
