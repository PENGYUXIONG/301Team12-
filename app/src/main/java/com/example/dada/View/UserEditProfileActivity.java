/*
 * UserEditProfileActivity
 *
 *
 * April 9, 2018
 *
 * Copyright (c) 2018 Team 12. CMPUT301, University of Alberta - All Rights Reserved.
 * You may use, distribute, or modify this code under terms and condition of the Code of Student Behaviour at University of Alberta.
 * You can find a copy of the license in this project. Otherwise please contact me.
 */
package com.example.dada.View;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dada.Controller.TaskController;
import com.example.dada.Controller.UserController;
import com.example.dada.Exception.UserException;
import com.example.dada.Model.OnAsyncTaskCompleted;
import com.example.dada.Model.OnAsyncTaskFailure;
import com.example.dada.Model.Task.NormalTask;
import com.example.dada.Model.Task.RequestedTask;
import com.example.dada.Model.Task.Task;
import com.example.dada.Model.User;
import com.example.dada.R;
import com.example.dada.Util.FileIOUtil;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *  Activity to edit user profile
 */
public class UserEditProfileActivity extends AppCompatActivity {
    // edit text for user
    private EditText usernameText;
    private EditText emailText;
    private EditText mobileText;

    // list of task for change their name
    private ArrayList<Task> requestedTaskList = new ArrayList<>();
    private ArrayList<Task> biddedTaskList = new ArrayList<>();
    private ArrayList<Task> assignedTaskList = new ArrayList<>();
    private ArrayList<Task> doneTaskList = new ArrayList<>();

    private Button saveButton;
    private User user;

    //Controllers for get list of tasks
    private UserController userController = new UserController(new OnAsyncTaskCompleted() {
        @Override
        public void onTaskCompleted(Object o) {
            User user = (User) o;
            FileIOUtil.saveUserInFile(user, getApplicationContext());
        }
    });

    private TaskController requestedTaskController = new TaskController(new OnAsyncTaskCompleted() {
        @Override
        public void onTaskCompleted(Object o) {
            requestedTaskList = (ArrayList<Task>) o;
        }
    });

    private TaskController biddedTaskController = new TaskController(new OnAsyncTaskCompleted() {
        @Override
        public void onTaskCompleted(Object o) {
            biddedTaskList = (ArrayList<Task>) o;
        }
    });

    private TaskController assignedTaskController = new TaskController(new OnAsyncTaskCompleted() {
        @Override
        public void onTaskCompleted(Object o) {
            assignedTaskList = (ArrayList<Task>) o;
        }
    });

    private TaskController doneTaskController = new TaskController(new OnAsyncTaskCompleted() {
        @Override
        public void onTaskCompleted(Object o) {
            doneTaskList = (ArrayList<Task>) o;
        }
    });

    //Controller for update
    private TaskController taskController = new TaskController(
            new OnAsyncTaskCompleted() {
                @Override
                public void onTaskCompleted(Object o) {
                    Task t = (Task) o;
                    FileIOUtil.saveRequesterTaskInFile(t, getApplicationContext());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit_profile);

        // get user info
        user = FileIOUtil.loadUserFromFile(getApplicationContext());

        // initialize text and button
        usernameText = findViewById(R.id.editText_userName_EditUserProfileActivity);
        emailText = findViewById(R.id.editText_email_EditUserProfileActivity);
        mobileText = findViewById(R.id.editText_mobile_EditUserProfileActivity);
        saveButton = findViewById(R.id.button_save_EditUserProfileActivity);
        assert saveButton != null;

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile();
            }
        });

    }

    /**
     * onStart() - android build in function
     */
    @Override
    public void onStart() {
        super.onStart();
        // get the user, use the user to setText
        usernameText.setText(user.getUserName());   // setText(user.getName)
        emailText.setText(user.getEmail());      // setText(user.getEmail)
        mobileText.setText(user.getPhone());     // setText(user.getMobile)

        //get tasks
        requestedTaskController.getRequesterRequestedTask(user.getUserName());
        biddedTaskController.getRequesterBiddedTask(user.getUserName());
        assignedTaskController.getRequesterAssignedTask(user.getUserName());
        doneTaskController.getRequesterDoneTask(user.getUserName());
    }

    /**
     * save and upload the new profile to the ES server
     */
    public void editProfile(){
        //get user input info
        String username = usernameText.getText().toString();
        String email = emailText.getText().toString();
        String mobile = mobileText.getText().toString();

        String oldUserName = user.getUserName();

        // show them to user
        user.setUserName(username);
        user.setEmail(email);
        user.setPhone(mobile);

        //initialize checking
        boolean validUsername = !(username.isEmpty() || username.trim().isEmpty());
        boolean validEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean validMobile = Patterns.PHONE.matcher(mobile).matches();

        //check input
        if ( !(validUsername && validEmail && validMobile) ){
            Toast.makeText(this, "Username/Email/Mobile is not valid.", Toast.LENGTH_SHORT).show();
        }
        else if(username.length() > 8){
            Toast.makeText(this, "max username length is 8 characters", Toast.LENGTH_SHORT).show();
        }
        else {
            try {
                Log.i("Debug", user.getID());

                userController.updateUser(user, oldUserName);

                // change all the name inside the tasks where the user participated
                for (Iterator iter = requestedTaskList.iterator(); iter.hasNext() ; ) {
                    Task oldTask = (Task) iter.next();
                    oldTask.setRequesterUserName(username);
                    taskController.updateTask(oldTask);
                }

                for (Iterator iter = biddedTaskList.iterator(); iter.hasNext() ; ) {
                    Task oldTask = (Task) iter.next();
                    if(oldTask.getRequesterUserName().equals(oldUserName)){
                        oldTask.setRequesterUserName(username);
                        taskController.updateTask(oldTask);
                    }
                    if(oldTask.getProviderUserName().equals(oldUserName)){
                        oldTask.setProviderUserName(username);
                        taskController.updateTask(oldTask);
                    }
                }

                for (Iterator iter = assignedTaskList.iterator(); iter.hasNext() ; ) {
                    Task oldTask = (Task) iter.next();
                    if(oldTask.getRequesterUserName().equals(oldUserName)){
                        oldTask.setRequesterUserName(username);
                        taskController.updateTask(oldTask);
                    }
                    if(oldTask.getProviderUserName().equals(oldUserName)){
                        oldTask.setProviderUserName(username);
                        taskController.updateTask(oldTask);
                    }
                }

                for (Iterator iter = doneTaskList.iterator(); iter.hasNext() ; ) {
                    Task oldTask = (Task) iter.next();
                    if(oldTask.getRequesterUserName().equals(oldUserName)){
                        oldTask.setRequesterUserName(username);
                        taskController.updateTask(oldTask);
                    }
                    if(oldTask.getProviderUserName().equals(oldUserName)){
                        oldTask.setProviderUserName(username);
                        taskController.updateTask(oldTask);
                    }
                }

                Toast.makeText(getApplicationContext(), "User profile changed, please login again.",Toast.LENGTH_SHORT).show();
                finish();
            } catch (UserException e) {
                // if the username has been taken
                Toast.makeText(this, "Username has been taken.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
