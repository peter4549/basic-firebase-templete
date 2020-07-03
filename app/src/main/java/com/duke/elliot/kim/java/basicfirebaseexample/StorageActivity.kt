package com.duke.elliot.kim.java.basicfirebaseexample

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_storage.*
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.*

class StorageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        button_upload_photo.setOnClickListener {
            openAlbum()
        }

        button_delete_photo.setOnClickListener {
            deletePhoto()
        }

        button_upload_file.setOnClickListener {
            openContent()
        }

        button_load_photo.setOnClickListener {
            val storageRef =
                FirebaseStorage.getInstance().reference.child("images")
                    .child(edit_text_load_filename.text.toString())
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                loadPhoto(uri.toString())
            }
        }

        button_download_photo.setOnClickListener {
            val storageRef =
                FirebaseStorage.getInstance().reference.child("images")
                    .child(edit_text_download_filename.text.toString())
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                //DownLoadFileFromUri().execute(uri.toString())
                downLoadFileFromUri(uri.toString())
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }

    fun openContent() {
        var intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE_CONTENT)
    }

    fun uploadFile(fileUri: Uri) {
        // Using original filename
        var metaCursor =
            contentResolver.query(fileUri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                null, null, null)!!
        metaCursor.moveToFirst()
        var fileName = metaCursor.getString(0)
        metaCursor.close()

        var storageRef =
            FirebaseStorage.getInstance().reference.child("files").child(fileName)

        storageRef.putFile(fileUri).continueWithTask {
            Toast.makeText(this, "File uploaded", Toast.LENGTH_SHORT).show()

            return@continueWithTask storageRef.downloadUrl
        }.addOnSuccessListener { uri ->
            println("URIHERE" + uri.toString())
        }
    }

    fun openAlbum() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    fun uploadPhoto(photoUri: Uri) {
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
        val fileName = "Image_" + timestamp + "_.png"
        var storageRef =
            FirebaseStorage.getInstance().reference.child("images").child(fileName)

        storageRef.putFile(photoUri).continueWithTask {
            Toast.makeText(this, "Photo uploaded", Toast.LENGTH_SHORT).show()

            return@continueWithTask storageRef.downloadUrl
        }.addOnSuccessListener { uri ->
            println("URIHERE" + uri.toString())
        }
    }

    fun deletePhoto() {
        FirebaseStorage.getInstance().reference.child("images")
            .child(edit_text_filename.text.toString()).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Photo Deleted", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadPhoto(downloadUri: String) {
        Glide.with(this).load(downloadUri).into(image_view)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_GALLERY) {
            val photoUri = data?.data!!
            image_view.setImageURI(photoUri)
            // 선택하면 콜백하는 형태인듯?
            uploadPhoto(photoUri)
        } else if (requestCode == REQUEST_CODE_CONTENT) {
            var fileUri = data?.data!!
            uploadFile(fileUri)
        }
    }

    fun downLoadFileFromUri(uri: String?) {
        runBlocking {
            withContext(Dispatchers.IO) {
                var downloadUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                var url = URL(uri)
                var connection = url.openConnection()
                connection.connect()

                // input stream to read file - 8k buffer
                var input = BufferedInputStream(url.openStream(), 8192)

                // output stream to write file
                var output = FileOutputStream(downloadUri + "wendyFile.png")
                var data = ByteArray(1024)
                var total = 0L

                // writing data to file
                var count: Int

                while(input.read(data).also {count = it} != -1){
                    total += count.toLong()

                    output.write(data, 0, count)
                }

                // closing output and streams
                output.flush()
                output.close()
                input.close()
            }
        }
        Toast.makeText(this@StorageActivity, "Download complete", Toast.LENGTH_SHORT).show()
    }

    inner class DownLoadFileFromUri : AsyncTask<String, Void, Void>() {
        override fun doInBackground(vararg p0: String?): Void? {
            var downloadUri = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()

            var url = URL(p0[0])
            var connection = url.openConnection()
            connection.connect()

            // input stream to read file - 8k buffer
            var input = BufferedInputStream(url.openStream(), 8192)

            // output stream to write file
            var output = FileOutputStream(downloadUri + "wendyFile.png")
            var data = ByteArray(1024)
            var total = 0L

            // writing data to file
            var count: Int

            while(input.read(data).also {count = it} != -1){
                total += count.toLong()

                output.write(data, 0, count)
            }

            // closing output and streams
            output.flush()
            output.close()
            input.close()

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Toast.makeText(this@StorageActivity, "Download complete", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_GALLERY = 0
        private const val REQUEST_CODE_CONTENT = 1
    }
}
