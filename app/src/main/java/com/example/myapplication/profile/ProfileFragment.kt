package com.example.myapplication.profile

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView


class ProfileFragment : Fragment() {
    
    lateinit var imageUser: CircleImageView
    lateinit var nickname: EditText
    lateinit var phone: TextView
    lateinit var dob: TextView
    lateinit var saveButton: Button
    
    private var uriImg: Uri? = null //путь к картинке
    
    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        //отвечает за выбор картинки из папки выбор картинки
        
        uriImg = it
        imageUser.setImageURI(uriImg) //устанавливаем в юзера нашу картинку(передаем)
    }
    
    lateinit var builder: AlertDialog //Вызываем диалогове окно
    
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        imageUser = view.findViewById(R.id.image_user_prof)
        nickname = view.findViewById(R.id.nickname_input_profile)
        phone = view.findViewById(R.id.phone_out)
        dob = view.findViewById(R.id.dob_out)
        saveButton = view.findViewById(R.id.save_btn)
        saveButton.setOnClickListener {
            update()
        }
        val firebaseAuth = FirebaseAuth.getInstance()
        getUserData(firebaseAuth.currentUser?.phoneNumber)
        return view
        
        
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        imageUser.setOnClickListener {
            selectImage.launch("image/*")
        }
        
        }
    
    
    
    private fun storeData(uri: Uri?) {//экземпляр класса пользователя
        val newUser = User( //новый тип класса пользователя
            FirebaseAuth.getInstance().currentUser!!.uid,
            FirebaseAuth.getInstance().currentUser!!.phoneNumber!!,
            nickname.text.toString(),
            dob.text.toString(),
            uri.toString()
        )
        
        FirebaseDatabase.getInstance().getReference("users") //табличка, где лежат все пользователи
            .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!)
//            .setValue(newUser).addOnCompleteListener { //добавление нового пользователя в БД
//                builder.dismiss()
//                if (it.isSuccessful) {
//                    Toast.makeText(requireContext(), "Успешная регистрация", Toast.LENGTH_SHORT).show()
//                    val intent = Intent(requireContext(), MainActivity::class.java) //запускаем новыую активити
//                    startActivity(intent)
//                    finish()
//                } else {
//                    Toast.makeText(requireContext(), "${it.exception!!.message}", Toast.LENGTH_SHORT).show()
//                    Log.e("err2", it.exception!!.message.toString())
//                }
//            }.addOnFailureListener {
//                Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
//                Log.e("err3", it.message.toString())
//            }
        
    }
    
    //Выгрузка данных из Firebase
    private fun getUserData(phoneNumber: String?) {
        FirebaseDatabase.getInstance().getReference("users").child(phoneNumber!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var user = snapshot.getValue(User::class.java)!!
                    nickname.setText(user.userName)
                    phone.text = user.phoneNumber
                    dob.text = user.dob
                    Glide.with(context!!).load(user.imageUser).into(imageUser)
                }
                
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
                
            })
        
        
    }
    
    //Обновление пользователя
    fun update() {
        val phone = Firebase.auth.currentUser?.phoneNumber
        if (phone != null) {
            var image: Uri? = null
            val storageRef = FirebaseStorage.getInstance().getReference("profile") //папка
                .child(FirebaseAuth.getInstance().currentUser!!.uid) //ребенок(подпапка)
                .child("profile.jpg")
    
            storageRef.putFile(uriImg!!) //profile.jpg закидываем нашу картинку сюда
                .addOnSuccessListener {
            
            
                    storageRef.downloadUrl
                        .addOnSuccessListener {
                            Log.d("AAA photo", it.toString())
                            storeData(it)
                            FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(phone)
                                .apply {
//                    child(User::userName.name)
//                        .setValue(nickname.text.toString())
                                    child(User::imageUser.name)
                                        .setValue(it.toString())
                                    /*child(User::phoneNumber.name)
                                        .setValue(phone)
                                    child(User::dob.name)
                                        .setValue(dob.text)*/
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT)
                                .show()
                            Log.e("err", it.message.toString())
                        }
                }
                .addOnFailureListener {
                    builder.dismiss()
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                    Log.e("err1", it.message.toString())
                }
            Log.d("AAA image", image.toString())
            
        }
        
    }
    
}