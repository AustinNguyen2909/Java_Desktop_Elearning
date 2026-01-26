package com.elearning.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for handling video uploads and playback
 */
public class VideoUtil {
    private static final String VIDEO_STORAGE_BASE = "videos/";
    private static final String[] SUPPORTED_FORMATS = {".mp4", ".avi", ".mov", ".mkv", ".flv", ".wmv"};
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500 MB

    /**
     * Upload video file for a lesson
     * Copies the file to project video storage directory
     *
     * @param parent Parent component for dialogs
     * @param courseId Course ID for organizing files
     * @param lessonId Lesson ID for file naming
     * @return Absolute path to the stored video file, or null if upload failed
     */
    public static String uploadVideo(Component parent, int courseId, int lessonId) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Video File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName().toLowerCase();
                for (String format : SUPPORTED_FORMATS) {
                    if (name.endsWith(format)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Video Files (*.mp4, *.avi, *.mov, *.mkv, *.flv, *.wmv)";
            }
        });

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Validate file size
            if (selectedFile.length() > MAX_FILE_SIZE) {
                JOptionPane.showMessageDialog(parent,
                    "File size exceeds maximum allowed size (500 MB)",
                    "File Too Large",
                    JOptionPane.ERROR_MESSAGE);
                return null;
            }

            try {
                // Create directory structure: videos/course_X/
                String destDir = VIDEO_STORAGE_BASE + "course_" + courseId + "/";
                Path dirPath = Paths.get(destDir);
                Files.createDirectories(dirPath);

                // Generate unique filename with timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String extension = getFileExtension(selectedFile.getName());
                String filename = "lesson_" + lessonId + "_" + timestamp + extension;
                Path destination = Paths.get(destDir + filename);

                // Copy file to destination
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Return absolute path
                return destination.toAbsolutePath().toString();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(parent,
                    "Failed to upload video: " + e.getMessage(),
                    "Upload Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Update video for an existing lesson
     * Optionally deletes old video file
     *
     * @param parent Parent component for dialogs
     * @param courseId Course ID
     * @param lessonId Lesson ID
     * @param oldVideoPath Previous video path (can be null)
     * @return New video path, or null if update failed
     */
    public static String updateVideo(Component parent, int courseId, int lessonId, String oldVideoPath) {
        String newPath = uploadVideo(parent, courseId, lessonId);

        if (newPath != null && oldVideoPath != null && !oldVideoPath.isEmpty()) {
            // Optionally delete old video file
            int confirm = JOptionPane.showConfirmDialog(parent,
                "Delete old video file?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                deleteVideo(oldVideoPath);
            }
        }

        return newPath;
    }

    /**
     * Delete a video file from storage
     *
     * @param videoPath Absolute path to video file
     * @return true if deletion was successful
     */
    public static boolean deleteVideo(String videoPath) {
        if (videoPath == null || videoPath.isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(videoPath);
            if (Files.exists(path)) {
                Files.delete(path);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if a video file exists
     *
     * @param videoPath Absolute path to video file
     * @return true if file exists and is readable
     */
    public static boolean videoExists(String videoPath) {
        if (videoPath == null || videoPath.isEmpty()) {
            return false;
        }

        File videoFile = new File(videoPath);
        return videoFile.exists() && videoFile.isFile() && videoFile.canRead();
    }

    /**
     * Get video file information
     *
     * @param videoPath Absolute path to video file
     * @return Formatted string with file info, or error message
     */
    public static String getVideoInfo(String videoPath) {
        if (!videoExists(videoPath)) {
            return "Video file not found";
        }

        File videoFile = new File(videoPath);
        long sizeInBytes = videoFile.length();
        double sizeInMB = sizeInBytes / (1024.0 * 1024.0);

        return String.format("%s (%.2f MB)", videoFile.getName(), sizeInMB);
    }

    /**
     * Get file extension including the dot
     *
     * @param filename File name
     * @return Extension (e.g., ".mp4"), or empty string if no extension
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return "";
    }

    /**
     * Validate video format
     *
     * @param filename File name to check
     * @return true if format is supported
     */
    public static boolean isValidVideoFormat(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        String lowerName = filename.toLowerCase();
        for (String format : SUPPORTED_FORMATS) {
            if (lowerName.endsWith(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get video storage directory for a course
     *
     * @param courseId Course ID
     * @return Path to course video directory
     */
    public static String getCourseVideoDirectory(int courseId) {
        return VIDEO_STORAGE_BASE + "course_" + courseId + "/";
    }

    /**
     * Open video with system default player (fallback option)
     *
     * @param parent Parent component for error dialogs
     * @param videoPath Absolute path to video file
     */
    public static void openWithSystemPlayer(Component parent, String videoPath) {
        if (!videoExists(videoPath)) {
            JOptionPane.showMessageDialog(parent,
                "Video file not found: " + videoPath,
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            File videoFile = new File(videoPath);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(videoFile);
            } else {
                JOptionPane.showMessageDialog(parent,
                    "Desktop operations not supported on this system",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                "Failed to open video: " + e.getMessage(),
                "Playback Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Get supported video formats
     *
     * @return Array of supported format extensions
     */
    public static String[] getSupportedFormats() {
        return SUPPORTED_FORMATS.clone();
    }

    /**
     * Get maximum allowed file size in bytes
     *
     * @return Maximum file size
     */
    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * Format file size in human-readable format
     *
     * @param bytes File size in bytes
     * @return Formatted string (e.g., "150.5 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
