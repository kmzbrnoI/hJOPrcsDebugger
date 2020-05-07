package kmzbrnoI.hjoprcsdebugger.ui.selectServer

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.helpers.CreateServerDialogResponse
import kmzbrnoI.hjoprcsdebugger.ui.serverConnector.ServerConnectorActivity
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb
import kotlinx.android.synthetic.main.select_stored_server.view.*
import java.util.ArrayList

class StoredServers : Fragment(), CreateServerDialogResponse {
    lateinit var storedServersAdapter: ArrayAdapter<String>

    var stored: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        val sp = context?.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        ServerDb.getInstance(sp)

        val view = inflater.inflate(R.layout.select_stored_server, container, false).apply {
            storedServersAdapter = ArrayAdapter(
                context,
                android.R.layout.simple_list_item_1, android.R.id.text1, stored
            )
            stored_servers_list_view.adapter = storedServersAdapter

            updateStoredServers()

            stored_servers_list_view.setOnItemClickListener { _, _, position, _ ->
                ServerDb.getInstance().let { ServerDbInstance ->
                    if (ServerDbInstance.stored[position].active) {
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

            stored__create_new_button.setOnClickListener{
                createServerDialog()
            }
        }

        return view
    }

    private fun createServerDialog() {
        val dialog = CreateServerDialog()
        dialog.delegate = this

        val transition = fragmentManager?.beginTransaction()
        if (transition != null) {
            dialog.show(transition, CreateServerDialog.TAG)
        }
    }

    private fun connectToServer(context: Context, index: Int) {
        val intent = Intent(context, ServerConnectorActivity::class.java)
        intent.putExtra("serverType", "stored")
        intent.putExtra("serverId", index)
        startActivity(intent)
    }

    private fun updateStoredServers() {
        stored.clear()
        for (s in ServerDb.getInstance().stored)
            stored.add(s.name + "\t" + s.host + "\n" + s.type)

        storedServersAdapter.notifyDataSetChanged()
    }

    override fun onServerCreated() {
        updateStoredServers()
    }
}