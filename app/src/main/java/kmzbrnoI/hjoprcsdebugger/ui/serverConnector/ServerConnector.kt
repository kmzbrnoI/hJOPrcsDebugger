package kmzbrnoI.hjoprcsdebugger.ui.serverConnector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R

class ServerConnector: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        val view = inflater.inflate(R.layout.server_connector, container, false).apply {  }

        return view
    }
}