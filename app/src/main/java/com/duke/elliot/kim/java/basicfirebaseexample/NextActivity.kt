package com.duke.elliot.kim.java.basicfirebaseexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_next.*

class NextActivity : AppCompatActivity() {

    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next)

        var googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        button_logout.setOnClickListener {
            logout()
        }

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

        // Make a crush, this reported to crashlytics
        button_crush.setOnClickListener {
            var a: String? = null
            a!!.length
        }

        button_firestore.setOnClickListener {
            startActivity(Intent(this, FirestoreActivity::class.java))
        }

        button_storage.setOnClickListener {
            startActivity(Intent(this, StorageActivity::class.java))
        }

        image_view.setOnClickListener {
            runTransaction()
        }

        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            val token = instanceIdResult.token
            println("TOKEN: $token")
        }

    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()

        // Google session out
        googleSignInClient?.signOut()

        // Facebook session out
        LoginManager.getInstance().logOut()

        finish()
    }

    private fun saveData() {
        var setEditTextString = edit_text_set.text.toString()
        var map = mutableMapOf<String, Any>()
        map["name"] = "Elliot"
        map["age"] = setEditTextString

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(FirebaseAuth.getInstance().uid.toString())
            .setValue(map)
    }

    private fun updateData() {
        var updateEditTextString = edit_text_update.text.toString()
        var map = mutableMapOf<String, Any>()
        map["gender"] = updateEditTextString

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(FirebaseAuth.getInstance().uid.toString())
            .updateChildren(map)
    }

    private fun deleteData() {
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(FirebaseAuth.getInstance().uid.toString())
            .removeValue()
    }

    fun readSingleData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(FirebaseAuth.getInstance().uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var map = p0.value as Map<String,Any>
                    text_view_read_single.text = map["age"].toString()
                }

            })
    }
    fun readObserveData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(FirebaseAuth.getInstance().uid.toString())
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var map = p0.value as Map<String,Any>
                    text_view_read_observe.text = map["age"].toString()
                }

            })
    }
    fun querySingleData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .orderByChild("age").equalTo(edit_text_query_single.text.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var map = p0.children.first().value as Map<String,Any>
                    text_view_query_single.text = map["name"].toString()
                }

            })
    }
    fun queryObserveData(){
        FirebaseDatabase.getInstance().reference
            .child("users")
            .orderByChild("age").equalTo(edit_text_query_observe.text.toString())
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var map = p0.children.first().value as Map<String,Any>
                    text_view_query_observe.text = map["name"].toString()
                }

            })
    }
    fun runTransaction(){
        var uid = FirebaseAuth.getInstance().uid

        FirebaseDatabase.getInstance().reference
            .child("users")
            .child("1")
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
