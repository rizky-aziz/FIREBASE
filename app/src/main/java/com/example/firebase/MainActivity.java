package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String ARTIST_NAME = "artistname";
    public static final String ARTIST_ID= "artistid";

    EditText editTextName;
    Button buttonAdd, buttonDelete;
    Spinner spinnerGenres;

    DatabaseReference databaseArtist;

    ListView listViewArtists;

    List<Artist> artists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseArtist = FirebaseDatabase.getInstance().getReference("artist");

        editTextName = (EditText)findViewById(R.id.editTextName);
        buttonAdd = (Button)findViewById(R.id.btnAddArtist);
        spinnerGenres = (Spinner)findViewById(R.id.spinnerGenres);
        buttonDelete = (Button)findViewById(R.id.buttonDelete);

        artists =  new ArrayList<>();
        listViewArtists = (ListView)findViewById(R.id.ListViewArtists);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addArtist();
            }
        });



        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = artists.get(i);

                Intent intent = new Intent(getApplicationContext(), AddTrackActivity.class);
                intent.putExtra(ARTIST_ID, artist.getArtistId());
                intent.putExtra(ARTIST_NAME, artist.getArtistName());

                startActivity(intent);
            }
        });

        listViewArtists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = artists.get(i);

                showUpdateDialog(artist.getArtistId(), artist.getArtistName());
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseArtist.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                artists.clear();
                for (DataSnapshot artistSnapshot : dataSnapshot.getChildren()){
                    Artist artist = artistSnapshot.getValue(Artist.class);

                    artists.add(artist);
                }

                ArtistList adapter = new ArtistList(MainActivity.this, artists);
                listViewArtists.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUpdateDialog(final String artistsId, String artistsName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        final  View dialogView = inflater.inflate(R.layout.update_dialog, null);

        dialogBuilder.setView(dialogView);

        final  EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final  Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdate);
        final  Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDelete);
        final  Spinner spinnerGenres = (Spinner) dialogView.findViewById(R.id.spinnerGenre);

        dialogBuilder.setTitle("Update Artist "+artistsName);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                String genre = spinnerGenres.getSelectedItem().toString();

                if (TextUtils.isEmpty(name)) {
                    editTextName.setError("Name required");
                    return;
                }
                updateArtist(artistsId, name, genre);

                alertDialog.dismiss();
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteArtist(artistsId);

            }
        });
    }

    public void DeleteArtist(String artisId) {
        DatabaseReference drArtist = FirebaseDatabase.getInstance().getReference("artist").child(artisId);
        DatabaseReference drTracks = FirebaseDatabase.getInstance().getReference("tracks").child(artisId);

        drArtist.removeValue();
        drTracks.removeValue();

        Toast.makeText(this, "Artist Delete", Toast.LENGTH_LONG).show();
    }

    private boolean updateArtist(String id, String name, String genre) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("artist").child(id);

        Artist artist = new Artist(id, name, genre);

        databaseReference.setValue(artist);

        Toast.makeText(this, "Artist Update Succesfully", Toast.LENGTH_LONG).show();
        return true;
    }

    private void addArtist(){
        String name = editTextName.getText().toString().trim();
        String genre = spinnerGenres.getSelectedItem().toString();

        if (!TextUtils.isEmpty(name)) {

            String id = databaseArtist.push().getKey();

            Artist artist = new Artist(id, name, genre);

            databaseArtist.child(id).setValue(artist);

            Toast.makeText(this, "Artis added", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "You should enter a name", Toast.LENGTH_LONG).show();
        }
    }

    private boolean deleteArtist(String id) {

        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("artist").child(id);

        dR.removeValue();

        DatabaseReference drTracks = FirebaseDatabase.getInstance().getReference("tracks").child(id);

        drTracks.removeValue();
        Toast.makeText(getApplicationContext(), "Artist Deleted", Toast.LENGTH_LONG).show();

        return true;
    }
}
