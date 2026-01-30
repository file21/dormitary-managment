package edu.aau.dorm.service;

import java.time.Instant;

public interface ApplicationWindowService {
    boolean isWindowOpen(String windowCode, Instant now);
    boolean isWindowClosed(String windowCode, Instant now);
}
