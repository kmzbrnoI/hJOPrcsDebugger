package kmzbrnoI.hjoprcsdebugger.ui.createServer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R

class CreateServer : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true

        return inflater.inflate(R.layout.new_server, container, false).apply {}
    }
}