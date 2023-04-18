package com.check.kmm.pluginchecks.android.kable.check.data


import com.juul.kable.DiscoveredService
import com.juul.kable.Peripheral
import kotlinx.coroutines.flow.*

class Device(
    private val peripheral: Peripheral,
) : Peripheral by peripheral {
    private val _discoveredServices = MutableStateFlow<List<DiscoveredService>>(listOf())
    val discoveredServices = _discoveredServices.asStateFlow()

    override suspend fun connect() {
        peripheral.connect()

        _discoveredServices.emit(peripheral.services!!)

    }
}
