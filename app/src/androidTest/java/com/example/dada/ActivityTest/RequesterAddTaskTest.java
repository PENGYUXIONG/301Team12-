package com.example.dada.ActivityTest;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.example.dada.R;
import com.example.dada.View.RequesterAddTaskActivity;
import com.example.dada.View.RequesterBrowseTaskActivity;
import com.robotium.solo.Solo;
import android.widget.EditText;
import com.example.dada.View.LoginActivity;

public class RequesterAddTaskTest extends ActivityInstrumentationTestCase2{
    private Solo solo;

    public RequesterAddTaskTest(){
        super(com.example.dada.View.RequesterAddTaskActivity.class);
    }

    public void testStart() throws Exception {
        Activity activity = getActivity();

    }

    @Override
    public void setUp() throws Exception {
        Log.d("TAG1", "setUp()");
        solo = new Solo(getInstrumentation(), getActivity());
    }

    /**
     * Test for add task
     */
    public void Testadd(){
        solo.assertCurrentActivity("Wrong Activity", RequesterAddTaskTest.class);
        solo.getView(R.layout.activity_requester_add_task);
        solo.clickOnButton(R.id.newTask_done_button);
        solo.assertCurrentActivity("Wrong Activity", RequesterBrowseTaskActivity.class);
    }


    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}