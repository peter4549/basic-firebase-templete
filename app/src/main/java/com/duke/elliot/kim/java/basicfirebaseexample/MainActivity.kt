package com.duke.elliot.kim.java.basicfirebaseexample

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {

    var googleSignInClient: GoogleSignInClient? = null
    var callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        printHashKey(this)

        button_email_login.setOnClickListener {
            loginEmail()
        }

        button_facebook_login.setOnClickListener {
            facebookLogin()
        }

        button_google_login.setOnClickListener {
            var signInIntent = googleSignInClient?.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        button_sign_up.setOnClickListener {
            createEmailId()
        }

        // 3600 초 써라 실제쓸때는.
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.fetch(0).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseRemoteConfig.fetchAndActivate()
                dialogDisplay(firebaseRemoteConfig)
            }
        }
    }

    fun dialogDisplay(firebaseRemoteConfig: FirebaseRemoteConfig) {
        var showDialog = firebaseRemoteConfig.getBoolean("message_caps")
        var message = firebaseRemoteConfig.getString("message_content")

        if (showDialog) {
            AlertDialog.Builder(this)
                .setTitle("Update")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
                    // this.finish()
                }).show()
        }
    }

    override fun onResume() {
        super.onResume()
        moveNextPage()
    }

    fun moveNextPage() {
        var currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, NextActivity::class.java))
        }
    }

    fun printHashKey(pContext: Context) {
        try {
            val info: PackageInfo = pContext.packageManager
                .getPackageInfo(pContext.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                println("printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {

        } catch (e: Exception) {

        }
    }

    fun facebookLogin() {
        LoginManager.getInstance().loginBehavior = LoginBehavior.WEB_VIEW_ONLY
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                firebaseAuthWithFacebook(result)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    fun createEmailId() {
        var email = edit_text_email.text.toString()
        var password = edit_text_password.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    moveNextPage()
                    println("Sign up Success")
                }
            }
    }

    fun loginEmail() {
        var email = edit_text_email.text.toString()
        var password = edit_text_password.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    moveNextPage()
                    println("Sign up Success")
                }
            }
    }

    private fun firebaseAuthWithFacebook(result: LoginResult?) {
        var credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful)
                moveNextPage()
                println("Facebook Login Success")
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful)
                moveNextPage()
                println("Google Login Success")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            var account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        }
    }

    companion object {
        const val RC_SIGN_IN = 1000
    }
}
