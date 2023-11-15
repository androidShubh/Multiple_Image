package com.example.multipleimage

import android.content.Context

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast

import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore


class Adapter(
    private val dataArrayList: ArrayList<ImageModel>,
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val deleteBt: AppCompatButton,
    private val selectBt: AppCompatButton,

    ) :
    RecyclerView.Adapter<Adapter.MyViewHolder>() {
    private var isSelected: Boolean = false
    private val selectedItems = arrayListOf<ImageModel>()


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val showImage: ShapeableImageView = itemView.findViewById(R.id.showImage)


    }

    fun removeSelectedItems() {
        var i = 1
        for (p in selectedItems.reversed()) {
            val id = p.id

            firestore.collection("images").document(id.toString())
                .delete().addOnSuccessListener {
                    Toast.makeText(context, "No - $i Item deleted", Toast.LENGTH_SHORT).show()
                    i++
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                    context.startActivity(intent)
                }
            notifyDataSetChanged()
        }
        selectedItems.clear()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val row = LayoutInflater.from(parent.context).inflate(R.layout.image_item, null, false)
        return MyViewHolder(row)
    }

    override fun getItemCount(): Int {
        return dataArrayList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(Uri.parse(dataArrayList[position].image)).into(holder.showImage)

        holder.itemView.setOnLongClickListener {
            isSelected = true
            if (selectedItems.contains(dataArrayList[position])) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                selectedItems.remove(dataArrayList[position])
            } else {
                holder.itemView.setBackgroundResource(R.color.primary)
                selectedItems.add(dataArrayList[position])
            }

            if (selectedItems.size == 0) {
                isSelected = false

                deleteBt.visibility = View.GONE
                selectBt.visibility = View.VISIBLE
            } else {
                selectBt.visibility = View.GONE
                deleteBt.visibility = View.VISIBLE

            }


            true
        }
        holder.itemView.setOnClickListener {
            if (isSelected) {
                if (selectedItems.contains(dataArrayList[position])) {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT)
                    selectedItems.remove(dataArrayList[position])
                } else {
                    holder.itemView.setBackgroundResource(R.color.primary)
                    selectedItems.add(dataArrayList[position])
                }
                if (selectedItems.size == 0) {
                    isSelected = false

                    deleteBt.visibility = View.GONE
                    selectBt.visibility = View.VISIBLE
                } else {
                    selectBt.visibility = View.GONE
                    deleteBt.visibility = View.VISIBLE

                }


            }

        }


    }
}