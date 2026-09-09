package kmzbrnoI.hjoprcsdebugger.ui.createServer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.databinding.NewServerBinding

class CreateServer : Fragment() {
    private var _binding: NewServerBinding? = null
    public val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        retainInstance = true
        _binding = NewServerBinding.inflate(inflater, container, false)
        return binding.root
    }
}