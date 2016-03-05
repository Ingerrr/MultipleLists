package com.example.inger.multiplelists;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    // Create variables
    ArrayList<String> listNames;
    ListView lists;
    ArrayAdapter<String> listAdapter;
    String clickedList;

    /*
    * Creates empty list if app is opened for the first time or reads existing list from file
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create link to ListView on screen
        lists = (ListView)findViewById(R.id.list);

        // Create empty list of to do lists
        listNames = new ArrayList<>();

        // Attempts to input list from file if it exists
        try {
            // Read existing to do lists from file
            Scanner scan = new Scanner(openFileInput("Listoflists.txt"));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                listNames.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Set Adapter to ListView
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listNames);
        lists.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        // Setup listener for clicks on ListView
        setupListener();

        // Check what was the last activity before app was killed
        SharedPreferences prefs = this.getSharedPreferences("settings", this.MODE_PRIVATE);
        int activityNumber = prefs.getInt("activity", 0);
        if (activityNumber == 1){
            // If last activity was not the MainActivity, go to the SingleListActivity with last opened list
            String lastList = prefs.getString("currentList", "");
            nextActivity(lastList);
        }
    }

    /*
    * Setup a listener for clicks on ListView
     */
    public void setupListener() {
        lists.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    /*
                    * Goes to the list that is long clicked
                     */
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        // Get title from list clicked on as a string
                        clickedList = lists.getItemAtPosition(position).toString();

                        // Go to SingleListActivity with that specific list
                        nextActivity(clickedList);
                    }
                });
    }

    /*
    * Opens new activity in which the user can make a new list and save it
     */
    public void addList(View view) {
        nextActivity("");
    }

    /*
    * Opens a new activity with the list that is clicked on or an empty one
     */
    public void nextActivity(String currentList){
        Intent intent = new Intent(this, SingleListActivity.class);

        // attaches the clicked list name so that the belonging list can be opened from SingleListActivity
        intent.putExtra("listNames", listNames);
        intent.putExtra("currentList", currentList);

        // Start MainActivity
        startActivity(intent);
        finish();
    }
}
