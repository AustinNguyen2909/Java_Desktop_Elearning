package com.elearning.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.*;

/**
 * Utility class for file operations, especially video uploads
 */
public class FileUtil {
    private static final String VIDEO_BASE_PATH = "videos/";
    private static final String[] VIDEO_EXTENSIONS = {"mp4", "avi", "mkv", "mov", "wmv", "flv"};
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

    /**
     * Open file chooser and upload video to course folder
     */
    public static String uploadVideo(int courseId, int lessonOrder) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Video File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Video Files", VIDEO_EXTENSIONS));

        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File sourceFile = fileChooser.getSelectedFile();
        String extension = getFileExtension(sourceFile.getName());

        // Create destination path
        String destFolder = VIDEO_BASE_PATH + "course_" + courseId + "/";
        String destFileName = "lesson_" + lessonOrder + "." + extension;
        String destPath = destFolder + destFileName;

        try {
            // Create directory if not exists
            Files.createDirectories(Paths.get(destFolder));

            // Copy file to destination
            Files.copy(sourceFile.toPath(), Paths.get(destPath),
                    StandardCopyOption.REPLACE_EXISTING);

            return destPath;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to upload video: " + e.getMessage(),
                    "Upload Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Upload thumbnail image for course
     */
    public static String uploadThumbnail(int courseId) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Thumbnail Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", IMAGE_EXTENSIONS));

        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File sourceFile = fileChooser.getSelectedFile();
        String extension = getFileExtension(sourceFile.getName());

        // Create destination path
        String destFolder = VIDEO_BASE_PATH + "course_" + courseId + "/";
        String destFileName = "thumbnail." + extension;
        String destPath = destFolder + destFileName;

        try {
            Files.createDirectories(Paths.get(destFolder));
            Files.copy(sourceFile.toPath(), Paths.get(destPath),
                    StandardCopyOption.REPLACE_EXISTING);
            return destPath;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to upload thumbnail: " + e.getMessage(),
                    "Upload Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }

    /**
     * Check if file exists
     */
    public static boolean fileExists(String filePath) {
        return filePath != null && Files.exists(Paths.get(filePath));
    }

    /**
     * Delete file
     */
    public static boolean deleteFile(String filePath) {
        try {
            if (filePath != null) {
                Files.deleteIfExists(Paths.get(filePath));
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get file size in MB
     */
    public static double getFileSizeMB(String filePath) {
        try {
            long bytes = Files.size(Paths.get(filePath));
            return bytes / (1024.0 * 1024.0);
        } catch (IOException e) {
            return 0;
        }
    }
}
