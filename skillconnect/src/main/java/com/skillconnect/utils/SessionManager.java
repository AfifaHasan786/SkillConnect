package com.skillconnect.utils;

import com.skillconnect.models.User;

/**
 * Manages user session information throughout the application.
 * Implements the Singleton pattern to ensure only one instance exists.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private static final Object lock = new Object();

    private SessionManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Gets the singleton instance of SessionManager.
     * @return The SessionManager instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Sets the current user for the session.
     * @param user The user to set as current
     */
    public void setCurrentUser(User user) {
        synchronized (lock) {
            if (user == null) {
                System.out.println("Warning: Attempting to set null user in session");
                return;
            }
            System.out.println("Setting session user: " + user.getUsername() + " (ID: " + user.getId() + ")");
            this.currentUser = user;
        }
    }

    /**
     * Gets the current user from the session.
     * @return The current User object, or null if no user is logged in
     */
    public User getCurrentUser() {
        synchronized (lock) {
            if (currentUser == null) {
                System.out.println("Warning: Attempting to get user from session but no user is logged in");
                return null;
            }
            System.out.println("Getting session user: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
            return currentUser;
        }
    }

    /**
     * Checks if a user is currently logged in.
     * @return true if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        synchronized (lock) {
            boolean loggedIn = currentUser != null;
            System.out.println("Checking login status: " + (loggedIn ? "logged in" : "not logged in"));
            if (loggedIn) {
                System.out.println("Current session user: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
            }
            return loggedIn;
        }
    }

    /**
     * Clears the current session.
     */
    public void clearSession() {
        synchronized (lock) {
            if (currentUser != null) {
                System.out.println("Clearing session for user: " + currentUser.getUsername() + " (ID: " + currentUser.getId() + ")");
            }
            currentUser = null;
        }
    }
}