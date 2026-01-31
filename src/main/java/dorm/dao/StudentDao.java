package dorm.dao;

import dorm.model.Student;

public interface StudentDao {
    Student getStudentByUserId(long userId);
}
