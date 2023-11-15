package com.example.multipleimage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony.Mms.Intents
import android.view.View
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private var images = arrayListOf<ImageModel>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter
    private lateinit var selectBt: AppCompatButton
    private lateinit var deleteBt: AppCompatButton
    private lateinit var chack: ImageView
    private var downloadUrl: Uri? = null
    private val fireStore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getData()

        recyclerView = findViewById(R.id.recycler)
        selectBt = findViewById(R.id.selectBt)
        deleteBt = findViewById(R.id.deleteBt)
        selectBt.visibility = View.VISIBLE
        adapter = Adapter(images,this,fireStore,deleteBt,selectBt)


        recyclerView.layoutManager = GridLayoutManager(this,2)
        recyclerView.adapter = adapter

        selectBt.setOnClickListener {
            selectImages()
        }
        val builder = AlertDialog.Builder(this)
        builder.setPositiveButton(
            "Yes",
            DialogInterface.OnClickListener { dialogInterface, i ->
                adapter.removeSelectedItems()
            })
        builder.setNegativeButton(
            "No",
            DialogInterface.OnClickListener { dialogInterface, i ->

            })




        deleteBt.setOnClickListener {
            val dialog = builder.create()
            dialog.setTitle("Delete?")
            dialog.setMessage("Are you sure you want to delete selected file(s)?")
            dialog.show()
        }




    }

    private fun selectImages() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, 123);
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count) {
                        val imageUri = data.clipData?.getItemAt(i)?.uri
                        if (imageUri != null) {
                            val ref =
                                storage.getReference("images").child(UUID.randomUUID().toString())
                            ref.putFile(imageUri)
                                .addOnSuccessListener {

                                    ref.downloadUrl.addOnSuccessListener { uri ->
                                        downloadUrl = uri
                                        insert(downloadUrl.toString())
                                    }

                                }


                        }
                    }

                } else {
                    val imageUri = data?.data
                    if (imageUri != null) {
                        val ref = storage.getReference("images").child(UUID.randomUUID().toString())
                        ref.putFile(imageUri)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                                ref.downloadUrl.addOnSuccessListener { uri ->
                                    downloadUrl = uri
                                    insert(downloadUrl.toString())
                                }

                            }
                    }
                }
            }
        }


    }

    private fun insert(uri: String) {
        val id = UUID.randomUUID().toString()
        val data = ImageModel(uri,id)
        fireStore.collection("images").document(id).set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Image uploaded on database", Toast.LENGTH_SHORT).show()
                val intent = Intent(this,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getData() {
        fireStore.collection("images").get()
            .addOnSuccessListener {
                val data = it.toObjects(ImageModel::class.java)
                images.addAll(data)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }


}