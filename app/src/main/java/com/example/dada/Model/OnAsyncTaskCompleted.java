/* OnAsyncTaskCompleted interface
 *
 * Version 1.0
 *
 * March 15, 2018
 *
 * Copyright (c) 2018 Team 12 CMPUT 301. University of Alberta - All Rights Reserved.
 * You may use distribute or modify this code under terms and condition of the Code of Student Behaviour at University of Alberta.
 * You can find a copy of licence in this project. Otherwise please contact contact sfeng3@ualberta.ca.
 */

package com.example.dada.Model;

/**
 * The interface On async task completed, will be
 * called after the async event is done.
 */
public interface OnAsyncTaskCompleted {
    /**
     * On task completed.
     *
     * @param o the object will be return by the asynctask
     */
    void onTaskCompleted(Object o);
}
