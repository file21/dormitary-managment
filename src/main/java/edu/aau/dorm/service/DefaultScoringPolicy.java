package edu.aau.dorm.service;

import edu.aau.dorm.model.DormApplication;
import edu.aau.dorm.model.Student;

/**
 * Simple scoring policy (adjust weights as needed).
 */
public final class DefaultScoringPolicy implements ScoringPolicy {

    @Override
    public int score(Student s, DormApplication a) {
        int score = 0;

        score += Math.min(s.yearOfStudy(), 6) * 5; // up to 30

        if (a.distanceKm() != null) {
            double d = a.distanceKm();
            if (d >= 300) score += 30;
            else if (d >= 150) score += 20;
            else if (d >= 50) score += 10;
        }

        if (a.disability()) score += 30;

        if (s.isPrivileged()) score += 15;

        return score;
    }
}
