package com.neon.savr;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.neon.savr.Model.Data;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExpenseFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;

    private RecyclerView recyclerView;

    private TextView expenseTotalSum;

    private EditText edtAmount, edtType, edtNote;

    private Button btnUpdate, btnDelete;

    private String type, note, post_key;
    private float amount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_expense, container, false);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("ExpenseData").child(uid);

        expenseTotalSum = myview.findViewById(R.id.expense_txt_result);


        recyclerView = myview.findViewById(R.id.recycler_id_expense);

        LinearLayoutManager layoutManager =new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                float totalvalue = 0;

                for (DataSnapshot mysnapshot:snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);
                    totalvalue+=data.getAmount();

                    String strTotalValue = String.valueOf(totalvalue);

                    expenseTotalSum.setText(strTotalValue);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        return myview;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data, MyViewHolder>adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (
                        Data.class,
                        R.layout.expense_recycler_data,
                        MyViewHolder.class,
                        mExpenseDatabase
                ) {
            @Override
            protected void populateViewHolder(MyViewHolder myViewHolder, final Data model, final int i) {
                myViewHolder.setType(model.getType());
                myViewHolder.setNote(model.getNote());
                myViewHolder.setDate(model.getDate());
                myViewHolder.setAmount(model.getAmount());

                myViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        post_key = getRef(i).getKey();
                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();
                        updateDataItem();
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View mView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        private void setType(String type){
            TextView mType = mView.findViewById(R.id.type_txt_expense);
            mType.setText(type);
        }
        private void setNote(String note){
            TextView mNote = mView.findViewById(R.id.note_txt_expense);
            mNote.setText(note);
        }
        private void setDate(String date){
            TextView mDate = mView.findViewById(R.id.date_txt_expense);
            mDate.setText(date);
        }
        private void setAmount(float amount){
            TextView mAmount = mView.findViewById(R.id.amount_txt_expense);
            String stramount = String.valueOf(amount);
            mAmount.setText(stramount);
        }

    }

    private void updateDataItem(){

        AlertDialog.Builder myDialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myView = inflater.inflate(R.layout.update_data_item, null);

        myDialog.setView(myView);

        edtAmount = myView.findViewById(R.id.amount_edt);
        edtType = myView.findViewById(R.id.type_edt);
        edtNote = myView.findViewById(R.id.note_edt);

        btnUpdate = myView.findViewById(R.id.btn_update);
        btnDelete = myView.findViewById(R.id.btn_delete);

        final AlertDialog dialog = myDialog.create();

        edtType.setText(type);
        edtType.setSelection(type.length());

        edtNote.setText(note);
        edtNote.setSelection(note.length());

        edtAmount.setText(String.valueOf(amount));
        edtAmount.setSelection(String.valueOf(amount).length());

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = edtType.getText().toString().trim();
                note = edtNote.getText().toString().trim();

                String mdamount = String.valueOf(amount);
                mdamount = edtAmount.getText().toString().trim();
                float myAmount = Float.parseFloat(mdamount);

                String mDate = DateFormat.getDateInstance().format(new Date());
                Data data = new Data(myAmount, type, note, post_key, mDate);

                mExpenseDatabase.child(post_key).setValue(data);
                dialog.dismiss();
                Toast.makeText(getActivity(), "Data updated!", Toast.LENGTH_SHORT).show();


            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mExpenseDatabase.child(post_key).removeValue();

                dialog.dismiss();
            }
        });
        dialog.show();


    }

}
