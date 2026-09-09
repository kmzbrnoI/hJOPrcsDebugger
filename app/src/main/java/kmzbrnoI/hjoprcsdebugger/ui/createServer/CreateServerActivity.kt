package kmzbrnoI.hjoprcsdebugger.ui.createServer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.databinding.NewServerActivityBinding
import kmzbrnoI.hjoprcsdebugger.models.Server
import kmzbrnoI.hjoprcsdebugger.storage.ServerDb

class CreateServerActivity: AppCompatActivity() {
    private lateinit var binding: NewServerActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewServerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragment = CreateServer()
            supportFragmentManager.beginTransaction()
                .replace(R.id.new_server_container, fragment)
                .commit()
        }
        supportFragmentManager.executePendingTransactions()

        val createServerFragment = supportFragmentManager.findFragmentById(R.id.new_server_container) as CreateServer

        binding.saveButton.setOnClickListener {
            val name = createServerFragment.binding.nameText.text.toString()
            val port = createServerFragment.binding.portText.text.toString()
            val ipAdr = createServerFragment.binding.ipText.text.toString()
            val about = createServerFragment.binding.aboutText.text.toString()

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

        binding.backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED, Intent())
            finish()
        }
    }
}