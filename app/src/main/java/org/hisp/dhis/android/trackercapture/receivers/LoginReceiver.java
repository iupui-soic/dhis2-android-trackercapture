package org.hisp.dhis.android.trackercapture.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.hisp.dhis.android.sdk.controllers.DhisController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;

import java.util.ArrayList;

/*mod: this class was added as a broadcast receiver.
 * It is called when training app does not have a user logged in
 * It is used to check if a user is logged in over here already.
 * If a user is logged in, we pass the user to training app
 */

public class LoginReceiver extends BroadcastReceiver {
    private static final String APPS_MHBS_TRAINING_PACKAGE = "edu.iupui.soic.bhi.plhi.mhbs.training";
    private static final String keyRequest = "key:loginRequest";

    @Override
    public void onReceive(Context context, Intent intent) {
        // check if a user is logged in
        if (MetaDataController.getUserAccount() != null) {

            // get user credentials
            ArrayList<String> userDetails = new ArrayList<>();
            userDetails.add(0, DhisController.getInstance().getSession().getCredentials().getUsername());
            userDetails.add(1, DhisController.getInstance().getSession().getCredentials().getPassword());
            userDetails.add(2, DhisController.getInstance().getSession().getServerUrl().toString());
            // use explicit front door intent to launch the login on training app with the user credentials
            Intent i = context.getPackageManager().getLaunchIntentForPackage(APPS_MHBS_TRAINING_PACKAGE);
            i.putExtra(keyRequest, userDetails);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}