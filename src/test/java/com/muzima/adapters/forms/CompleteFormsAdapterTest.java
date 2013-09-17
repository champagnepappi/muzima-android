package com.muzima.adapters.forms;

import android.widget.ListView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersSectionIndexerAdapterWrapper;
import com.muzima.controller.FormController;
import com.muzima.model.CompleteFormWithPatientData;
import com.muzima.model.PatientMetaData;
import com.muzima.model.collections.CompleteFormsWithPatientData;
import com.muzima.testSupport.CustomTestRunner;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.muzima.adapters.forms.CompleteFormsAdapter.BackgroundQueryTask;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(CustomTestRunner.class)
public class CompleteFormsAdapterTest {
    private CompleteFormsAdapter formsAdapter;
    private FormController formController;

    @Before
    public void setUp() throws Exception {
        formController = mock(FormController.class);
        formsAdapter = new CompleteFormsAdapter(null, 0, formController);

        Robolectric.getBackgroundScheduler().pause();
        Robolectric.getUiThreadScheduler().pause();
    }

    @Test
    public void queryTask_shouldFetchAllCompletedForms() throws Exception, FormController.FormFetchException {
        BackgroundQueryTask queryTask = new BackgroundQueryTask(formsAdapter);
        CompleteFormsWithPatientData completeFormsWithPatientData = new CompleteFormsWithPatientData();
        completeFormsWithPatientData.add(new CompleteFormWithPatientData());

        when(formController.getAllCompleteForms()).thenReturn(completeFormsWithPatientData);

        queryTask.execute();
        Robolectric.runBackgroundTasks();
        assertEquals(completeFormsWithPatientData, queryTask.get(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void queryTask_shouldBuildPatientList() throws Exception, FormController.FormFetchException {
        BackgroundQueryTask queryTask = new BackgroundQueryTask(formsAdapter);
        final PatientMetaData patient1MetaData = new PatientMetaData("family name", "given name", "middle name", "identifier1");
        final PatientMetaData patient2MetaData = new PatientMetaData("family name", "given name", "middle name", "identifier2");
        CompleteFormsWithPatientData completeFormsWithPatientData = new CompleteFormsWithPatientData();
        completeFormsWithPatientData.add(new CompleteFormWithPatientData(){{
            setPatientMetaData(patient1MetaData);
        }});
        completeFormsWithPatientData.add(new CompleteFormWithPatientData(){{
            setPatientMetaData(patient2MetaData);
        }});
        completeFormsWithPatientData.add(new CompleteFormWithPatientData(){{
            setPatientMetaData(patient1MetaData);
        }});

        when(formController.getAllCompleteForms()).thenReturn(completeFormsWithPatientData);

        List<PatientMetaData> patients = new ArrayList<PatientMetaData>() {{
            add(patient1MetaData);
            add(patient2MetaData);
        }};

        StickyListHeadersListView listView = new StickyListHeadersListView(Robolectric.application);
        listView.setAdapter(formsAdapter);
        formsAdapter.setListView(listView);

        queryTask.execute();
        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        assertThat(formsAdapter.getPatients(), is(patients));
    }

    @Test
    public void sortFormsByPatientName_shouldSortPatientsList() throws Exception {
        final PatientMetaData patient1MetaData = new PatientMetaData("Obama", "Barack", "Hussein", "id1");
        final PatientMetaData patient2MetaData = new PatientMetaData("Obama", "George", "W", "id2");
        CompleteFormsAdapter completeFormsAdapter = new CompleteFormsAdapter(null, 0, formController);
        completeFormsAdapter.setPatients(new ArrayList<PatientMetaData>() {{
            add(patient2MetaData);
            add(patient1MetaData);
        }});

        StickyListHeadersListView listView = new StickyListHeadersListView(Robolectric.application);
        listView.setAdapter(completeFormsAdapter);
        completeFormsAdapter.setListView(listView);

        completeFormsAdapter.sortFormsByPatientName(completeFormsAdapter.getCurrentListData());

        assertThat(completeFormsAdapter.getPatients().get(0), is(patient1MetaData));
        assertThat(completeFormsAdapter.getPatients().get(1), is(patient2MetaData));
    }

    @Test
    @Ignore
    public void sortFormsByPatientName_shouldSortCompleteFormsListByPatientMetadata() throws Exception {
        final PatientMetaData patient1MetaData = new PatientMetaData("Obama", "Barack", "Hussein", "id1");
        final PatientMetaData patient2MetaData = new PatientMetaData("Obama", "George", "W", "id2");
        final PatientMetaData patient3MetaData = new PatientMetaData("Bush", "George", "W", "id3");

        CompleteFormsAdapter completeFormsAdapter = new CompleteFormsAdapter(Robolectric.application, 0, formController);
        completeFormsAdapter.setPatients(new ArrayList<PatientMetaData>() {{
            add(patient1MetaData);
            add(patient2MetaData);
            add(patient3MetaData);
        }});

        CompleteFormsWithPatientData completeFormsWithPatientData = new CompleteFormsWithPatientData();

        CompleteFormWithPatientData completeFormWithPatientData1 = new CompleteFormWithPatientData();
        completeFormWithPatientData1.setPatientMetaData(patient1MetaData);
        CompleteFormWithPatientData completeFormWithPatientData2 = new CompleteFormWithPatientData();
        completeFormWithPatientData2.setPatientMetaData(patient3MetaData);
        CompleteFormWithPatientData completeFormWithPatientData3 = new CompleteFormWithPatientData();
        completeFormWithPatientData3.setPatientMetaData(patient1MetaData);
        CompleteFormWithPatientData completeFormWithPatientData4 = new CompleteFormWithPatientData();
        completeFormWithPatientData4.setPatientMetaData(patient2MetaData);

        completeFormsWithPatientData.add(completeFormWithPatientData1);
        completeFormsWithPatientData.add(completeFormWithPatientData2);
        completeFormsWithPatientData.add(completeFormWithPatientData3);
        completeFormsWithPatientData.add(completeFormWithPatientData4);

        completeFormsAdapter.addAll(completeFormsWithPatientData);

        completeFormsAdapter.sortFormsByPatientName(formsAdapter.getCurrentListData());

        assertThat(completeFormsAdapter.getItem(0), is(completeFormWithPatientData2));
        assertThat(completeFormsAdapter.getItem(1), is(completeFormWithPatientData1));
        assertThat(completeFormsAdapter.getItem(2), is(completeFormWithPatientData3));
        assertThat(completeFormsAdapter.getItem(3), is(completeFormWithPatientData4));
    }
}
