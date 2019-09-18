package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddTrackActivity extends AppCompatActivity {

    TextView textViewArtistsName;
    EditText editTextTrackName;
    SeekBar seekBarRating;
    Button buttonAddTraack;

    ListView listViewTrack;

    DatabaseReference databaseTrack;

    List<Track> tracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_track);

        textViewArtistsName = (TextView)findViewById(R.id.textViewArtistsName);
        editTextTrackName = (EditText) findViewById(R.id.editTextTrackName);
        seekBarRating = (SeekBar) findViewById(R.id.seekBarRating );
        listViewTrack = (ListView)findViewById(R.id.ListViewTrack);
        buttonAddTraack = (Button)findViewById(R.id.btnAddTrack);

        Intent intent = getIntent();

        tracks =  new ArrayList<>();
        String id = intent.getStringExtra(MainActivity.ARTIST_ID);
        String name= intent.getStringExtra(MainActivity.ARTIST_NAME);

        textViewArtistsName.setText(name);

        databaseTrack = FirebaseDatabase.getInstance().getReference("tracks").child(id);

        buttonAddTraack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTrack();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseTrack.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tracks.clear();

                for (DataSnapshot trackSnapshot : dataSnapshot.getChildren()) {
                    Track track = trackSnapshot.getValue(Track.class);
                    tracks.add(track);
                }
                TrackList trackListAdapter = new TrackList(AddTrackActivity.this, tracks);
                listViewTrack.setAdapter(trackListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveTrack(){
        String trackName = editTextTrackName.getText().toString().trim();
        int rating = seekBarRating.getProgress();

        if (!TextUtils.isEmpty(trackName)) {
            String id = databaseTrack.push().getKey();
            Track track = new Track(id, trackName, rating);

            databaseTrack.child(id).setValue(track);

            Toast.makeText(this, "Track saved succesfully", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Track name should not be empty", Toast.LENGTH_LONG).show();
        }
    }
}
