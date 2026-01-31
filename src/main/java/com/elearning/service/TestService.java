package com.elearning.service;

import com.elearning.dao.AnswerOptionDAO;
import com.elearning.dao.CourseDAO;
import com.elearning.dao.CourseTestDAO;
import com.elearning.dao.TestQuestionDAO;
import com.elearning.model.AnswerOption;
import com.elearning.model.Course;
import com.elearning.model.CourseTest;
import com.elearning.model.TestQuestion;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for test management operations
 * Singleton pattern for single instance across application
 */
public class TestService {
    private final CourseTestDAO testDAO;
    private final TestQuestionDAO questionDAO;
    private final AnswerOptionDAO optionDAO;
    private final CourseDAO courseDAO;

    // Private constructor to prevent direct instantiation
    private TestService() {
        this.testDAO = new CourseTestDAO();
        this.questionDAO = new TestQuestionDAO();
        this.optionDAO = new AnswerOptionDAO();
        this.courseDAO = new CourseDAO();
    }

    // Static inner holder class - lazily loaded and thread-safe
    private static class SingletonHolder {
        private static final TestService INSTANCE = new TestService();
    }

    // Public accessor method
    public static TestService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Create a new test for a course (instructor only)
     */
    public CourseTest createTest(CourseTest test, int instructorId) {
        // Verify ownership
        Course course = courseDAO.findById(test.getCourseId());
        if (course == null || !course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("You can only create tests for your own courses");
        }

        // Check if test already exists for this course
        if (testDAO.existsByCourseId(test.getCourseId())) {
            throw new IllegalStateException("A test already exists for this course");
        }

        return testDAO.create(test);
    }

    /**
     * Update an existing test (instructor only)
     */
    public boolean updateTest(CourseTest test, int instructorId) {
        // Verify ownership
        Course course = courseDAO.findById(test.getCourseId());
        if (course == null || !course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("You can only update tests for your own courses");
        }

        return testDAO.update(test);
    }

    /**
     * Delete a test (instructor only)
     */
    public boolean deleteTest(int testId, int instructorId) {
        CourseTest test = testDAO.findById(testId);
        if (test == null) {
            return false;
        }

        // Verify ownership
        Course course = courseDAO.findById(test.getCourseId());
        if (course == null || !course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("You can only delete tests for your own courses");
        }

        return testDAO.delete(testId);
    }

    /**
     * Get test by course ID
     */
    public CourseTest getTestByCourseId(int courseId) {
        return testDAO.findByCourseId(courseId);
    }

    /**
     * Get test with all questions and options loaded
     */
    public CourseTest getTestWithQuestions(int testId) {
        CourseTest test = testDAO.findById(testId);
        if (test == null) {
            return null;
        }

        // Load questions
        List<TestQuestion> questions = questionDAO.findByTestId(testId);

        // Load options for each question
        for (TestQuestion question : questions) {
            List<AnswerOption> options = optionDAO.findByQuestionId(question.getId());
            question.setOptions(options);
        }

        // Set question count and total points
        test.setQuestionCount(questions.size());
        test.setTotalPoints((int) questionDAO.getTotalPoints(testId));

        return test;
    }

    /**
     * Add a question to a test (instructor only)
     */
    public TestQuestion addQuestion(TestQuestion question, int instructorId) {
        // Verify ownership
        CourseTest test = testDAO.findById(question.getTestId());
        if (test == null) {
            throw new IllegalArgumentException("Test not found");
        }

        Course course = courseDAO.findById(test.getCourseId());
        if (course == null || !course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("You can only add questions to your own course tests");
        }

        // Set order index if not set
        if (question.getOrderIndex() == null || question.getOrderIndex() == 0) {
            question.setOrderIndex(questionDAO.getNextOrderIndex(question.getTestId()));
        }

        return questionDAO.create(question);
    }

    /**
     * Add answer options to a question (instructor only)
     */
    public List<AnswerOption> addOptions(int questionId, List<AnswerOption> options, int instructorId) {
        // Verify ownership
        TestQuestion question = questionDAO.findById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Question not found");
        }

        CourseTest test = testDAO.findById(question.getTestId());
        Course course = courseDAO.findById(test.getCourseId());
        if (course == null || !course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("You can only add options to your own course tests");
        }

        // Validate: must have exactly 4 options
        if (options.size() != 4) {
            throw new IllegalArgumentException("Must provide exactly 4 options (A, B, C, D)");
        }

        // Validate: exactly 1 must be correct
        long correctCount = options.stream().filter(AnswerOption::getIsCorrect).count();
        if (correctCount != 1) {
            throw new IllegalArgumentException("Exactly one option must be marked as correct");
        }

        // Create all options
        List<AnswerOption> createdOptions = new ArrayList<>();
        for (AnswerOption option : options) {
            option.setQuestionId(questionId);
            AnswerOption created = optionDAO.create(option);
            if (created != null) {
                createdOptions.add(created);
            }
        }

        return createdOptions;
    }

    /**
     * Publish a test (make it visible to students)
     */
    public boolean publishTest(int testId, int instructorId) {
        // Verify ownership
        CourseTest test = testDAO.findById(testId);
        if (test == null) {
            return false;
        }

        Course course = courseDAO.findById(test.getCourseId());
        if (course == null || !course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("You can only publish your own course tests");
        }

        // Validate test before publishing
        if (!validateTest(testId)) {
            throw new IllegalStateException("Cannot publish test: validation failed");
        }

        return testDAO.publish(testId);
    }

    /**
     * Unpublish a test (hide from students)
     */
    public boolean unpublishTest(int testId, int instructorId) {
        // Verify ownership
        CourseTest test = testDAO.findById(testId);
        if (test == null) {
            return false;
        }

        Course course = courseDAO.findById(test.getCourseId());
        if (course == null || !course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("You can only unpublish your own course tests");
        }

        return testDAO.unpublish(testId);
    }

    /**
     * Validate test before publishing
     */
    public boolean validateTest(int testId) {
        // Must have at least 1 question
        List<TestQuestion> questions = questionDAO.findByTestId(testId);
        if (questions.isEmpty()) {
            return false;
        }

        // Each question must have exactly 4 options and 1 correct option
        for (TestQuestion question : questions) {
            if (!optionDAO.validateQuestion(question.getId())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Delete a question (instructor only)
     */
    public boolean deleteQuestion(int questionId, int instructorId) {
        // Verify ownership
        TestQuestion question = questionDAO.findById(questionId);
        if (question == null) {
            return false;
        }

        CourseTest test = testDAO.findById(question.getTestId());
        Course course = courseDAO.findById(test.getCourseId());
        if (course == null || !course.getInstructorId().equals(instructorId)) {
            throw new SecurityException("You can only delete questions from your own course tests");
        }

        return questionDAO.delete(questionId);
    }

    /**
     * Get all questions for a test
     */
    public List<TestQuestion> getQuestions(int testId) {
        List<TestQuestion> questions = questionDAO.findByTestId(testId);

        // Load options for each question
        for (TestQuestion question : questions) {
            List<AnswerOption> options = optionDAO.findByQuestionId(question.getId());
            question.setOptions(options);
        }

        return questions;
    }

    /**
     * Check if test exists for a course
     */
    public boolean hasTest(int courseId) {
        return testDAO.existsByCourseId(courseId);
    }
}
