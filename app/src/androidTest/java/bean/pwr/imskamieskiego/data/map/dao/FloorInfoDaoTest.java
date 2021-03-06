package bean.pwr.imskamieskiego.data.map.dao;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.database.sqlite.SQLiteConstraintException;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bean.pwr.imskamieskiego.TestObserver;
import bean.pwr.imskamieskiego.data.LocalDB;
import bean.pwr.imskamieskiego.data.map.entity.FloorInfoEntity;
import bean.pwr.imskamieskiego.model.map.Location;

import static org.junit.Assert.*;

public class FloorInfoDaoTest {

    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    private LocalDB database;
    private FloorInfoDao floorInfoDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                LocalDB.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();

        floorInfoDao = database.getFloorInfoDao();
        List<FloorInfoEntity> floorsList = Arrays.asList(
                new FloorInfoEntity(0, "ground floor"),
                new FloorInfoEntity(1, "1st floor"),
                new FloorInfoEntity(2, "2nd floor")
        );
        floorInfoDao.insertAllFloors(floorsList);
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test(expected = SQLiteConstraintException.class)
    public void addElementWithNullName() {
        List<FloorInfoEntity> floorList = Collections.singletonList(
                new FloorInfoEntity(4, null)
        );
        floorInfoDao.insertAllFloors(floorList);
    }

    @Test
    public void getListOfFloorNames() {
        String[] expectedNames = {"ground floor", "1st floor", "2nd floor"};

        LiveData<String[]> floorNames = floorInfoDao.getFloorNames();
        TestObserver<String[]> observer = new TestObserver<>();
        floorNames.observeForever(observer);


        assertArrayEquals(expectedNames, observer.observedValues.get(0));
    }
}