package com.example.maps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //Declaracion y asignacion de los elementos de la interfaz a variables locales
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        //Instancia de FirebaseAuth para gestionar el registro y login de usuarios
        val auth = FirebaseAuth.getInstance()

        //Configuracion del listener para el boon de registro

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password =  passwordEditText.text.toString().trim()
            //Validaciones: se asegura que los campos de email y contraseña no esten vacios
            if(email.isEmpty()){
                emailEditText.error = "El correo electronico es obligatorio"
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            if(password.isEmpty()){
                passwordEditText.error = "La contraseña es obligatoria"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            if(password.length<6){
                passwordEditText.error = "La contraseña debe tener al menos 6 caracteres"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            //LLama  al metodo de Firebase para crear un nuevo usuario con email y contraseña
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Error en el registro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password =  passwordEditText.text.toString().trim()
            //Validaciones: se asegura que los campos de email y contraseña no esten vacios
            if(email.isEmpty()){
                emailEditText.error = "El correo electronico es obligatorio"
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            if(password.isEmpty()){
                passwordEditText.error = "La contraseña es obligatoria"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            //Inicia sesion en Firebase con el email y la contraseña proporcionados
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    Toast.makeText(this, "Inicio de sesion exitoso", Toast.LENGTH_SHORT).show()
                    // Crear un intent para abrir P8RealtimeDatabase

                    // ----------------------NOTA DE BALMI: EN ESTA SECCIO SE DEBERIA CAMBIAR AL MAIN-------------------------
                    val intent = Intent(this, MainActivity::class.java)

                    //Coonfigurar el Intent para que cierre la actividad actual y evitar regresar a ella
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    //Finalizar la actividad actual para que no quede en el stack
                    finish()
                }else{
                    Toast.makeText(this, "Error al iniciar sesion: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        logoutButton.setOnClickListener {
            auth.signOut()//Cierra la sesion actual de Firebase
            Toast.makeText(this, "Sesion cerrada", Toast.LENGTH_SHORT).show()
            //Reinicia la actividad para volver a la pantalla de login
            //Opcional: volver a la LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}