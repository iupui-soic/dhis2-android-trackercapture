package org.hisp.dhis.android.trackercapture.fragments.selectprogram;

import android.content.Context;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.events.OnRowClick;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttribute;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.TrackedEntityInstanceColumnNamesRow;
import org.hisp.dhis.android.sdk.ui.fragments.selectprogram.SelectProgramFragmentForm;
import org.hisp.dhis.android.sdk.persistence.loaders.Query;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.persistence.models.FailedItem;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.EventRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.events.TrackedEntityInstanceItemRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SelectProgramFragmentQuery implements Query<SelectProgramFragmentForm> {
    private final String mOrgUnitId;
    private final String mProgramId;

    public SelectProgramFragmentQuery(String orgUnitId, String programId) {
        mOrgUnitId = orgUnitId;
        mProgramId = programId;
    }

    @Override
    public SelectProgramFragmentForm query(Context context) {

        SelectProgramFragmentForm fragmentForm = new SelectProgramFragmentForm();
        List<EventRow> teiRows = new ArrayList<>();

        // create a list of EventItems
        Program selectedProgram = MetaDataController.getProgram(mProgramId);
        if (selectedProgram == null || isListEmpty(selectedProgram.getProgramStages())) {
            return fragmentForm;
        }

        // since this is single event its only 1 stage
        ProgramStage programStage = selectedProgram.getProgramStages().get(0);
        if (programStage == null || isListEmpty(programStage.getProgramStageDataElements())) {
            return fragmentForm;
        }

        List<ProgramTrackedEntityAttribute> attributes = selectedProgram.getProgramTrackedEntityAttributes();
        if (isListEmpty(attributes)) {
            return fragmentForm;
        }

        List<String> attributesToShow = new ArrayList<>();
        TrackedEntityInstanceColumnNamesRow columnNames = new TrackedEntityInstanceColumnNamesRow();

        for (ProgramTrackedEntityAttribute attribute : attributes) {
            if (attribute.getDisplayInList() && attributesToShow.size() < 3) {
                attributesToShow.add(attribute.getTrackedEntityAttributeId());
                if (attribute.getTrackedEntityAttribute() != null) {
                    String name = attribute.getTrackedEntityAttribute().getName();
                    if (attributesToShow.size() == 1) {
                        columnNames.setFirstItem(name);
                    } else if (attributesToShow.size() == 2) {
                        columnNames.setSecondItem(name);
                    } else if (attributesToShow.size() == 3) {
                        columnNames.setThirdItem(name);
                    }

                }
            }
        }
        teiRows.add(columnNames);

        List<Enrollment> enrollments = TrackerController.getEnrollments(
                mProgramId, mOrgUnitId);
        List<Long> trackedEntityInstanceIds = new ArrayList<>();
        if (isListEmpty(enrollments)) {
            return fragmentForm;
        } else {
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getLocalTrackedEntityInstanceId() > 0) {
                    if (!trackedEntityInstanceIds.contains(enrollment.getLocalTrackedEntityInstanceId()))
                        trackedEntityInstanceIds.add(enrollment.getLocalTrackedEntityInstanceId());
                }
            }
        }
        List<TrackedEntityInstance> trackedEntityInstanceList = new ArrayList<>();
        if (!isListEmpty(trackedEntityInstanceIds)) {
            for (long localId : trackedEntityInstanceIds) {
                TrackedEntityInstance tei = TrackerController.getTrackedEntityInstance(localId);
                trackedEntityInstanceList.add(tei);
            }
        }

        List<TrackedEntityAttribute> trackedEntityAttributes = new Select().from(TrackedEntityAttribute.class).queryList();
        Map<String, TrackedEntityAttribute> trackedEntityAttributeMap = new HashMap<>();
        for(TrackedEntityAttribute trackedEntityAttribute : trackedEntityAttributes) {
            trackedEntityAttributeMap.put(trackedEntityAttribute.getUid(), trackedEntityAttribute);
        }

        List<FailedItem> failedEvents = TrackerController.getFailedItems(FailedItem.TRACKEDENTITYINSTANCE);

        Set<String> failedEventIds = new HashSet<>();
        for (FailedItem failedItem : failedEvents) {
            TrackedEntityInstance tei = (TrackedEntityInstance) failedItem.getItem();
            if(tei == null) {
                failedItem.delete();
            } else {
                if(failedItem.getHttpStatusCode()>=0) {
                    failedEventIds.add(tei.getTrackedEntityInstance());
                }
            }
        }

        for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstanceList) {
            if (trackedEntityInstance == null) continue;
            teiRows.add(createTrackedEntityInstanceItem(context,
                    trackedEntityInstance, attributesToShow, attributes,
                    trackedEntityAttributeMap, failedEventIds));
        }

        fragmentForm.setEventRowList(teiRows);
        fragmentForm.setProgram(selectedProgram);
        fragmentForm.setColumnNames(columnNames);

        if(selectedProgram.getTrackedEntity() != null) {
            columnNames.setmTitle(selectedProgram.getTrackedEntity().getName() + "("  + (  teiRows.size() - 1)  + ")" );
        }

        return fragmentForm;
    }

    private EventRow createTrackedEntityInstanceItem(Context context, TrackedEntityInstance trackedEntityInstance,
                                                                         List<String> attributesToShow, List<ProgramTrackedEntityAttribute> attributes,
                                                                         Map<String, TrackedEntityAttribute> trackedEntityAttributeMap,
                                                                         Set<String> failedEventIds) {
        TrackedEntityInstanceItemRow trackedEntityInstanceItemRow = new TrackedEntityInstanceItemRow(context);
        trackedEntityInstanceItemRow.setTrackedEntityInstance(trackedEntityInstance);

        if (trackedEntityInstance.isFromServer()) {
            trackedEntityInstanceItemRow.setStatus(OnRowClick.ITEM_STATUS.SENT);
        } else if (failedEventIds.contains(trackedEntityInstance.getTrackedEntityInstance())) {
            trackedEntityInstanceItemRow.setStatus(OnRowClick.ITEM_STATUS.ERROR);
        } else {
            trackedEntityInstanceItemRow.setStatus(OnRowClick.ITEM_STATUS.OFFLINE);
        }

        for (int i = 0; i < attributesToShow.size(); i++) {
            if (i > attributesToShow.size()) break;

            String attributeUid = attributesToShow.get(i);
            if (attributeUid != null) {
                TrackedEntityAttributeValue teav = TrackerController.getTrackedEntityAttributeValue(attributeUid, trackedEntityInstance.getLocalId());

                TrackedEntityAttribute trackedEntityAttribute = trackedEntityAttributeMap.get(attributeUid);
                if (teav == null || trackedEntityAttribute == null) {
                    continue;
                }

                String value = teav.getValue();
                if(trackedEntityAttribute.isOptionSetValue()) {
                    if(trackedEntityAttribute.getOptionSet() == null) {
                        continue;
                    }
                    OptionSet optionSet = MetaDataController.getOptionSet(trackedEntityAttribute.getOptionSet());
                    if(optionSet == null) {
                        continue;
                    }
                    List<Option> options = MetaDataController.getOptions(optionSet.getUid());
                    if(options == null) {
                        continue;
                    }
                    for(Option option : options) {
                        if(option.getCode().equals(value)) {
                            value = option.getName();
                        }
                    }
                }

                if (i == 0) {
                    trackedEntityInstanceItemRow.setFirstItem(value);
                } else if (i == 1) {
                    trackedEntityInstanceItemRow.setSecondItem(value);
                } else if (i == 2) {
                    trackedEntityInstanceItemRow.setThirdItem(value);
                }
            }
        }
        return trackedEntityInstanceItemRow;
    }

    private static <T> boolean isListEmpty(List<T> items) {
        return items == null || items.isEmpty();
    }

}
