package com.elearning.ui.components;

import com.elearning.model.Lesson;
import com.elearning.model.User;
import com.elearning.service.LessonService;
import com.elearning.util.SessionManager;
import com.elearning.util.VideoUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Dialog for creating or editing a lesson
 */
public class LessonDialog extends JDialog {
    private final int courseId;
    private final Lesson lesson; // null if creating new
    private final User currentUser;
    private final LessonService lessonService;
    private boolean success = false;

    private JTextField titleField;
    private JTextArea descriptionArea;
    private JSpinner durationSpinner;
    private JCheckBox previewCheckbox;
    private JLabel videoInfoLabel;
    private String selectedVideoPath;

    public LessonDialog(Dialog owner, int courseId, Lesson lesson) {
        super(owner, lesson == null ? "Create New Lesson" : "Edit Lesson", true);
        this.courseId = courseId;
        this.lesson = lesson;
        this.currentUser = SessionManager.getInstance().getCurrentUser();
        this.lessonService = new LessonService();

        initComponents();
        if (lesson != null) {
            loadLessonData();
        }
        setSize(500, 600);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        titleField = new JTextField();
        panel.add(titleField, gbc);

        // Duration
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Duration (min):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        durationSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 600, 1));
        panel.add(durationSpinner, gbc);

        // Preview
        gbc.gridx = 1; gbc.gridy = 2;
        previewCheckbox = new JCheckBox("Allow Preview");
        previewCheckbox.setBackground(Color.WHITE);
        panel.add(previewCheckbox, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(descriptionArea), gbc);

        // Video
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Video:"), gbc);
        gbc.gridx = 1;
        JPanel videoPanel = new JPanel(new BorderLayout(10, 0));
        videoPanel.setBackground(Color.WHITE);
        videoInfoLabel = new JLabel("No video selected");
        videoPanel.add(videoInfoLabel, BorderLayout.CENTER);
        JButton chooseBtn = new JButton("Select Video");
        chooseBtn.addActionListener(e -> selectVideo());
        videoPanel.add(chooseBtn, BorderLayout.EAST);
        panel.add(videoPanel, gbc);

        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(248, 249, 250));
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveLesson());
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void selectVideo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mov");
            }
            public String getDescription() { return "Video Files (*.mp4, *.avi, *.mov)"; }
        });

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedVideoPath = selectedFile.getAbsolutePath();
            videoInfoLabel.setText(selectedFile.getName());
        }
    }

    private void loadLessonData() {
        titleField.setText(lesson.getTitle());
        descriptionArea.setText(lesson.getDescription());
        durationSpinner.setValue(lesson.getDurationMinutes());
        previewCheckbox.setSelected(lesson.isPreview());
        if (lesson.getVideoPath() != null) {
            videoInfoLabel.setText(new File(lesson.getVideoPath()).getName());
        }
    }

    private void saveLesson() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title is required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Lesson l = lesson != null ? lesson : new Lesson();
        l.setCourseId(courseId);
        l.setTitle(title);
        l.setDescription(descriptionArea.getText().trim());
        l.setDurationMinutes((Integer) durationSpinner.getValue());
        l.setPreview(previewCheckbox.isSelected());

        try {
            boolean result;
            if (lesson == null) {
                result = lessonService.createLesson(l, currentUser.getId(), currentUser.getRole());
            } else {
                result = lessonService.updateLesson(l, currentUser.getId(), currentUser.getRole());
            }

            if (result) {
                // If a new video was selected, upload it
                if (selectedVideoPath != null) {
                    // Refresh lesson object to get generated ID if it was null (new lesson)
                    if (lesson == null) {
                        List<Lesson> lessons = lessonService.getCourseLessons(courseId, currentUser.getRole(), currentUser.getId(), false);
                        // Find the one we just created (matching title and order index, or just the last one)
                        l = lessons.stream()
                                .filter(ls -> ls.getTitle().equals(title))
                                .findFirst()
                                .orElse(l);
                    }
                    
                    String uploadedPath = VideoUtil.uploadVideo(this, courseId, l.getId());
                    if (uploadedPath != null) {
                        l.setVideoPath(uploadedPath);
                        lessonService.updateLesson(l, currentUser.getId(), currentUser.getRole());
                    }
                }
                success = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save lesson", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() { return success; }
}
