package edu.aau.dorm.dao;

import edu.aau.dorm.model.ApplicationStatus;
import edu.aau.dorm.model.DormApplication;

import java.time.Instant;
import java.util.List;

public interface ApplicationDao {
    DormApplication getById(long id);
    List<DormApplication> findAll();
    DormApplication create(DormApplication application);
    void update(DormApplication application);
    void delete(long id);
    void markSubmitted(long id, int score, Instant submittedAt);
    void setStatus(long id, ApplicationStatus status);
}
