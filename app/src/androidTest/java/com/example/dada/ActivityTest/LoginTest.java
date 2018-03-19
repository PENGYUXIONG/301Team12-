package com.example.dada.ActivityTest;


import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.example.dada.R;
import com.robotium.solo.Solo;
import android.widget.EditText;
import com.example.dada.View.LoginActivity;

/**
 * Intent test for log in
 */

public class LoginTest extends ActivityInstrumentationTestCase2{
    private Solo solo;

    public LoginTest(){
        super(com.example.dada.View.LoginActivity.class);
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
     * Test for invalid and valid log in format
     */
    public void TestLogin(){
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
        // test not exist user
        solo.enterText((EditText) solo.getView(R.id.edit_text_login_username), "notuser");
        solo.clickOnButton("Provider");
        solo.clickOnButton("Login");
        assertTrue(solo.waitForText("User does not exist, please signup"));

        //test exist user
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
        solo.clearEditText((EditText) solo.getView(R.id.edit_text_login_username));
        solo.enterText((EditText) solo.getView(R.id.edit_text_login_username), "user");
        solo.clickOnButton("provider");
        solo.clickOnButton("Login");

    }


    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }



}
