package edu.aau.dorm.service;

import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.model.Student;

/**
 * Strategy interface: scoring rules can change without rewriting the system.
 */
public interface ScoringPolicy {
    int score(Student student, DormApplication application);
}
