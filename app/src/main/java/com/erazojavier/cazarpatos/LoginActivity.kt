package com.erazojavier.cazarpatos

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    // //=================Manejo de Archivos=================
    lateinit var manejadorArchivo: FileHandler
    lateinit var manejadorArchivoExterno: FileHandler
    lateinit var checkBoxRecordarme: CheckBox
    //========================================================

    lateinit var editTextEmail: EditText
    lateinit var editTextPassword:EditText
    lateinit var buttonLogin: Button
    lateinit var buttonNewUser:Button
    lateinit var mediaPlayer: MediaPlayer
    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Inicialización de variables
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonNewUser = findViewById(R.id.buttonNewUser)
        // Initialize Firebase Auth
        auth = Firebase.auth

        // //=================Manejo de Archivos=================
        manejadorArchivo = EncryptedSharedPreferencesManager(this)
        checkBoxRecordarme = findViewById(R.id.checkBoxRecordarme)
        LeerDatosDePreferencias()

        //Grabar en un archivo los datos
        manejadorArchivoExterno = FileExternalManager(this)
        //========================================================


        //Eventos clic
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val clave = editTextPassword.text.toString()
            //Validaciones de datos requeridos y formatos
            if(!ValidarDatosRequeridos())
                return@setOnClickListener

            //Guardar datos en preferencias.
            GuardarDatosEnPreferencias()
            /*
            //Si pasa validación de datos requeridos, ir a pantalla principal
            val intencion = Intent(this, MainActivity::class.java)
            intencion.putExtra(EXTRA_LOGIN, email)
            startActivity(intencion)*/
            AutenticarUsuario(email, clave)
        }
        buttonNewUser.setOnClickListener{
            val intencion = Intent(this, CrearUsuarioActivity::class.java)
            intencion.putExtra(EXTRA_LOGIN, auth.currentUser!!.email)
            startActivity(intencion)
        }
        mediaPlayer=MediaPlayer.create(this, R.raw.title_screen)
        mediaPlayer.start()
    }


    private fun ValidarDatosRequeridos():Boolean{
        val email = editTextEmail.text.toString()
        val clave = editTextPassword.text.toString()
        if (email.isEmpty()) {
            editTextEmail.setError("El email es obligatorio")
            editTextEmail.requestFocus()
            return false
        }
        if (clave.isEmpty()) {
            editTextPassword.setError("La clave es obligatoria")
            editTextPassword.requestFocus()
            return false
        }
        if (clave.length < 3) {
            editTextPassword.setError("La clave debe tener al menos 3 caracteres")
            editTextPassword.requestFocus()
            return false
        }
        return (this.validarCampos(clave,email))
//        return true
    }
    override fun onDestroy() {
        mediaPlayer.release()
        super.onDestroy()
    }
    // //=================Manejo de Archivos=================
    private fun LeerDatosDePreferencias(){
        val listadoLeido = manejadorArchivo.ReadInformation()
        if(listadoLeido.first != null){
            checkBoxRecordarme.isChecked = true
        }
        editTextEmail.setText ( listadoLeido.first )
        editTextPassword.setText ( listadoLeido.second )
    }

    private fun GuardarDatosEnPreferencias() {
        val email = editTextEmail.text.toString()
        val clave = editTextPassword.text.toString()
        val listadoAGrabar: Pair<String, String>
        if(checkBoxRecordarme.isChecked){
            listadoAGrabar = email to clave
        }
        else{
            listadoAGrabar ="" to ""
        }
        manejadorArchivo.SaveInformation(listadoAGrabar)
        manejadorArchivoExterno.SaveInformation((listadoAGrabar))
        println(manejadorArchivoExterno.ReadInformation())
    }
    //========================================================
    //==============================EXAMEN MÉTODOS============================================

    private fun  validarCorreo(correo: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        if (!pattern.matcher(correo).matches()){
            editTextEmail.error = "El correo no es válido"
            editTextEmail.requestFocus()
            return false
        }
        return true
    }

    private fun validarLongitudContrasena(contrasena: String, minCaract: Int): Boolean{
        if(contrasena.length < minCaract){
            editTextPassword.error = "Debe tener mínimo $minCaract caracteres"
            editTextPassword.requestFocus()
            return false
        }
        return true
    }

    private fun validarCampos(contrasena: String, correo:String): Boolean {
        return (this.validarCorreo(correo)  &&
                this.validarLongitudContrasena(contrasena, 8))
    }

    //Autenticacion con firebase

    fun AutenticarUsuario(email:String, password:String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(EXTRA_LOGIN, "signInWithEmail:success")

                //Si pasa validación de datos requeridos, ir a pantalla principal
                val intencion = Intent(this, MainActivity::class.java)
                intencion.putExtra(EXTRA_LOGIN, auth.currentUser!!.email)
                startActivity(intencion)
                //finish()
            } else {
                Log.w(EXTRA_LOGIN, "signInWithEmail:failure", task.exception)
                Toast.makeText(baseContext, task.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }

    }
}

