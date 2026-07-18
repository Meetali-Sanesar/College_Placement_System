package com.yourname.campusplacement.entity;

/**
 * Status of a placement drive as managed by the Placement Cell.
 * UPCOMING  → drive is scheduled but not yet accepting applications
 * OPEN      → students can apply
 * CLOSED    → applications are closed
 */
public enum DriveStatus {
    UPCOMING, OPEN, CLOSED
}
