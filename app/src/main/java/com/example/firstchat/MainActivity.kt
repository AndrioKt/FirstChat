package com.example.firstchat

import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstchat.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var adapter : UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
        setUpActionBar()
        val database = Firebase.database("https://fisrtchat-93973-default-rtdb.europe-west1.firebasedatabase.app") //здесь хранится ссылка на базу данных (в скобках указывать ссылку если она автоматически не подхватывается из json)
        val myRef = database.getReference("message").child("Andrio") //тут берется ссылка на путь (узел)
        binding.btSend.setOnClickListener {
            myRef.child(myRef.push().key ?: "is empty").setValue(User(auth.currentUser?.displayName, binding.editMessage.text.toString()))//заполняем узел именем и сообщением пользователя
        }   //myRef.push().key генерирует уникальный ключ для каждого сообщения
        chatReader(myRef)//инициализируем чат (непрерывный слушатель)
        initRcView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {//добавялем кнопки меню в активити
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initRcView()=with(binding){
        adapter = UserAdapter()
        rcView.layoutManager = LinearLayoutManager(this@MainActivity)
        rcView.adapter = adapter
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {//функция считывания нажатий на кнопки меню
        if(item.itemId == R.id.sign_out){//если id кнопки совпадает с id выхода
            auth.signOut()//то выйти из аккаунта
            finish()//и в этом случае закрывает активити (переходим на стартовое окно регистрации)
        }
        return super.onOptionsItemSelected(item)
    }
    private fun chatReader(dRef: DatabaseReference){//создадим функцию считывани с БД. аргументом указываем ссылку на БД dRef
        dRef.addValueEventListener(object :ValueEventListener{
            //данный метод будет непрерывно прослушивать все что в БД
            override fun onDataChange(snapshot: DataSnapshot) {//когда данные изменяются
                val list = ArrayList<User>()
                for (mes in snapshot.children) {//по очереди выдает все children элементы в виде snap[shot класса
                    val user = mes.getValue(User::class.java)
                    if(user != null)list.add(user)
                }
                adapter.submitList(list)
            }

            override fun onCancelled(error: DatabaseError) {//при отмене

            }

        })
    }
        //будем использовать библиотеку picasso, для этого прописываем зависимость в gradle
        private fun setUpActionBar(){//для показа аватарки используем home button (кнопка назад в виде стрелки в левом верхнем углу)
            val ab = supportActionBar
            Thread{//в связи с нагрузкой библиотеки пикассо, используем отдельный поток для подгрузки
                val bMap = Picasso.get().load(auth.currentUser?.photoUrl).get()//сюда передаем URL ссылку на картинку, и превращаем в bitMap
                val dIcon = BitmapDrawable(resources,bMap)//далее bitMap превращаем в drawable для использования
                runOnUiThread{ //системные кнопки нельзя использовать на второстепенном потоке, поэтому мы указываем что после опереции с картинкой мы запускаем остальное на основном потоке
                    ab?.setDisplayHomeAsUpEnabled(true)//активируем кнопку
                    ab?.setHomeAsUpIndicator(dIcon)//далее передаем drawable картинку в метод отображения кнопки в виде иконки (нашего аватара)
                    ab?.title = auth.currentUser?.displayName//используем вместо отображения названия приложения - имя аккаунта
                }
            }.start()

        }

}