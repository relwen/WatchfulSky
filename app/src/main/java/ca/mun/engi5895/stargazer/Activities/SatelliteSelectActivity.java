package ca.mun.engi5895.stargazer.Activities;



import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.orekit.errors.OrekitException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import ca.mun.engi5895.stargazer.AndroidAestheticAdditions.MyListAdapter;
import ca.mun.engi5895.stargazer.OrbitingBodyCalculations.Entity;
import ca.mun.engi5895.stargazer.OrekitDataInstallation.celestrakData;
import ca.mun.engi5895.stargazer.R;

/**
 * Activity representing the satellite select screen that comes up
 * when you click "Geocentric" in the main activity
 */

public class SatelliteSelectActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private ListView listView;

    private ProgressBar progressBar;
    //private ArrayList<String> list = new ArrayList<String>();
    private ArrayAdapter<String> adapterList;
    private ArrayAdapter<String> favoriteList;

    public static ArrayList<Entity> selectedSats = new ArrayList<>();
    private static ArrayList<Object> satList = new ArrayList<>();
    private static ArrayList<Object> favoriteSats = new ArrayList<>();

    private static String TLE1;
    private static String TLE2;
    private static Entity currentEntity = null;

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    List<String> expandableListTitle;
    HashMap<String, List<String>> expandableListDetail;

    ExpandableListView expandableListView_fav;
    ExpandableListAdapter expandableListAdapter_fav;
    List<String> expandableListTitle_fav;
    HashMap<String, List<String>> expandableListDetail_fav;


    // Bottom navigation bar
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home: // Clicking the "satellites" button

                    expandableListView.setVisibility(View.VISIBLE); // Set the main list to visible
                    expandableListView_fav.setVisibility(View.INVISIBLE); // set the favorites list to invisible

                    return true;

                case R.id.navigation_dashboard: // Clicking the "favorites" button

                    System.out.println("Clicked Favourites");
                    expandableListView.setVisibility(View.INVISIBLE);

                    getFavData();

                    return true;
            }
            return false;
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satellite_sel);

        BottomNavigationView navigation = findViewById(R.id.navigation);
       navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener); // Set click lsitener for bottom navbar

        // Find the expandable list views, one for all satellites and one for favorite satellites
        expandableListView = findViewById(R.id.expandableListView);
        expandableListView_fav = findViewById(R.id.expandableListView_fav);

        // Get the satellite data from the internal files
        // Represents the child titles of the expandable list view
        try {
            expandableListDetail = celestrakData.getSatData(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Get the satellite categories for the expandable list view
        // Stored as the keys of the hash map
        expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
        // Set the custom adapter for the expandable list view
        expandableListAdapter = new MyListAdapter(this, expandableListTitle, expandableListDetail);
        expandableListView.setAdapter(expandableListAdapter);


        // Run the getSatsCreate method and make the expandable list view appear
        try {
            getSatsCreate();
            expandableListView.setVisibility(View.VISIBLE);

            // listView.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }

      //  listView = (ListView) findViewById(R.id.lvid2);
    }

    // Method that populates the expandable list view with the favorite satellites
    // Also handles clicking a favorite satellite and doing calculations with its data
    // Ran when the "favorites" button is clicked
    public void getFavData() {

        // Get the favorites data for the expandable list view
        try {
            expandableListDetail_fav = celestrakData.getSatDataFavorites(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Get the list titles for favorite satellites
        expandableListTitle_fav = new ArrayList<>(expandableListDetail_fav.keySet());
        // Get custom adapter, passing in the hashmap keys and data
        expandableListAdapter_fav = new MyListAdapter(this, expandableListTitle_fav, expandableListDetail_fav);
        // Set the adapter and make the list visible
        expandableListView_fav.setAdapter(expandableListAdapter_fav);
        expandableListView_fav.setVisibility(View.VISIBLE);

        // Set a click listener for the expandable list view
        expandableListView_fav.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                //Type of satellite is set to the parent chosen
                String satType = expandableListTitle_fav.get(groupPosition);
                // Satellite chosen is set to whatever name was chosen
                String satChosen = expandableListDetail_fav.get(expandableListTitle_fav.get(groupPosition)).get(childPosition);
                // Add the chosen satellite to the satList
                satList.add(satChosen);

                String fileName = null;

                // Need to know the filename corresponding to the chosen satellite
                if (satType.equals("Space Stations")) {
                    fileName = "favorites_stations.txt";
                } else if (satType.equals("Newly Launched Satellites"))
                    fileName = "favorites_tle-new.txt";
                else if (satType.equals("GPS Satellites")) {
                    fileName = "favorites_gps-ops.txt";
                } else if (satType.equals("Communications Satellites"))
                    fileName = "favorites_geo.txt";
                else if (satType.equals("Intelsat Satellites"))
                    fileName = "favorites_intelsat.txt";
                else if (satType.equals("Science Satellites"))
                    fileName = "favorites_science.txt";




                //Start the re-parsing of the text file for the TLE data for chosen satellite
                FileInputStream stream1 = null;
                try {
                    stream1 = openFileInput(fileName); //openFileInput auto opens from getFilesDir() directory
                    // getFilesDir() is directory of internal app storage
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                InputStreamReader sreader1 = new InputStreamReader(Objects.requireNonNull(stream1));
                BufferedReader breader1 = new BufferedReader(sreader1);

                String line1;
                String TLE1 = "";
                String TLE2 = "";

                //Read each lne of file, if its equal to the one chosen from the list, update TLE strings and break loop
                try {
                    while ((line1 = breader1.readLine()) != null) {
                        if (line1.equals(satChosen)) { //If the current line is the name of the one we chose from the list
                            TLE1 = breader1.readLine(); // Next line is first TLE string
                            TLE2 = breader1.readLine(); // Next line is second TLE string

                            break; //Break loop, data successfully acquired
                        }
                    }
                  //  breader1.close();  sreader1.close();stream1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Navigate to the MapsActivity screen
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                // Send the chosen satellite name and filename into the MapsActivity
                intent.putExtra("CHOSEN_SAT_NAME", satChosen);
                intent.putExtra("filename", fileName);
                startActivity(intent);


                try {
                    currentEntity = new Entity(satChosen, TLE1, TLE2); //Create a new sat entity using the satellite name and two TLE strings
                    selectedSats.add(currentEntity); // Add entity to selectedSats list
                    SatelliteSelectActivity.getSelectedSat();
                } catch (OrekitException e) {
                    e.printStackTrace();
                }

                return false;

            }

        });


    }

    /**
     * Handles click of satellite in main satellite list
     * @throws IOException
     */
    public void getSatsCreate() throws IOException {

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                //Handles the click of an item from the list
                String satType = expandableListTitle.get(groupPosition);
                String satChosen = expandableListDetail.get(expandableListTitle.get(groupPosition)).get(childPosition);
                satList.add(satChosen);

                String fileName = null;


                if (satType.equals("Space Stations")) {
                    fileName = "stations.txt";
                } else if (satType.equals("Newly Launched Satellites"))
                    fileName = "tle-new.txt";
                else if (satType.equals("GPS Satellites"))
                    fileName = "gps-ops.txt";
                else if (satType.equals("Communications Satellites"))
                    fileName = "geo.txt";
                else if (satType.equals("Intelsat Satellites"))
                    fileName = "intelsat.txt";
                else if (satType.equals("Science Satellites"))
                    fileName = "science.txt";

                System.out.println("Filename: "+ fileName);


                //Start the re-parsing of the text file for the TLE data for chosen satellite

                FileInputStream stream1 = null;
                try {
                    stream1 = openFileInput(fileName); //openFileInput auto opens from getFilesDir() directory
                    // getFilesDir() is directory of internal app storage
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


                InputStreamReader sreader1 = new InputStreamReader(Objects.requireNonNull(stream1));
                BufferedReader breader1 = new BufferedReader(sreader1);

                String line1;
                String TLE1 = "";
                String TLE2 = "";

                //Read each lne of file, if its equal to the one chosen from the list, update TLE strings and break loop


                try {
                    while ((line1 = breader1.readLine()) != null) {
                        if (line1.equals(satChosen)) { //If the current line is the one we chose from the list
                            TLE1 = breader1.readLine();
                            TLE2 = breader1.readLine();

                            break;
                        }
                    }
                    breader1.close();sreader1.close();stream1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Tle 1: " + TLE1);
                System.out.println("Tle 2: " + TLE2);

                // Navigate to the MapsActivity and send the filename and chosen satellite name
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("CHOSEN_SAT_NAME", satChosen);
                intent.putExtra(MapsActivity.FILENAME, fileName);
                startActivity(intent);


                try {
                    currentEntity = new Entity(satChosen, TLE1, TLE2); // Create entity from chosen satellite
                    selectedSats.add(currentEntity); // Add entity to selectedsats list
                    SatelliteSelectActivity.getSelectedSat();
                } catch (OrekitException e) {
                    e.printStackTrace();
                }

                return false;

            }

        });
    }


    public static ArrayList<Entity> getSelectedSat() { //ArrayList<String> getSelectedSat(){
        System.out.println(selectedSats.size());

        return selectedSats;
    }

    public static void clearSelectedSats() {
        selectedSats.clear();
        satList.clear();

    }

}