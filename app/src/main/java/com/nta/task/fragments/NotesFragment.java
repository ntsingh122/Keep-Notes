package com.nta.task.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nta.task.R;
import com.nta.task.adapters.NotesAdapter;
import com.nta.task.database.DatabaseHelper;
import com.nta.task.models.Note;
import com.nta.task.utils.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;


public class NotesFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private FloatingActionButton addNotesButton;
    private ExtendedFloatingActionButton signOutButton;
    private DatabaseHelper databaseHelper;
    private List<Note> noteList = new ArrayList<>();
    private Context mContext;
    private ProgressBar loadingBar;
    private static final String USER_ID = "userId";
    private static final String USER_EMAIL = "userEmail";

    // TODO: Rename and change types of parameters
    private String userId;
    private String userEmail;
    private GoogleSignInClient mGoogleSignInClient;

    public NotesFragment() {
        // Required empty public constructor
    }

    public static NotesFragment newInstance(@NonNull String userId,@NonNull String userEmail) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        args.putString(USER_EMAIL, userEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);

        userId = getArguments().getString(USER_ID);
        userEmail = getArguments().getString(USER_EMAIL);
        databaseHelper = new DatabaseHelper(mContext);
        noteList.addAll(databaseHelper.getAllNotes(userId));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.notes_RV);
        loadingBar = view.findViewById(R.id.loading_note);
        addNotesButton = view.findViewById(R.id.addNotes_FB);
        notesAdapter = new NotesAdapter(mContext,noteList);
        signOutButton = view.findViewById(R.id.signout_fb);
        setUpRecyclerView();
        addClickListeners();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void addClickListeners() {
        addNotesButton.setOnClickListener(view -> addNoteDialog());
        signOutButton.setOnClickListener(view -> signOut());
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));
    }


    private void signOut() {
        loadingBar.setVisibility(View.VISIBLE);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        loadingBar.setVisibility(View.INVISIBLE);
                        replaceFragment(LoginFragment.newInstance());
//                       finish();
                    }
                });
    }


    public void replaceFragment(Fragment someFragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, someFragment);
        transaction.commit();
    }

    private void addNoteDialog() {
        showNoteDialog(false, null, -1);
    }

    private void setUpRecyclerView() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(notesAdapter);
    }

    private void createNote(String note) {
        long id = databaseHelper.insertNote(note,userId);

        Note n = databaseHelper.getNote(id,userId);

        if (n != null) {
            noteList.add(0, n);

            notesAdapter.notifyDataSetChanged();

        }
    }


    private void updateNote(String note, int position) {
        Note n = noteList.get(position);
        n.setNote(note);

        databaseHelper.updateNote(n,userId);

        noteList.set(position, n);
        notesAdapter.notifyItemChanged(position);

    }

    private void deleteNote(int position) {
        databaseHelper.deleteNote(noteList.get(position),userId);

        noteList.remove(position);
        notesAdapter.notifyItemRemoved(position);

    }

    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, noteList.get(position), position);
                } else {
                    deleteNote(position);
                }
            }
        });
        builder.show();
    }


    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mContext);
        View view = layoutInflaterAndroid.inflate(R.layout.add_note, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mContext);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? "Add note" : "Update note ");

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(mContext, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                if (shouldUpdate && note != null) {
                    updateNote(inputNote.getText().toString(), position);
                } else {
                    createNote(inputNote.getText().toString());
                }
            }
        });
    }
}