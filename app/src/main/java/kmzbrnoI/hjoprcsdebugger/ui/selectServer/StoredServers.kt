package kmzbrnoI.hjoprcsdebugger.ui.selectServer

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.constants.STORED_SERVERS_RELOAD
import kmzbrnoI.hjoprcsdebugger.databinding.SelectStoredServerBinding
import kmzbrnoI.hjoprcsdebugger.responses.CreateServerDialogResponse
import kmzbrnoI.hjoprcsdebugger.responses.ServerDbResponse
import kmzbrnoI.hjoprcsdebugger.ui.serverConnector.ServerConnectorActivity
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb
import kmzbrnoI.hjoprcsdebugger.ui.createServer.CreateServer
import java.util.ArrayList

class StoredServers : Fragment(), ServerDbResponse {
    lateinit var storedServersAdapter: ArrayAdapter<String>
    private var _binding: SelectStoredServerBinding? = null
    val binding get() = _binding!!

    var stored: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        val sp = context?.getSharedPreferences("prefs", Context.MODE_PRIVATE)

        ServerDb.getInstance(sp)

        _binding = SelectStoredServerBinding.inflate(inflater, container, false)
        binding.apply {
            storedServersAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, stored
            )
            storedServersListView.adapter = storedServersAdapter

            updateStoredServers()

            storedServersListView.setOnItemClickListener { _, _, position, _ ->
                ServerDb.getInstance().let { ServerDbInstance ->
                    if (ServerDbInstance.stored[position].active) {
                        connectToServer(requireContext(), position)
                    } else {
                        AlertDialog.Builder(requireContext())
                            .setMessage(R.string.conn_server_offline)
                            .setPositiveButton(
                                getString(R.string.yes)
                            ) { _, _ ->
                                connectToServer(requireContext(), position)
                            }
                            .setNegativeButton(
                                getString(R.string.no)
                            ) { _, _ -> }
                            .setCancelable(false)
                            .show()
                    }
                }
            }

            storedServersListView.setOnItemLongClickListener { _, _, position, _ ->
                storedServersListView.getChildAt(position - storedServersListView.firstVisiblePosition)?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryLighten))
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.delete_server_alert)
                    .setPositiveButton(
                        getString(R.string.yes)
                    ) { _, _ ->
                        removeServer(position)
                        storedServersListView.getChildAt(position - storedServersListView.firstVisiblePosition)?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
                    }
                    .setNegativeButton(
                        getString(R.string.no)
                    ) { _, _ ->
                        storedServersListView.getChildAt(position - storedServersListView.firstVisiblePosition)?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))
                    }
                    .setCancelable(false)
                    .show()
                true
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun removeServer(position: Int) {
        val servedDb = ServerDb.getInstance()
        servedDb.delegate = this

        servedDb.removeStoredServer(position)
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

    override fun response(output: Int) {
        when (output) {
            STORED_SERVERS_RELOAD -> {
                updateStoredServers()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == STORED_SERVERS_RELOAD) {
            if (resultCode == Activity.RESULT_OK) {
                updateStoredServers()
            }
        }
    }
}