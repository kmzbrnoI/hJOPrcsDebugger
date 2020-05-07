package kmzbrnoI.hjoprcsdebugger.ui.selectServer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.ui.serverConnector.ServerConnectorActivity
import kmzbrnoI.hjoprcsdebugger.constants.FOUND_SERVERS_RELOAD
import kmzbrnoI.hjoprcsdebugger.constants.REQUEST_WIFI_PERMISSION
import kmzbrnoI.hjoprcsdebugger.helpers.UDPDiscoverResponse
import kmzbrnoI.hjoprcsdebugger.network.UDPDiscover
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb
import kotlinx.android.synthetic.main.select_found_server.view.*
import java.util.ArrayList

class FoundServers : Fragment(), UDPDiscoverResponse {
    lateinit var foundServersAdapter: ArrayAdapter<String>

    var found: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        val sp = context?.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        ServerDb.getInstance(sp)

        context?.let { context ->
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_WIFI_STATE
                )
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

        val view = inflater.inflate(R.layout.select_found_server, container, false).apply {
            foundServersAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, android.R.id.text1, found
            )
            found_servers_list_view.adapter = foundServersAdapter

            found_servers_list_view.setOnItemClickListener { _, _, position, _ ->
                ServerDb.getInstance().let { ServerDbInstance ->
                    if (ServerDbInstance.found[position].active) {
                        connectToServer(context, position)
                    } else {
                        AlertDialog.Builder(context)
                            .setMessage(R.string.conn_server_offline)
                            .setPositiveButton(
                                getString(R.string.yes)
                            ) { _, _ ->
                                connectToServer(context, position)
                            }
                            .setNegativeButton(
                                getString(R.string.no)
                            ) { _, _ -> }.show()
                    }
                }
            }

            swipe_refresh_layout.setOnRefreshListener {
                discoverServers()
                swipe_refresh_layout.isRefreshing = false
            }

            found__create_new_button.setOnClickListener{
                val dialog = CreateServerDialog()

                val transition = fragmentManager?.beginTransaction()
                if (transition != null) {
                    dialog.show(transition, CreateServerDialog.TAG)
                }
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
        val udpDiscover =
            UDPDiscover(context?.getSystemService(Context.WIFI_SERVICE) as WifiManager)
        udpDiscover.delegate = this
        udpDiscover.execute()
    }

    private fun connectToServer(context: Context, index: Int) {
        val intent = Intent(context, ServerConnectorActivity::class.java)
        intent.putExtra("serverType", "found")
        intent.putExtra("serverId", index)
        startActivity(intent)
    }

    private fun updateFoundServers() {
        found.clear()
        for (s in ServerDb.getInstance().found) {
            val statusText = if (s.active) "online" else "offline"
            found.add(s.name + "\t" + s.host + "\n" + s.type + " \t" + statusText)
        }

        foundServersAdapter.notifyDataSetChanged()
    }

    override fun discoveringFinished(output: Int) {
        when (output) {
            FOUND_SERVERS_RELOAD -> {
                updateFoundServers()
            }
        }
    }
}