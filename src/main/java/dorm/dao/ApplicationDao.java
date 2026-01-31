package dorm.dao;

import dorm.model.ApplicationStatus;
import dorm.model.DormApplication;

import java.time.Instant;

public interface ApplicationDao {
    DormApplication getById(long id);
    void markSubmitted(long id, int score, Instant submittedAt);
    void setStatus(long id, ApplicationStatus status);
}
