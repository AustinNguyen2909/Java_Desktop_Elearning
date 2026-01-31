package com.elearning.service;

import com.elearning.dao.CertificateDAO;
import com.elearning.dao.CourseDAO;
import com.elearning.dao.UserDAO;
import com.elearning.model.Certificate;
import com.elearning.model.Course;
import com.elearning.model.User;
import com.elearning.util.CertificateImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for issuing and managing course certificates.
 */
public class CertificateService {
    private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final CertificateDAO certificateDAO;
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;

    private CertificateService() {
        this.certificateDAO = new CertificateDAO();
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
    }

    private static class SingletonHolder {
        private static final CertificateService INSTANCE = new CertificateService();
    }

    public static CertificateService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Certificate issueIfEligible(int userId, int courseId, double progressPercent) {
        if (progressPercent < 100.0) {
            return null;
        }

        User user = userDAO.findById(userId);
        if (user == null || !"USER".equals(user.getRole())) {
            return null;
        }

        Certificate existing = certificateDAO.findByUserAndCourse(userId, courseId);
        if (existing != null) {
            return existing;
        }

        Course course = courseDAO.findById(courseId);
        if (course == null) {
            return null;
        }

        LocalDate today = LocalDate.now();
        String code = String.format("CERT-%d-%d-%s", courseId, userId, today.format(CODE_DATE_FORMAT));
        Path imagePath = buildCertificatePath(code, today);

        if (!renderCertificateImage(imagePath, user.getFullName(), course.getTitle(), code, today)) {
            return null;
        }

        Certificate certificate = new Certificate();
        certificate.setUserId(userId);
        certificate.setCourseId(courseId);
        certificate.setCertificateCode(code);
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setFilePath(imagePath.toString());

        if (certificateDAO.insert(certificate)) {
            certificate.setCourseTitle(course.getTitle());
            return certificate;
        }

        return null;
    }

    /**
     * Issue a certificate for passing a test
     */
    public Certificate issueTestCertificate(int userId, int courseId, int testId, int attemptId, double scorePercentage) {
        // Check if certificate already exists
        Certificate existing = certificateDAO.findByUserAndCourse(userId, courseId);
        if (existing != null) {
            return existing; // Already has certificate
        }

        // Get user and course info
        User user = userDAO.findById(userId);
        Course course = courseDAO.findById(courseId);
        
        if (user == null || course == null || !"USER".equals(user.getRole())) {
            return null;
        }

        // Generate certificate
        LocalDate today = LocalDate.now();
        String code = String.format("CERT-%d-%d-%s", courseId, userId, today.format(CODE_DATE_FORMAT));
        Path imagePath = buildCertificatePath(code, today);

        // Render certificate image with test score
        if (!renderTestCertificateImage(imagePath, user.getFullName(), course.getTitle(), code, today, scorePercentage)) {
            return null;
        }

        // Create certificate record
        Certificate certificate = new Certificate();
        certificate.setUserId(userId);
        certificate.setCourseId(courseId);
        certificate.setTestId(testId);
        certificate.setAttemptId(attemptId);
        certificate.setCertificateCode(code);
        certificate.setStudentName(user.getFullName());
        certificate.setCourseTitle(course.getTitle());
        certificate.setScoreAchieved(scorePercentage);
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setFilePath(imagePath.toString());

        // Save to database
        if (certificateDAO.insert(certificate)) {
            return certificate;
        }

        return null;
    }

    public List<Certificate> getCertificatesForUser(int userId) {
        return certificateDAO.findByUserId(userId);
    }

    public Path ensureCertificateImage(Certificate certificate, String studentName) {
        if (certificate == null) {
            return null;
        }

        Path existingPath = certificate.getFilePath() != null ? Paths.get(certificate.getFilePath()) : null;
        if (existingPath != null && Files.exists(existingPath)) {
            return existingPath;
        }

        LocalDate issuedDate = certificate.getIssuedAt() != null
                ? certificate.getIssuedAt().toLocalDate()
                : LocalDate.now();
        Path targetPath = buildCertificatePath(certificate.getCertificateCode(), issuedDate);

        if (renderCertificateImage(targetPath, studentName, certificate.getCourseTitle(),
                certificate.getCertificateCode(), issuedDate)) {
            certificate.setFilePath(targetPath.toString());
            certificateDAO.updateFilePath(certificate.getId(), targetPath.toString());
            return targetPath;
        }

        return null;
    }

    private Path buildCertificatePath(String certificateCode, LocalDate issuedDate) {
        Path dir = Paths.get("certificates").toAbsolutePath();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String date = issuedDate != null ? issuedDate.format(FILE_DATE_FORMAT) : LocalDate.now().format(FILE_DATE_FORMAT);
        String safeName = certificateCode != null ? certificateCode : ("CERT-" + date);
        return dir.resolve(safeName + ".png");
    }

    private boolean renderCertificateImage(Path path, String studentName, String courseTitle, String code, LocalDate issuedDate) {
        String displayName = (studentName == null || studentName.isBlank()) ? "Student" : studentName.trim();
        String displayCourse = (courseTitle == null || courseTitle.isBlank()) ? "Course" : courseTitle.trim();

        BufferedImage image = CertificateImageUtil.renderCertificate(displayName, displayCourse, code, issuedDate);
        try {
            ImageIO.write(image, "png", path.toFile());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Render certificate image with test score information
     */
    private boolean renderTestCertificateImage(Path path, String studentName, String courseTitle, String code, LocalDate issuedDate, double scorePercentage) {
        String displayName = (studentName == null || studentName.isBlank()) ? "Student" : studentName.trim();
        String displayCourse = (courseTitle == null || courseTitle.isBlank()) ? "Course" : courseTitle.trim();

        // Use the existing certificate renderer but add score info to the course title
        String courseWithScore = displayCourse + " (Score: " + String.format("%.1f%%", scorePercentage) + ")";
        
        BufferedImage image = CertificateImageUtil.renderCertificate(displayName, courseWithScore, code, issuedDate);
        try {
            ImageIO.write(image, "png", path.toFile());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
