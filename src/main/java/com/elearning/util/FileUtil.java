package com.elearning.util;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for file operations, including hero image and video uploads
 */
public class FileUtil {
    private static final String VIDEO_BASE_PATH = "videos/";
    private static final String THUMBNAIL_BASE_PATH = "thumbnails/";
    private static final String[] VIDEO_EXTENSIONS = {"mp4", "avi", "mkv", "mov", "wmv", "flv"};
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

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
     * Upload hero image (thumbnail) for course
     * Stores in thumbnails/course_X/ directory with timestamp-based naming
     *
     * @param parent Parent component for dialogs
     * @param courseId Course ID for organizing files
     * @return Absolute path to the stored image file, or null if upload failed
     */
    public static String uploadThumbnail(Component parent, int courseId) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Course Hero Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", IMAGE_EXTENSIONS));

        int result = fileChooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File sourceFile = fileChooser.getSelectedFile();

        // Validate file size (max 5 MB)
        if (sourceFile.length() > MAX_IMAGE_SIZE) {
            JOptionPane.showMessageDialog(parent,
                String.format("Image file too large. Maximum size: 5 MB\nCurrent size: %.2f MB",
                    sourceFile.length() / (1024.0 * 1024.0)),
                "File Too Large",
                JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            // Create directory structure: thumbnails/course_X/
            String destDir = THUMBNAIL_BASE_PATH + "course_" + courseId + "/";
            Path dirPath = Paths.get(destDir);
            Files.createDirectories(dirPath);

            // Generate unique filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String extension = getFileExtension(sourceFile.getName());
            String filename = "course_" + courseId + "_" + timestamp + "." + extension;
            Path destination = Paths.get(destDir + filename);

            // Copy file to destination
            Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Return absolute path
            return destination.toAbsolutePath().toString();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                "Failed to upload hero image: " + e.getMessage(),
                "Upload Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Upload hero image for temporary course (before course ID is assigned)
     * Uses temp directory
     *
     * @param parent Parent component for dialogs
     * @return Absolute path to the stored image file, or null if upload failed
     */
    public static String uploadThumbnailTemp(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Course Hero Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", IMAGE_EXTENSIONS));

        int result = fileChooser.showOpenDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File sourceFile = fileChooser.getSelectedFile();

        // Validate file size (max 5 MB)
        if (sourceFile.length() > MAX_IMAGE_SIZE) {
            JOptionPane.showMessageDialog(parent,
                String.format("Image file too large. Maximum size: 5 MB\nCurrent size: %.2f MB",
                    sourceFile.length() / (1024.0 * 1024.0)),
                "File Too Large",
                JOptionPane.ERROR_MESSAGE);
            return null;
        }

        try {
            // Create temp directory
            String destDir = THUMBNAIL_BASE_PATH + "temp/";
            Path dirPath = Paths.get(destDir);
            Files.createDirectories(dirPath);

            // Generate unique filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String extension = getFileExtension(sourceFile.getName());
            String filename = "course_temp_" + timestamp + "." + extension;
            Path destination = Paths.get(destDir + filename);

            // Copy file to destination
            Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Return absolute path
            return destination.toAbsolutePath().toString();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                "Failed to upload hero image: " + e.getMessage(),
                "Upload Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Move thumbnail from temp directory to course directory
     *
     * @param tempPath Temporary file path
     * @param courseId Course ID
     * @return New absolute path, or null if move failed
     */
    public static String moveThumbnailFromTemp(String tempPath, int courseId) {
        if (tempPath == null || tempPath.isEmpty()) {
            return null;
        }

        try {
            File tempFile = new File(tempPath);
            if (!tempFile.exists()) {
                return null;
            }

            // Create destination directory
            String destDir = THUMBNAIL_BASE_PATH + "course_" + courseId + "/";
            Files.createDirectories(Paths.get(destDir));

            // Generate new filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String extension = getFileExtension(tempFile.getName());
            String filename = "course_" + courseId + "_" + timestamp + "." + extension;
            Path destination = Paths.get(destDir + filename);

            // Move file
            Files.move(tempFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            return destination.toAbsolutePath().toString();

        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * Get thumbnail storage directory for a course
     *
     * @param courseId Course ID
     * @return Path to course thumbnail directory
     */
    public static String getCourseThumbnailDirectory(int courseId) {
        return THUMBNAIL_BASE_PATH + "course_" + courseId + "/";
    }

    /**
     * Validate image format
     *
     * @param filename File name to check
     * @return true if format is supported
     */
    public static boolean isValidImageFormat(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        String lowerName = filename.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lowerName.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get image file information
     *
     * @param imagePath Absolute path to image file
     * @return Formatted string with file info, or error message
     */
    public static String getImageInfo(String imagePath) {
        if (!fileExists(imagePath)) {
            return "Image file not found";
        }

        File imageFile = new File(imagePath);
        long sizeInBytes = imageFile.length();
        double sizeInMB = sizeInBytes / (1024.0 * 1024.0);

        return String.format("%s (%.2f MB)", imageFile.getName(), sizeInMB);
    }

    /**
     * Delete thumbnail image
     *
     * @param thumbnailPath Absolute path to thumbnail file
     * @return true if deletion was successful
     */
    public static boolean deleteThumbnail(String thumbnailPath) {
        return deleteFile(thumbnailPath);
    }

    /**
     * Check if image file size is within limit
     *
     * @param filePath Path to image file
     * @return true if size is within limit
     */
    public static boolean isImageSizeValid(String filePath) {
        try {
            long bytes = Files.size(Paths.get(filePath));
            return bytes <= MAX_IMAGE_SIZE;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get supported image formats
     *
     * @return Array of supported image format extensions
     */
    public static String[] getSupportedImageFormats() {
        return IMAGE_EXTENSIONS.clone();
    }

    /**
     * Get maximum allowed image size in bytes
     *
     * @return Maximum image file size
     */
    public static long getMaxImageSize() {
        return MAX_IMAGE_SIZE;
    }
}
