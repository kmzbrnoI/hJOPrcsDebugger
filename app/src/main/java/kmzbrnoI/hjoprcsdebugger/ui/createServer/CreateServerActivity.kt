package kmzbrnoI.hjoprcsdebugger.ui.createServer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.databinding.NewServerActivityBinding
import kmzbrnoI.hjoprcsdebugger.databinding.NewServerBinding
import kmzbrnoI.hjoprcsdebugger.models.Server
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb

class CreateServerActivity: AppCompatActivity() {
    private lateinit var binding: NewServerBinding
    private lateinit var bindingActivity: NewServerActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewServerBinding.inflate(layoutInflater)
        bindingActivity = NewServerActivityBinding.inflate(layoutInflater)
        setContentView(R.layout.new_server_activity)

        if (savedInstanceState == null) {
            val fragment = CreateServer()
            supportFragmentManager.beginTransaction()
                .replace(R.id.new_server_activity, fragment)
                .commit()
        }

        bindingActivity.saveButton.setOnClickListener {
            val name = binding.nameText.text.toString()
            val port = binding.portText.text.toString()
            val ipAdr = binding.ipText.text.toString()
            val about = binding.aboutText.text.toString()

            if (name == "" || port == "" || ipAdr == "") {
                AlertDialog.Builder(this)
                    .setMessage(R.string.ns_warning_compulsory)
                    .setCancelable(false)
                    .setPositiveButton("ok"
                    ) { _, _ -> }
                    .setCancelable(false)
                    .show()
            } else if (name.contains("--")) {
                AlertDialog.Builder(this)
                    .setMessage(R.string.ns_warning_invalid_characters)
                    .setCancelable(false)
                    .setPositiveButton("ok"
                    ) { _, _ -> }
                    .setCancelable(false)
                    .show()
            } else {
                ServerDb.getInstance().addStoredServer(
                    Server(
                        name,
                        ipAdr,
                        Integer.parseInt(port),
                        false,
                        about,
                        "",
                        ""
                    )
                )

                setResult(Activity.RESULT_OK, Intent())
                finish()
            }
        }

        bindingActivity.backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, Intent())
            finish()
        }
    }
}
