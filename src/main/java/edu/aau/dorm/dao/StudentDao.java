package edu.aau.dorm.dao;

import edu.aau.dorm.model.Student;

public interface StudentDao {
    Student getStudentByUserId(long userId);
}
