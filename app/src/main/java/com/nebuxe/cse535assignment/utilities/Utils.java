package com.nebuxe.cse535assignment.utilities;

import android.content.Context;
import android.util.Log;

import com.nebuxe.cse535assignment.R;

import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static byte[] toByteArray(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);

        return bytes;
    }

    public static ByteBuffer clone(ByteBuffer byteBuffer) {
        ByteBuffer clone = ByteBuffer.allocate(byteBuffer.capacity());
        clone.put(byteBuffer);
        clone.rewind();
        return clone;
    }

    public static File getLocalFile(Context context, String filename) {
        String appName = context.getResources().getString(R.string.app_name);

        File mediaStorageDirectory = new File(context.getFilesDir(), appName);
        if (!mediaStorageDirectory.exists()) {
            if (!mediaStorageDirectory.mkdirs()) {
                return null;
            }
        }

        for (File f : mediaStorageDirectory.listFiles()) {
            Log.d("CAMERA", f.getName());
            f.delete();
        }

        if (filename == null) {
            String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            filename = "VID_" + appName.toUpperCase() + "_" + timeStamp + ".mp4";
        }
        String filepath = mediaStorageDirectory.getPath() + File.separator + filename;
        return new File(filepath);
    }

    public static int countPeaks(List<Double> series) {
        int nPeaks = 0;

        for (int i = 1; i <= series.size() - 2; i++) {
            if (series.get(i) > series.get(i - 1) && series.get(i) > series.get(i + 1)) {
                nPeaks++;
            }
        }

        return nPeaks;
    }

    private static List<Double> getMovingAverage(List<Double> series, int windowSize) {
        List<Double> movingAverage = new ArrayList<>();
        double sum = 0;
        for (int i = 0; i < series.size(); i++) {
            sum += series.get(i);

            if (i >= windowSize - 1) {
                movingAverage.add(sum / windowSize);
                sum -= series.get(i - windowSize + 1);
            }
        }

        return movingAverage;
    }

    public static int countPeaks(List<Double> series, int windowSize) {

        List<Double> movingAverage = getMovingAverage(series, windowSize);
        List<Double> doubleMovingAverage = getMovingAverage(movingAverage, windowSize);
        return countPeaks(doubleMovingAverage);

//        return countPeaks(movingAverage);
    }

    public static boolean saveAdjacencyMatrixText(Context context, String adjacencyMatrix) {
        try {
            File directory = new File(context.getFilesDir(), "adjacency_matrices");
            if (!directory.exists()) {
                directory.mkdir();
            }

            File adjacencyMatrixFile = new File(directory, "matrix.txt");
            FileWriter fileWriter = new FileWriter(adjacencyMatrixFile);
            fileWriter.append(adjacencyMatrix);
            fileWriter.flush();
            fileWriter.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
