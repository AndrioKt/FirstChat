package com.example.firstchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.firstchat.databinding.ActivityMainBinding
import com.example.firstchat.databinding.ActivitySignInLauncherBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInLauncher : AppCompatActivity() {
    lateinit var launcher: ActivityResultLauncher<Intent>//создаем лаунчер
    lateinit var auth: FirebaseAuth//создаем переменную встроенного класса авторизации
    lateinit var binding: ActivitySignInLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySignInLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth//инициализируем аутентификацию
        launcher= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){//инициализируем лаунчер
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)//достаем из этой функции наш аккаунт в переменную
            try {
                val account = task.getResult(ApiException::class.java)//будем доставать результат и отслеживать ошибки ApiException
                if (account != null) firebaseAuth(account.idToken!!)//получаем токен аккаунта
            }catch (e: ApiException){
                Log.d("MyLog","Api error")
            }
        }

        binding.btSignIn.setOnClickListener { //создадим слушатель на кнопку авторизации
            googleSignIn()
        }
        checkAuthState()//при открытии приложения сразу проверяем. Если уже зарегестрированы, то сразху переходить к майн активити, если нет то на окно регистрации
    }

    private fun getClient() : GoogleSignInClient{
        val gso = GoogleSignInOptions//настраиваем запрос, по которому прит входе выдаст окошко авторизации
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun googleSignIn(){
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)//запускаем лаунчер для авторизации (который вернет результат авторизации)
    }

    private fun firebaseAuth(idToken:String){//функция авторизации по токену
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { //фызываем авторизацию по токену, с проверкой правильности (addOnCompleteListener)
            if (it.isSuccessful) {
                Log.d("MyLog", "Google signIn is done")
                checkAuthState()
            }
            else Log.d("MyLog","Google sign In error")
        }
    }

    private fun checkAuthState() {//функция провеннрки аутентификации
        if (auth.currentUser != null) {//если не равно null то уже залогинен
            val i = Intent(
                this,
                MainActivity::class.java)//если уже зарегестрированы то переходимм к майн активити
                startActivity(i)
        }
    }
}