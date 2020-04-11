package kmzbrnoI.hjoprcsdebugger

import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.constants.FOUND_SERVERS_RELOAD
import kmzbrnoI.hjoprcsdebugger.constants.REQUEST_WIFI_PERMISSION
import kmzbrnoI.hjoprcsdebugger.helpers.UDPDiscoverResponse
import kmzbrnoI.hjoprcsdebugger.network.UDPDiscover
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb
import java.util.ArrayList

class SelectServer : Fragment(), UDPDiscoverResponse {
    var found: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true
        val view = inflater.inflate(R.layout.select_server, container, false)

        val sp = context?.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        ServerDb.instance = ServerDb(sp)

        context?.let { context ->
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_WIFI_STATE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                discoverServers()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_WIFI_STATE),
                    REQUEST_WIFI_PERMISSION
                )
            }
        }

        return view
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_WIFI_PERMISSION && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            discoverServers()
        }
    }

    private fun discoverServers() {
        val udpDiscover = UDPDiscover(context?.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        udpDiscover.delegate = this
        udpDiscover.execute()
    }

    fun updateFoundServers() {
        found.clear()
        for (s in ServerDb.instance?.found!!) {
            val statusText = if (s.active) "online" else "offline"
            found.add(s.name + "\t" + s.host + "\n" + s.type + " \t" + statusText)
        }

        // fAdapter.notifyDataSetChanged()
    }

    override fun discoveringFinished(output: Int) {
        when (output) {
            FOUND_SERVERS_RELOAD -> {
                updateFoundServers()
            }
        }
    }
}