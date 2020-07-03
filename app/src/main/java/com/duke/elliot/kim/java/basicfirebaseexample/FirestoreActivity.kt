package com.duke.elliot.kim.java.basicfirebaseexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.android.synthetic.main.activity_firestore.*
import kotlinx.android.synthetic.main.activity_next.*
import kotlinx.android.synthetic.main.activity_next.button_delete
import kotlinx.android.synthetic.main.activity_next.button_query_observe
import kotlinx.android.synthetic.main.activity_next.button_query_single
import kotlinx.android.synthetic.main.activity_next.button_read_observe
import kotlinx.android.synthetic.main.activity_next.button_read_single
import kotlinx.android.synthetic.main.activity_next.button_set
import kotlinx.android.synthetic.main.activity_next.button_update
import kotlinx.android.synthetic.main.activity_next.edit_text_query_observe
import kotlinx.android.synthetic.main.activity_next.edit_text_query_single
import kotlinx.android.synthetic.main.activity_next.edit_text_set
import kotlinx.android.synthetic.main.activity_next.edit_text_update
import kotlinx.android.synthetic.main.activity_next.image_view
import kotlinx.android.synthetic.main.activity_next.text_view_query_observe
import kotlinx.android.synthetic.main.activity_next.text_view_read_observe
import kotlinx.android.synthetic.main.activity_next.text_view_read_single
import kotlinx.android.synthetic.main.activity_next.text_view_run_transaction

class FirestoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firestore)

        button_set.setOnClickListener {
            saveData()
        }

        button_update.setOnClickListener {
            updateData()
        }

        button_delete.setOnClickListener {
            deleteData()
        }

        button_read_single.setOnClickListener {
            readSingleData()
        }
        button_read_observe.setOnClickListener {
            readObserveData()
        }
        button_query_single.setOnClickListener {
            querySingleData()
        }
        button_query_observe.setOnClickListener {
            queryObserveData()
        }

        button_compound_query_1.setOnClickListener {
            compoundQuery1()
        }

        button_compound_query_2.setOnClickListener {
            compoundQuery2()
        }

        button_compound_query_3.setOnClickListener {
            compoundQuery3()
        }

        image_view.setOnClickListener {
            runTransaction()
        }
    }

    private fun saveData() {
        var setEditTextString = edit_text_set.text.toString()
        var map = mutableMapOf<String, Any>()
        map["name"] = "Elliot"
        map["gender"] = "Male"
        map["age"] = setEditTextString

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .set(map)
    }

    private fun updateData() {
        var updateEditTextString = edit_text_update.text.toString()
        var map = mutableMapOf<String, Any>()
        map["gender"] = updateEditTextString

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .update(map)
    }

    private fun deleteData() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .delete()
    }

    fun readSingleData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .get().addOnSuccessListener { documentSnapshot ->
                var map = documentSnapshot.data as Map<String,Any>
                text_view_read_single.text = map["age"].toString()
            }
    }
    fun readObserveData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                var map = documentSnapshot?.data as Map<String, Any>
                text_view_read_observe.text = map["age"].toString()
            }
    }
    fun querySingleData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("age", edit_text_query_single.text.toString())
            .get().addOnSuccessListener { querySnapshot ->
                println(querySnapshot.documents)
            }
    }
    fun queryObserveData(){
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("age", edit_text_query_observe.text.toString())
            .addSnapshotListener { qS, a ->
                var map = qS?.documents?.first()?.data as Map<String,Any>
                text_view_query_observe.text = map["name"].toString()
            }
    }

    fun compoundQuery1() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("age", edit_text_query_single.text.toString())
            .whereEqualTo("name", "Elliot")
            .get().addOnSuccessListener { querySnapshot ->
                println("call by query1 " + querySnapshot.documents)
            }
    }

    fun compoundQuery2() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereLessThan("age", "100000")
            .whereEqualTo("gender", "Male")
            .get().addOnSuccessListener { querySnapshot ->
                println("call by query2 " + querySnapshot.documents)
            }
    }

    fun compoundQuery3() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .whereGreaterThan("age", "1")
            .whereLessThan("age", "100000")
            .get().addOnSuccessListener { querySnapshot ->
                println("call by query3 " + querySnapshot.documents)
            }  // get() 메서드는 스냅샷 리스터를 등록하는 것으로 대체될 수 있다.
    }

    fun runTransaction(){
        var uid = FirebaseAuth.getInstance().uid

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(FirebaseAuth.getInstance().uid.toString())
            .runTransaction(object : Transaction.Handler{
                override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                    var p = p2?.getValue(UserModel::class.java)
                    text_view_run_transaction.text = p?.likeCount.toString()
                    if(p!!.likes.containsKey(uid)){
                        image_view.setImageResource(android.R.drawable.star_on)
                    }else{
                        image_view.setImageResource(android.R.drawable.star_off)
                    }
                }

                override fun doTransaction(p0: MutableData): Transaction.Result {
                    var p = p0.getValue(UserModel::class.java)
                    if(p == null){
                        p = UserModel()
                        p.likeCount = 1
                        p.likes[uid!!] = true
                        p0.value = p
                        return Transaction.success(p0)
                    }
                    if(p.likes.containsKey(uid)){
                        p.likeCount = p.likeCount!! - 1
                        p.likes.remove(uid)
                    }else{
                        p.likeCount = p.likeCount!! + 1
                        p.likes[uid!!] = true
                    }
                    p0.value = p

                    return Transaction.success(p0)
                }

            })
    }
}
