package com.example.semana_9

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private var isDark = false
    private var themeItem: MenuItem? = null

    private lateinit var userPreferences: SharedPreferences
    private lateinit var imageView: ImageView
    private lateinit var editTextNombre: EditText
    private lateinit var editTextEdad: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var buttonGuardar: Button
    private lateinit var buttonCargar: Button
    private lateinit var textViewSesiones: TextView

    companion object {
        private const val USER_PREFS = "UserPrefs"
        private const val THEME_PREFS = "prefs"
        private const val KEY_NOMBRE = "nombre"
        private const val KEY_EDAD = "edad"
        private const val KEY_EMAIL = "email"
        private const val KEY_SESIONES = "sesiones_iniciadas"
        private const val KEY_DARK_THEME = "dark"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val themePrefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE)
        isDark = themePrefs.getBoolean(KEY_DARK_THEME, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        userPreferences = getSharedPreferences(USER_PREFS, MODE_PRIVATE)

        initViews()
        incrementarSesiones()
        setupListeners()
        cargarDatos()
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        editTextNombre = findViewById(R.id.editTextText)
        editTextEdad = findViewById(R.id.editTextNumber)
        editTextEmail = findViewById(R.id.editTextTextEmailAddress)
        buttonGuardar = findViewById(R.id.button3)
        buttonCargar = findViewById(R.id.button4)
        textViewSesiones = findViewById(R.id.textView2)
    }

    private fun setupListeners() {
        buttonGuardar.setOnClickListener {
            guardarDatos()
        }

        buttonCargar.setOnClickListener {
            cargarDatos()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        themeItem = menu.findItem(R.id.theme_dark)
        updateIcon()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.theme_dark) {
            isDark = !isDark

            getSharedPreferences(THEME_PREFS, MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK_THEME, isDark)
                .apply()

            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateIcon() {
        themeItem?.setIcon(if (isDark) R.drawable.sun else R.drawable.moon)
    }

    private fun incrementarSesiones() {
        val sesionesActuales = userPreferences.getInt(KEY_SESIONES, 0)
        val nuevasSesiones = sesionesActuales + 1

        with(userPreferences.edit()) {
            putInt(KEY_SESIONES, nuevasSesiones)
            apply()
        }

        actualizarTextoSesiones(nuevasSesiones)
    }

    private fun actualizarTextoSesiones(sesiones: Int) {
        textViewSesiones.text = "Has iniciado sesión: $sesiones veces"
    }

    private fun guardarDatos() {
        val nombre = editTextNombre.text.toString().trim()
        val edadString = editTextEdad.text.toString().trim()
        val email = editTextEmail.text.toString().trim()

        editTextNombre.error = null
        editTextEdad.error = null
        editTextEmail.error = null

        when {
            nombre.isEmpty() -> {
                editTextNombre.error = "El nombre es requerido"
                editTextNombre.requestFocus()
                return
            }
            nombre.length < 2 -> {
                editTextNombre.error = "El nombre debe tener al menos 2 caracteres"
                editTextNombre.requestFocus()
                return
            }
            edadString.isEmpty() -> {
                editTextEdad.error = "La edad es requerida"
                editTextEdad.requestFocus()
                return
            }
            email.isEmpty() -> {
                editTextEmail.error = "El email es requerido"
                editTextEmail.requestFocus()
                return
            }
            !isValidEmail(email) -> {
                editTextEmail.error = "Formato de email inválido"
                editTextEmail.requestFocus()
                return
            }
        }

        val edad = try {
            edadString.toInt().also { edadValue ->
                when {
                    edadValue < 0 -> {
                        editTextEdad.error = "La edad no puede ser negativa"
                        editTextEdad.requestFocus()
                        return
                    }
                    edadValue > 150 -> {
                        editTextEdad.error = "La edad no puede ser mayor a 150 años"
                        editTextEdad.requestFocus()
                        return
                    }
                }
            }
        } catch (e: NumberFormatException) {
            editTextEdad.error = "Ingresa un número válido"
            editTextEdad.requestFocus()
            return
        }

        with(userPreferences.edit()) {
            putString(KEY_NOMBRE, nombre)
            putInt(KEY_EDAD, edad)
            putString(KEY_EMAIL, email)
            apply()
        }

        Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
    }

    private fun cargarDatos() {
        val nombre = userPreferences.getString(KEY_NOMBRE, "")
        val edad = userPreferences.getInt(KEY_EDAD, 0)
        val email = userPreferences.getString(KEY_EMAIL, "")
        val sesiones = userPreferences.getInt(KEY_SESIONES, 0)

        editTextNombre.setText(nombre)
        editTextEmail.setText(email)

        if (edad > 0) {
            editTextEdad.setText(edad.toString())
        } else {
            editTextEdad.setText("")
        }

        actualizarTextoSesiones(sesiones)

        if (!nombre.isNullOrEmpty() || !email.isNullOrEmpty() || edad > 0) {
            Toast.makeText(this, "Datos cargados exitosamente", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No hay datos guardados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun limpiarCampos() {
        editTextNombre.setText("")
        editTextEdad.setText("")
        editTextEmail.setText("")
        editTextNombre.error = null
        editTextEdad.error = null
        editTextEmail.error = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDark", isDark)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isDark = savedInstanceState.getBoolean("isDark", isDark)
    }
}