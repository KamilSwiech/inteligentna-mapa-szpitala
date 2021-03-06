/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package bean.pwr.imskamieskiego.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import bean.pwr.imskamieskiego.data.LocalDB;
import bean.pwr.imskamieskiego.data.map.dao.FloorInfoDao;

/**
 * Implementation of IFloorDataRepository. Data for floors names are taken from database, and floor
 * images from assets.
 */
public class FloorDataRepository implements IFloorDataRepository {

    private final String TAG = "MapImgRepository";
    private InputStream mapImageStream;
    private FloorInfoDao floorInfoDao;
    private MutableLiveData<InputStream> mapImageLiveData;
    private Context context;

    public FloorDataRepository(LocalDB dataBase, Context context) {
        this.context = context;
        floorInfoDao = dataBase.getFloorInfoDao();
        mapImageStream = null;
    }

    @Override
    public LiveData<String[]> getFloorNames() {
        return floorInfoDao.getFloorNames();
    }

    @Override
    public LiveData<InputStream> getMapImage(int floor) {

        if (mapImageLiveData == null){
            mapImageLiveData = new MutableLiveData<>();
        }

        if (mapImageStream != null){
            try {
                mapImageStream.close();
            } catch (IOException e) {
                Log.i(TAG, "mapImageStream closed with error");
                e.printStackTrace();
            }
        }

        mapImageStream = getMapStream(floor);
        mapImageLiveData.postValue(mapImageStream);

        return mapImageLiveData;
    }


    private InputStream getMapStream(int floor){
        final String MAP_IMAGE_DIR = "map-img/";
        final String MAP_IMG_TYPE = "png";

        InputStream mapImageStream = null;
        String imgPath = MAP_IMAGE_DIR + floor + "." + MAP_IMG_TYPE;

        Log.d(TAG, "Path: " + imgPath);

        try {
            mapImageStream = context.getAssets().open(imgPath, AssetManager.ACCESS_STREAMING);
        } catch (IOException e) {
            Log.w(TAG, "Problem with map image open.");
            e.printStackTrace();
        }

        return mapImageStream;
    }

}
