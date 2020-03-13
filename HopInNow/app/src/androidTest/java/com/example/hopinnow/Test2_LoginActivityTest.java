package com.example.hopinnow;

import android.app.Activity;
import android.widget.EditText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.hopinnow.activities.DriverMapActivity;
import com.example.hopinnow.activities.LoginActivity;
import com.example.hopinnow.activities.RegisterActivity;
import com.example.hopinnow.activities.RegisterVehicleInfoActivity;
import com.example.hopinnow.activities.RiderMapActivity;

import com.robotium.solo.Solo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Author: Tianyu Bai
 * UI tests on login activities. Robotium test framework is used.
 */
@RunWith(AndroidJUnit4.class)
public class Test2_LoginActivityTest {
    private Solo solo;

    @Rule
    public ActivityTestRule<LoginActivity> rule =
            new ActivityTestRule<>(LoginActivity.class, true, true);

    /**
     * Runs before all tests and creates solo instance.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
        solo = new Solo(getInstrumentation(),
                rule.getActivity());
    }


    /**
     * Gets the Activity
     * @throws Exception
     */
    @Test
    public void start() throws Exception{
        Activity activity = rule.getActivity();
    }


    //TODO WRONG ACCOUNT THEN RIGHT ACCOUNT
    // NOTE6 : previous use must be logged out to login properly, else user that DNE can still log in
    /**
     * Tests invalid account.
     * @throws InterruptedException
     */
    @Test
    public void Case1_invalidAccount() throws InterruptedException {
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);
        solo.clickOnButton("LOGIN");
        assertTrue(solo.waitForText("Please", 1, 2000));

        // test email
        solo.enterText((EditText)solo.getView(R.id.loginEmailEditText),"invalid");
        assertTrue(solo.waitForText("invalid", 1, 2000));

        // test password
        solo.enterText((EditText)solo.getView(R.id.loginPassword),"11111111");
        assertTrue(solo.waitForText("1", 1, 2000));

        solo.clickOnButton("LOGIN");
        //TODO assert that the account is invalid after page is fixed

        Thread.sleep(2000);
    }

    /**
     * Tests valid account.
     * @throws InterruptedException
     */
    @Test
    public void Case2_validAccount() throws InterruptedException {
        solo.assertCurrentActivity("Wrong Activity", LoginActivity.class);

        // test email
        solo.enterText((EditText)solo.getView(R.id.loginEmailEditText),"folanqi123@ualberta.ca");
        assertTrue(solo.waitForText("folanqi123@ualberta.ca",
                1, 2000));

        // test password
        solo.enterText((EditText)solo.getView(R.id.loginPassword),"12345678");
        assertTrue(solo.waitForText("12345678", 1, 2000));

        solo.clickOnButton("LOGIN");
        solo.assertCurrentActivity("Wrong Activity", RiderMapActivity.class);

        Thread.sleep(2000);
        solo.clickOnView(solo.getView(R.id.riderMenuBtn));
        solo.clickOnView(solo.getView(R.id.riderMyProfile));
        solo.clickOnView(solo.getView(R.id.proLogoutBtn));
    }



    /**
     * Close activity after each test
     * @throws Exception
     *      throws all exception
     */
    @After
    public void tearDown() throws Exception{
        solo.finishOpenedActivities();
    }
}
