package org.hisp.dhis.android.trackercapture.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.hisp.dhis.android.sdk.controllers.DhisController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntity;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.List;

/*mod: this class was added as a broadcast receiver.
 * It is called when training app does not have a user logged in
 * It is used to check if a user is logged in over here already.
 * If a user is logged in, we pass the user to training app
 */

public class LoginReceiver extends BroadcastReceiver {
    private static final String APPS_MHBS_TRAINING_PACKAGE = "edu.iupui.soic.bhi.plhi.mhbs.training";
    private static final String keyRequest = "key:loginRequest";
    private static final String PIN_ID = "Nf2mWKVwmGK";

    @Override
    public void onReceive(Context context, Intent intent) {
        // check if a user is logged in
        if (MetaDataController.getUserAccount() != null) {

            // get user credentials
            ArrayList<String> userDetails = new ArrayList<>();
            StringBuffer validPins = new StringBuffer();
            StringBuffer orgUnits = new StringBuffer();
            StringBuffer trackerIds = new StringBuffer();
            userDetails.add(0, DhisController.getInstance().getSession().getCredentials().getUsername());
            userDetails.add(1, DhisController.getInstance().getSession().getCredentials().getPassword());
            userDetails.add(2, DhisController.getInstance().getSession().getServerUrl().toString());
            for (Enrollment enrollment : TrackerController.getActiveEnrollments()) {
                List<TrackedEntityAttributeValue> arrayList = enrollment.getAttributes();
                for (int i = 0; i < arrayList.size(); i++) {
                    if (arrayList.get(i).getTrackedEntityAttributeId().equals(PIN_ID)) {
                        validPins.append(enrollment.getAttributes().get(i).getValue() + ",");
                    }
                }
                orgUnits.append(enrollment.getOrgUnit() + ",");
                trackerIds.append(enrollment.getTrackedEntityInstance() + ",");
            }
            userDetails.add(3, validPins.toString());
            userDetails.add(4, orgUnits.toString());
            userDetails.add(5, trackerIds.toString());
            // use explicit front door intent to launch the login on training app with the user credentials
            Intent i = context.getPackageManager().getLaunchIntentForPackage(APPS_MHBS_TRAINING_PACKAGE);
            i.putExtra(keyRequest, userDetails);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}