/* TaskController
 *
 * Version 1.0
 *
 * March 15, 2018
 *
 * Copyright (c) 2018 Team 12 CMPUT 301. University of Alberta - All Rights Reserved.
 * You may use distribute or modify this code under terms and condition of the Code of Student Behaviour at University of Alberta.
 * You can find a copy of licence in this project. Otherwise please contact contact sfeng3@ualberta.ca.
 */

package com.example.dada.Controller;

import android.content.Context;
import android.util.Log;

import com.example.dada.Exception.TaskException;
import com.example.dada.Model.OnAsyncTaskCompleted;
import com.example.dada.Model.OnAsyncTaskFailure;
import com.example.dada.Model.Task.NormalTask;
import com.example.dada.Model.Task.Task;
import com.example.dada.Model.User;
import com.example.dada.Util.FileIOUtil;
import com.example.dada.Util.TaskUtil;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Request model's controller, a glue between Activity and Model. Give access for activity(View) to
 * modify and update model.
 */
public class TaskController {

    /**
     * The Listener, callback method when the async task is done
     */
    public OnAsyncTaskCompleted listener;
    public OnAsyncTaskFailure offlineHandler;

    /**
     * Instantiates a new Request controller.
     *
     * @param listener the listener, custom callback method
     */
    public TaskController(OnAsyncTaskCompleted listener) {
        this.listener = listener;
    }

    public TaskController(OnAsyncTaskCompleted listener, OnAsyncTaskFailure offlineHandler) {
        this.listener = listener;
        this.offlineHandler = offlineHandler;
    }

    /**
     * Create a new task and send it to the server
     *
     * @param task The request to be created
     */
    public void createTask(Task task) {
        Task.CreateTaskTask t = new Task.CreateTaskTask(listener, offlineHandler);
        try {
            t.execute(task);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Update a task
     *
     * @param task The task to be updated
     */
    public void updateTask(Task task) {
        Task.UpdateTaskTask t = new Task.UpdateTaskTask(listener, offlineHandler);
        t.execute(task);
    }

    /**
     * Delete a task
     *
     * @param task The task to be deleted
     */
    public void deleteTask(Task task) {
        Task.DeleteTaskTask t = new Task.DeleteTaskTask(listener);
        t.execute(task);
    }

    /**
     * Get a list of all tasks
     *
     * @return An ArrayList of tasks
     */
    public ArrayList<NormalTask> getAllTask() {
        Task.GetTasksListTask t = new Task.GetTasksListTask(listener);
        t.execute("");

        ArrayList<NormalTask> tasks = new ArrayList<>();

        try {
            tasks = t.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return tasks;
    }


    /**
     * Get a list of task that match the geo-location
     *
     * @param coordinates   the coordinate of the provider location
     */
    // http://stackoverflow.com/questions/36805014/how-to-merge-geo-distance-filter-with-bool-term-query
    // Author: Val
    public void searchTaskByGeoLocation(List<Double> coordinates) {
        String query = String.format(
                "{\n" +
                        "    \"query\": {\n" +
                        "       \"filtered\" : {\n" +
                        "           \"filter\" : {\n" +
                        "               \"geo_distance\" : {\n" +
                        "                   \"distance\" : \"5km\",\n" +
                        "                   \"coordinates\" : [%.6f, %.6f]\n" +
                        "                 }\n" +
                        "             }\n" +
                        "         }\n" +
                        "      }\n" +
                        "}", coordinates.get(0), coordinates.get(1));

        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }


    /**
     * Get a list of tasks that match the keyword
     *
     * @param keyword           The keyword to match
     * @return An ArrayList of matching tasks.
     */
    public void searchRequestedTaskByKeyword(String keyword) {
        String query = String.format(
                "{\n" +
                        "    \"query\": {\n" +
                        "       \"wildcard\" : {\n" +
                        "           \"description\" : \"*%s*\" \n" +
                        "       }\n" +
                        "    },\n" +
                        "    \"filter\": {\n" +
                        "       \"bool\" : {\n" +
                        "           \"must\" : [" +
                        "               { \"term\": {\"status\": \"requested\"} }\n" +
                        "           ]\n" +
                        "       }\n" +
                        "    }\n" +
                        "}", keyword);

        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }

    /**
     * Get a list of tasks that match the keyword
     *
     * @param keyword           The keyword to match
     * @return An ArrayList of matching tasks.
     */
    public void searchBiddedTaskByKeyword(String keyword) {
        String query = String.format(
                "{\n" +
                        "    \"query\": {\n" +
                        "       \"wildcard\" : {\n" +
                        "           \"description\" : \"*%s*\" \n" +
                        "       }\n" +
                        "    },\n" +
                        "    \"filter\": {\n" +
                        "       \"bool\" : {\n" +
                        "           \"must\" : [" +
                        "               { \"term\": {\"status\": \"bidded\"} }\n" +
                        "           ]\n" +
                        "       }\n" +
                        "    }\n" +
                        "}", keyword);

        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }


    /**
     * Get a list of requested tasks for provider
     */
    public void getProviderRequestedTask(){
        String query = String.format(
                "{\n" +
                        "    \"query\": {\n" +
                        "       \"term\" : { \"status\" : \"requested\" }\n" +
                        "    }\n" +
                        "}");
        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }

    /**
     * Get a list of bidded tasks for provider
     */
    public void getProviderBiddedTask(){
        String query = String.format(
                "{\n" +
                        "    \"query\": {\n" +
                        "       \"term\" : { \"status\" : \"bidded\" }\n" +
                        "    }\n" +
                        "}");
        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }

    /**
     * Get a list of tasks of provider's assigned tasks
     *
     * @param providerUserName the provider's user name
     */
    public void getProviderAssignedTask(String providerUserName){
        String query = String.format(
                "{\n" +
                        "    \"filter\": {\n" +
                        "       \"bool\" : {\n" +
                        "           \"must\" : [\n " +
                        "               { \"term\": {\"providerUserName\": \"%s\"} },\n" +
                        "               { \"term\": {\"status\": \"assigned\"} }\n" +
                        "           ]\n" +
                        "       }\n" +
                        "    }\n" +
                        "}", providerUserName);
        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }



    /**
     * Get a list of tasks of provider's done tasks
     *
     * @param providerUserName the provider's user name
     */
    public void getProviderDoneTask(String providerUserName) {
        String query = String.format(
                "{\n" +
                        "    \"filter\": {\n" +
                        "       \"bool\" : {\n" +
                        "           \"must\" : [\n " +
                        "               { \"term\": {\"providerUserName\": \"%s\"} },\n" +
                        "               { \"term\": {\"status\": \"done\"} }\n" +
                        "           ]\n" +
                        "       }\n" +
                        "    }\n" +
                        "}", providerUserName);
        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }

    /**
     * Get a list of tasks of requester's requested tasks
     *
     * @param requesterUserName the requester's username
     */
    public void getRequesterRequestedTask(String requesterUserName) {
        String query = String.format(
                "{\n" +
                        "    \"filter\": {\n" +
                        "       \"bool\" : {\n" +
                        "           \"must\" : [\n " +
                        "               { \"term\": {\"requesterUserName\": \"%s\"} }, \n" +
                        "               { \"term\": {\"status\": \"requested\"} }\n" +
                        "           ]\n" +
                        "       }\n" +
                        "    }\n" +
                        "}", requesterUserName);
        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }

    /**
     * Get a list of tasks of requester's bidded tasks
     *
     * @param requesterUserName the requester's username
     */
    public void getRequesterBiddedTask(String requesterUserName) {
        String query = String.format(
                "{\n" +
                        "    \"filter\": {\n" +
                        "       \"bool\" : {\n" +
                        "           \"must\" : [\n " +
                        "               { \"term\": {\"requesterUserName\": \"%s\"} }, \n" +
                        "               { \"term\": {\"status\": \"bidded\"} }\n" +
                        "           ]\n" +
                        "       }\n" +
                        "    }\n" +
                        "}", requesterUserName);
        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }

    /**
     * Get a list of tasks of requester's assigned tasks
     *
     * @param requesterUserName the requester's username
     */
    public void getRequesterAssignedTask(String requesterUserName) {
        String query = String.format(
                "{\n" +
                        "    \"filter\": {\n" +
                        "       \"bool\" : {\n" +
                        "           \"must_not\" : {" +
                        "               \"term\": {\"isCompleted\": true}\n" +
                        "           },\n" +
                        "           \"must\" : [\n " +
                        "               { \"term\": {\"requesterUserName\": \"%s\"} }, \n" +
                        "               { \"term\": {\"status\": \"assigned\"} }\n" +
                        "           ]\n" +
                        "       }\n" +
                        "    }\n" +
                        "}", requesterUserName);
        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }



    /**
     * Get a list of tasks of requester's Done tasks
     *
     * @param requesterUserName the requester's user name
     */
    public void getRequesterDoneTask(String requesterUserName) {
        String query = String.format(
                "{\n" +
                        "    \"filter\": {\n" +
                        "       \"bool\" : {\n" +
                        "           \"must\" : [\n " +
                        "               { \"term\": {\"requesterUserName\": \"%s\"} },\n" +
                        "               { \"term\": {\"status\": \"done\"} }\n" +
                        "           ]\n" +
                        "       }\n" +
                        "    }\n" +
                        "}", requesterUserName);
        Task.GetTasksListTask task = new Task.GetTasksListTask(listener);
        task.execute(query);
    }

    /**
     * Handle Off Line
     */

    /**
     * Get a list of provider's requested tasks while offline
     * @param context            activity context
     */
    public void getProviderOfflineRequestedTask(Context context) {
        ArrayList<String> fileList = TaskUtil.getProviderTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> tasksList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = tasksList.iterator();
        while (it.hasNext()) {
            Task r = it.next();
            if (r.getStatus() == null) continue;
            if (!r.getStatus().equals("requested")) {
                it.remove();
            }
        }
        if (tasksList.isEmpty()) return;
        listener.onTaskCompleted(tasksList);
    }

    /**
     * Get a list of provider's bidded tasks while offline
     * @param context activity context
     */
    public void getProviderOfflineBiddedTask(Context context) {
        ArrayList<String> fileList = TaskUtil.getProviderTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> tasksList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = tasksList.iterator();
        while (it.hasNext()) {
            Task r = it.next();
            if (r.getStatus() == null) continue;
            if (!r.getStatus().equals("bidded")) {
                it.remove();
            }
        }
        if (tasksList.isEmpty()) return;
        listener.onTaskCompleted(tasksList);
    }

    /**
     * Get a list of provider's assigned tasks while offline
     * @param providerUserName the provider's user name
     * @param context activity context
     */
    public void getProviderOfflineAssignedTask(String providerUserName, Context context) {
        ArrayList<String> fileList = TaskUtil.getProviderTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> tasksList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = tasksList.iterator();
        while (it.hasNext()) {
            Task r = it.next();
            if (r.getProviderUserName() == null || !r.getProviderUserName().equals(providerUserName)
                    || r.getStatus() == null || !r.getStatus().equals("assigned")) {
                it.remove();
            }
        }
        if (tasksList.isEmpty()) return;
        listener.onTaskCompleted(tasksList);
    }



    /**
     * Get a list of provider's done tasks while offline
     * @param providerUserName the provider's user name
     * @param context activity context
     */
    public void getProviderOfflineDoneTask(String providerUserName, Context context) {
        ArrayList<String> fileList = TaskUtil.getProviderTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> tasksList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = tasksList.iterator();
        while (it.hasNext()) {
            Task r = it.next();
            if (r.getProviderUserName() == null || !r.getProviderUserName().equals(providerUserName)
                    || r.getStatus() == null || !r.getStatus().equals("done")) {
                it.remove();
            }
        }
        if (tasksList.isEmpty()) return;
        listener.onTaskCompleted(tasksList);
    }

    /**
     * Get a list of requester's requested tasks while offline
     * @param requesterUserName the requester's user name
     * @param context            activity context
     */
    public void getRequesterOfflineRequestedTask(String requesterUserName, Context context) {
        ArrayList<String> fileList = TaskUtil.getRequesterTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> tasksList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = tasksList.iterator();
        while (it.hasNext()) {
            Task r = it.next();
            if (r.getStatus() == null) continue;
            if (r.getRequesterUserName() == null) continue;
            if (!r.getRequesterUserName().equals(requesterUserName) || !r.getStatus().equals("requested")) {
                it.remove();
            }
        }
        if (tasksList.isEmpty()) return;
        listener.onTaskCompleted(tasksList);
    }

    /**
     * Get a list of requester's bidded tasks while offline
     * @param requesterUserName the requester's user name
     * @param context activity context
     */
    public void getRequesterOfflineBiddedTask(String requesterUserName, Context context) {
        ArrayList<String> fileList = TaskUtil.getRequesterTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> tasksList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = tasksList.iterator();
        while (it.hasNext()) {
            Task r = it.next();
            if (r.getStatus() == null) continue;
            if (r.getRequesterUserName() == null) continue;
            if (!r.getRequesterUserName().equals(requesterUserName) || !r.getStatus().equals("bidded")) {
                it.remove();
            }
        }
        if (tasksList.isEmpty()) return;
        listener.onTaskCompleted(tasksList);
    }

    /**
     * Get a list of requester's assigned tasks while offline
     * @param requesterUserName the requester's user name
     * @param context activity context
     */
    public void getRequesterOfflineAssignedTask(String requesterUserName, Context context) {
        ArrayList<String> fileList = TaskUtil.getRequesterTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> tasksList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = tasksList.iterator();
        while (it.hasNext()) {
            Task r = it.next();
            if (r.getRequesterUserName() == null || !r.getRequesterUserName().equals(requesterUserName)
                        || r.getStatus() == null || !r.getStatus().equals("assigned")) {
                it.remove();
            }
        }
        if (tasksList.isEmpty()) return;
        listener.onTaskCompleted(tasksList);
    }



    /**
     * Get a list of requester's Done tasks while offline
     * @param requesterUserName the requester's user name
     * @param context activity context
     */
    public void getRequesterOfflineDoneTask(String requesterUserName, Context context) {
        ArrayList<String> fileList = TaskUtil.getRequesterTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> tasksList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = tasksList.iterator();
        while (it.hasNext()) {
            Task r = it.next();
            if (r.getRequesterUserName() == null || !r.getRequesterUserName().equals(requesterUserName)
                    || r.getStatus() == null || !r.getStatus().equals("done")) {
                it.remove();
            }
        }
        if (tasksList.isEmpty()) return;
        listener.onTaskCompleted(tasksList);
    }
    /**
     * Send provider's bidded request to the server once the device is back online
     * @param requesterUserName the requester's user name
     * @param context activity context
     */
    public void updateRequesterOfflineTask(String requesterUserName, Context context) {
        ArrayList<String> fileList = TaskUtil.getAcceptedTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> requestsList = FileIOUtil.loadTaskFromFile(context, fileList);
        for (Task t : requestsList) {
            if (t.getRequesterUserName() == null || t.getRequesterUserName() == requesterUserName) {
                updateTask(t);
                // Delete file after it has been upload
                context.deleteFile(TaskUtil.generateAcceptedTaskFileName(t));
            }
        }
    }

        /**
     * Get a list of requester's pending tasks while offline
     * @param requesterUserName   the driver's user name
     * @param context            activity context
     */
    public void getRequesterOfflineTask(String requesterUserName, Context context) {
        ArrayList<String> fileList = TaskUtil.getRequesterTaskList(context);
        if (fileList == null) return;
        ArrayList<Task> requestsList = FileIOUtil.loadTaskFromFile(context, fileList);
        Iterator<Task> it = requestsList.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (!t.getRequesterUserName().equals(requesterUserName)) {
                it.remove();
            }
        }
        if (requestsList.isEmpty()) return;
        listener.onTaskCompleted(requestsList);
    }

    /**
     * Provider bid task.
     *
     * @param task              the task
     * @param providerUserName  the provider user name
     * @param price             the price of the task
     */
    public void providerBidTask(Task task, String providerUserName, double price) throws TaskException {
        task.providerBidTask(providerUserName, price);
        updateTask(task);
    }

    /**
     * Requester assign task.
     *
     * @param task the task to be assigned
     * @param providerUserName the provider user name
     */
    public void requesterAssignTask(Task task, String providerUserName) throws TaskException {
        task.requesterAssignProvider(providerUserName);
        updateTask(task);
    }

    /**
     * Requester change assign task to bidded task.
     *
     * @param task the task to be assigned completed
     * @param providerUserName the provider user name
     */
    public void requesterCancelAssignedTask(Task task, String providerUserName) throws TaskException {
        task.requesterCancelAssigned(providerUserName);
        updateTask(task);
    }


    /**
     * Requester confirm task done.
     *
     * @param task the task to be confirmed done
     */
    public void requesterDoneTask(Task task) throws TaskException {
        task.requesterDoneTask();
        updateTask(task);
    }
}

