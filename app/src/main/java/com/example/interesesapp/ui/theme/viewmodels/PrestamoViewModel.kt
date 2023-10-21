package com.example.interesesapp.ui.theme.viewmodels

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.interesesapp.components.Alert
import com.example.interesesapp.components.MainButton
import com.example.interesesapp.components.MainTextField
import com.example.interesesapp.components.ShowInfoCards
import com.example.interesesapp.components.SpaceH
import com.example.interesesapp.components.models.PrestamoState
import com.example.interesesapp.ui.theme.InteresesAppTheme
import com.example.interesesapp.views.ContentHomeView
import com.example.interesesapp.views.calcularCuota
import java.math.BigDecimal
import java.math.RoundingMode


class PrestamoViewModel: ViewModel() {
    var state by mutableStateOf(PrestamoState())
        private set

    fun confirmDialog(){
        state = state.copy(showAlert = false)
    }

    fun limpiar(){
        state = state.copy(
            montoPrestamo = "",
            cantCuotas = "",
            tasa = "",
            montoInteres = 0.0,
            montoCuota = 0.0
        )
    }

    fun onValue(value: String, campo: String){
        Log.i("chdez", campo)
        Log.i("chdez", value)

        when (campo) {
            "montoPrestamo" -> state = state.copy(montoPrestamo = value)
            "cuotas" -> state = state.copy(cantCuotas = value)
            "tasa" -> state = state.copy(tasa = value)
        }
    }

    private fun calcularTotal(
        montoPrestamo: Double, cantCuotas: Int, tasaInteresAnual: Double
    ): Double {
        val res = cantCuotas * calcularCuota(montoPrestamo, cantCuotas, tasaInteresAnual)
        return BigDecimal(res).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    private fun calcularCuota(
        montoPrestamo: Double, cantCuotas: Int, tasaInteresAnual: Double
    ): Double{
        val tasaInteresMensual = tasaInteresAnual / 12 / 100

        val cuota = montoPrestamo * tasaInteresMensual * Math.pow(
            1 + tasaInteresMensual,
            cantCuotas.toDouble()
        ) / (Math.pow(1 + tasaInteresMensual, cantCuotas.toDouble()) - 1)

        val cuotaRedondeada = BigDecimal(cuota).setScale(2, RoundingMode.HALF_UP).toDouble()

        return cuotaRedondeada
    }

    fun calcular() {
        val montoPrestamo = state.montoPrestamo
        val cantCuotas = state.cantCuotas
        val tasa = state.tasa
        if (montoPrestamo!="" && cantCuotas!="" && tasa!= "") {
            state = state.copy(
                montoCuota = calcularCuota(montoPrestamo.toDouble(), cantCuotas.toInt(), tasa.toDouble()),
                montoInteres = calcularCuota(montoPrestamo.toDouble(), cantCuotas.toInt(), tasa.toDouble())
            )
        }  else {
            state = state.copy(showAlert = true)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(viewModel: PrestamoViewModel){
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text(text = "Calculadora Prestamos", color = Color.White)},
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

    }) {
        ContentHomeView(it, viewModel)
    }
}

@Composable
fun ContentHomeView(paddingValues: PaddingValues,viewModel: PrestamoViewModel){
    val state = viewModel.state

    ShowInfoCards(
        titleInteres = "Total",
        montoInteres = state.montoInteres,
        titleMonto = "Cuota:",
        monto = state.montoCuota
    )

    MainTextField(value = state.montoPrestamo,
        onValueChange = { viewModel.onValue(value = it, campo = "montoPrestamo") }, label = "Prestamo")
    SpaceH()
    MainTextField(value = state.cantCuotas,
        onValueChange = { viewModel.onValue(value = it, campo = "Cuotas") }, label = "Cuotas")
    SpaceH(10.dp)
    MainTextField(value = state.tasa,
        onValueChange = { viewModel.onValue(value = it, campo = "tasa") }, label = "Tasa")
    SpaceH(10.dp)

    MainButton(text = "Calcular") {
        viewModel.calcular()
    }
    SpaceH()

    MainButton(text = "Limpiar", color = Color.Red) {
        viewModel.limpiar()
    }

    if (viewModel.state.showAlert){
        Alert(title = "Alerta",
            message = "Ingresa los Datos",
            confirmText = "Aceptar",
            onConfirmClick = { viewModel.confirmDialog() }) {
        }
    }
}
class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: PrestamoViewModel by viewModels()
        setContent{
            InteresesAppTheme() {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeView(viewModel)
                }
            }
        }
    }
}
