package com.dkprojects.dkklan.war;

public enum WarState {
    WAITING,    // Invite sent
    PREPARING,  // Lobby countdown
    STARTED,    // Fighting
    ENDED,      // Finished
    CANCELLED   // Aborted
}
