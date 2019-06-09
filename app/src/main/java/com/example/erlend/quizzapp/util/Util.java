package com.example.erlend.quizzapp.util;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.widget.ProgressBar;

import com.annimon.stream.Collectors;
import com.annimon.stream.IntStream;
import com.annimon.stream.Stream;
import com.example.erlend.quizzapp.database.DatabaseContract;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**Class with static helper methods. This class depends on the Android framework.**/
public class Util {

    private Util() {}

    private static Random random;
    static {
        random = new Random(); //Unngår å oprette en ny (pseudo)random hver gang et tilfeldig spørsmål forespørres
    }

    //=====================================
    //==NETWORK==
    //=====================================

    /**
     * Calulates the network-speed based on a number of file downloads fpsicified by nTimes. The download is a 1MB big file.
     * @param nTimes number of times the test will be run. The returned valeu is the avarage of the tests. Higher values
     *              is recomended to get a more nuanced picture for unstable networks,
     *               but is disadviced in cases where network usage should be kept at a mimimum.
     * @return the speed in MB/s. This is the average of the tests.
     */

    public static double getNetworkSpeedMBPrSec(int nTimes){
        //For nedlasting av filen: https://www.journaldev.com/924/java-download-file-url

        ArrayList<Double> listTests = new ArrayList<>();

            IntStream.range(0, nTimes).forEach(i -> {
                URL urlToFile;
                try {
                    urlToFile = new URL("http://ask.hiof.no/~erlendao/testfilMedium");
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(urlToFile.openStream());
                    byte[] data = new byte[1024 * 1024];

                    long timestampStart = System.currentTimeMillis();
                    while ( bufferedInputStream.read(data) != -1);
                    long timestampEnd = System.currentTimeMillis();

                    double usedTimeSeconds =  (timestampEnd - timestampStart) / 1000d;
                    double megabytesPrSecond = (1024 / usedTimeSeconds) / 1024d;
                    listTests.add(megabytesPrSecond);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

            return Stream.of(listTests).collect(Collectors.averagingDouble(elem -> elem));
    }

    public static boolean hasNetworkConnection(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }



    //=====================================
    //==DATABASE==
    //=====================================

    /**Generates a string composed of the digit and if single-digited, padded with a zero in front**/
    public static String padWithZeroIfOneDigit(int digit){
        String strDigit = String.valueOf(digit);
        return strDigit.length() == 1 ? "0"+strDigit: strDigit;
    }

    /** Generates a SQL-lite compatible string for the creation of a table. Default character-set is UTF-8.
      @param table_name the name of the table being created
     * @param columns a  arbitrary amount of colums. Has to be greater than 0.
     * @throws IllegalArgumentException if number of columns is zero**/
    public static String generateSQLCreateTable(String table_name, Column... columns ) throws IllegalArgumentException{
        if(columns.length == 0){
           throw new IllegalArgumentException("Number of columns to generate must be greater than 0");
        }

        StringBuilder sql = new StringBuilder();
        sql.append(String.format("CREATE TABLE IF NOT EXISTS %s ( ", table_name));
        for (Column column: columns){
            sql.append(String.format("%s %s, ", column.name, column.type.str()));
        }
        
        sql.delete(sql.length() -2, sql.length() -1 ); //Removes the last comma
        sql.append(" );");
        return sql.toString();
    }

    public static class Column {
        public final String name;
        public final DatabaseContract.TYPE type;

        public Column(String name, DatabaseContract.TYPE type){
            this.name = name;
            this.type = type;
        }
    }

    public static boolean doesSqlLiteTableExist(String strTableName, SQLiteDatabase db){
        //https://stackoverflow.com/questions/1601151/how-do-i-check-in-sqlite-whether-a-table-exists
        Cursor cursor = db.rawQuery(String.format("SELECT * FROM sqlite_master WHERE type='table' AND tbl_name='%s';", strTableName), null);

        boolean tableExists = false;
        if(cursor != null){
            tableExists = cursor.getCount() > 0;
            cursor.close();
        }
        return tableExists;
    }

    public static String generateSQLDropTable(String tablename){
        return String.format("DROP TABLE IF EXISTS %s;", tablename);
    }



    //=====================================
    //==MATH==
    //=====================================

    //https://stackoverflow.com/questions/39824914/how-do-i-round-to-the-nearest-ten
    public static int roundToNearest(int num, int nearestToRound){
        return Math.round(num / nearestToRound) * nearestToRound;
    }

    /**Generates a pseudorandom number in the range spesified. This method should not be used when real randomness is required**/
    public static int randomNumBetween(int fromIncl, int toIncl){
        //https://stackoverflow.com/questions/363681/how-do-i-generate-random-integers-within-a-specific-range-in-java
        return random.nextInt(toIncl - fromIncl + 1 ) + fromIncl;
    }

    public static int percentageInt(int part, int total){
        return (int) (( ((float) part) / ((float) total) ) * 100);
    }



    //=====================================
    //==ANDROID==
    //=====================================

    public static boolean isPortrait(Resources resources) {
        return resources.getConfiguration().orientation == 1;
    }

    //Has to be called from UI-thread
    public static void updateProgressBar(int progress, ProgressBar progressBar) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("updateProgress: Progress not in interval 0-100");
        }
        if (VERSION.SDK_INT >= 24) {
            progressBar.setProgress(progress, true);
        } else {
            progressBar.setProgress(progress);
        }
        progressBar.setProgress(progress);
    }

    //https://stackoverflow.com/questions/11831188/how-to-get-bitmap-from-a-url-in-android
    public static Bitmap getBitmapFromURL(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getRandomColor(){
        return Colors.values()[random.nextInt(Colors.numOfColors())].getColor();
    }

    private enum Colors{
        RED(Color.RED), BLUE(Color.BLUE), GREEN(Color.GREEN), YELLOW(Color.YELLOW),
        PURPLE(Color.argb(255, 150, 7, 245 )), ORANGE(Color.argb(255, 255, 157, 0));
        private final int color;

        Colors(int color){
            this.color = color;
        }

        public int getColor() {
            return color;
        }

        public static int numOfColors(){
            return Colors.values().length;
        }
    }



    //=====================================
    //==MISC==
    //=====================================

    public static List<Integer> listWithUniqueRandomNums(int minInc, int maxInc, int numToGet){
        List<Integer> list = new ArrayList<>();
        while (list.size() < numToGet){
            int randomNum = randomNumBetween(minInc, maxInc);
            if(!list.contains(randomNum)){
                list.add(randomNum);
            }
        }
        return list;
    }

}
