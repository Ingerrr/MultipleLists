package com.example.inger.multiplelists;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class SingleListActivity extends AppCompatActivity {

    // Create variables
    ArrayList<String> toDoes;
    ListView listToDo;
    ArrayAdapter<String> listAdapter;
    String textFileName;
    ArrayList<String> listNames;
    String titleList = "";
    SharedPreferences prefs;

    /*
    * Creates empty list if app is opened for the first time or reads existing list from file
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_list);

        // Set number of last activity so that it can be accessed from MainActivity
        // 0 stands for MainActivity and 1 for SingleListActivity
        prefs = this.getSharedPreferences("settings", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("activity", 1);

        // Get extras from intent
        Bundle extras = getIntent().getExtras();
        listNames = extras.getStringArrayList("listNames");
        titleList = extras.getString("currentList");

        // Save title of current list
        editor.putString("currentList", titleList);
        editor.commit();

        // convert title into text file name
        textFileName = titleList.replace(" ", "") + ".txt";

        // Create link to ListView on screen
        listToDo = (ListView) findViewById(R.id.list);

        // Create empty toDoes
        toDoes = new ArrayList<String>();

        // Attempts to input list from file if it exists
        try {
            // Read existing toDoes from file
            Scanner scan = new Scanner(openFileInput(textFileName));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                toDoes.add(line);
            }
            // Set title of the list
            ((TextView) findViewById(R.id.title)).setText(titleList);

        } catch (FileNotFoundException e) {
            // Activate creation of new list if file doesn't exist
            RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

            // Make list name invisible
            RelativeLayout listName = (RelativeLayout) mainLayout.findViewById(R.id.listName);
            listName.setVisibility(INVISIBLE);

            // Make EditText field for list name visible
            RelativeLayout setListName = (RelativeLayout) mainLayout.findViewById(R.id.setListName);
            setListName.setVisibility(VISIBLE);
        }

        // Set Adapter to ListView
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toDoes);
        listToDo.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        // Setup listener for clicks on ListView
        setupListener();
    }

    /*
    * Adds item to toDoes after user input
     */
    public void addItem(View view) throws FileNotFoundException {

        // Check if a title for the list has been given
        if (titleList.matches("")){
            // Ask user to give a title first
            Toast.makeText(this, R.string.titleFirst, Toast.LENGTH_LONG).show();
            return;
        }
        else {
            // Read input from user
            String input = ((EditText) findViewById(R.id.input)).getText().toString();
            if(input.matches("")){
                return;
            }
            // Add input to toDoes
            toDoes.add(input);

            // Update ListView
            listAdapter.notifyDataSetChanged();

            // Clear EditText
            ((EditText) findViewById(R.id.input)).setText("");

            // Store/update list in text file
            writeToFile(toDoes, textFileName);
        }
    }

    /*
    * Setup a listener for clicks on ListView
     */
    public void setupListener() {
        listToDo.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {

                    /*
                    * Deletes an item from the list when it is long-clicked
                     */
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        // Removes item from list
                        toDoes.remove(position);

                        // Update ListView
                        listAdapter.notifyDataSetChanged();

                        // Update text file
                        try {
                            writeToFile(toDoes, textFileName);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        return true;
                    }
                }
        );
    }

    /*
    * Writes list to text file so that ListView doesn't reset after reboot
     */
    public void writeToFile(ArrayList<String> toDoes, String textFileName) throws FileNotFoundException {

        // Create link to file
        PrintStream out = new PrintStream(openFileOutput(textFileName, MODE_PRIVATE));

        // Write toDoes to file one by one
        for (int i = 0; i < toDoes.size(); i++) {
            out.println(toDoes.get(i));
        }

        // Close file
        out.close();
    }

    /*
    * Set name of list according to user input and store in textfiles.
     */
    public void setListName(View view) throws FileNotFoundException {

        // Get input from EditText
        titleList = ((EditText) findViewById(R.id.inputListName)).getText().toString();
        if(titleList.matches("")){
            return;
        }
        // Save title of current list
        prefs = this.getSharedPreferences("settings", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("currentList", titleList);
        editor.commit();

        // Deactivate EditText field for title
        findViewById(R.id.setListName).setVisibility(INVISIBLE);

        // Display title
        findViewById(R.id.listName).setVisibility(VISIBLE);
        ((TextView) findViewById(R.id.title)).setText(titleList);

        // Add input to listNames
        listNames.add(titleList);

        // Update Listoflists.txt
        writeToFile(listNames, "Listoflists.txt");

        // Convert title into text file name
        textFileName = titleList.replace(" ", "") + ".txt";

        // Create text file for this list
        writeToFile(toDoes, textFileName);
    }

    /*
    * Delete this specific list file and update Listoflists.txt
     */
    public void deleteList(View view) throws FileNotFoundException {

        // Ask for permission to delete the list in a popup window
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(R.string.sureToDelete);
        dlgAlert.setTitle(R.string.delete);

        // Include an OK button in the popup window
        dlgAlert.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {

            /*
            * If OK is pressed, delete the list
             */
            @Override
            public void onClick(DialogInterface dialog, int id) {

                // Update listNames
                listNames.remove(titleList);

                // Update Listoflists.txt
                try {
                    writeToFile(listNames, "Listoflists.txt");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // Delete file of current list
                deleteFile(textFileName);

                // go back  to main activity
                backToMain();
            }
        });

        // Enable the user to close the popup window by clicking next to it
        dlgAlert.setCancelable(true);

        // Create popup window
        dlgAlert.create().show();
    }

    /*
    * Check if a title for the list is given, if so return to MainActivity
     */
    public void backToMain(View view) {

        // Exit SingleListActivity
        backToMain();
    }

    /*
    * Go back to the main menu
     */
    public void backToMain() {

        // Set activity number back to 0 to avoid getting stuck in SingleListActivity
        SharedPreferences prefs = this.getSharedPreferences("settings", this.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("activity", 0);
        editor.commit();

        // Start MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /*
    * Save current state for in case the app is killed
     */
    public void onSaveInstanceState(Bundle outState) {

        // Read text from EditText for listname
        String editTextTitle = ((EditText) findViewById(R.id.inputListName)).getText().toString();

        // Read text from EditText for items
        String editTextItem = ((EditText) findViewById(R.id.input)).getText().toString();

        // Save EditTexts
        super.onSaveInstanceState(outState);
        outState.putString("editTextTitle", editTextTitle);
        outState.putString("editTextItem", editTextItem);
    }

    public void onRestoreInstanceState(Bundle inState) {

        // Retrieve EditTexts
        super.onRestoreInstanceState(inState);
        String editTextTitle = inState.getString("editTextTitle");
        String editTextItem = inState.getString("editTextItem");

        // Restore EditTexts
        ((EditText) findViewById(R.id.inputListName)).setText(editTextTitle);
        ((EditText) findViewById(R.id.input)).setText(editTextItem);
    }

}


