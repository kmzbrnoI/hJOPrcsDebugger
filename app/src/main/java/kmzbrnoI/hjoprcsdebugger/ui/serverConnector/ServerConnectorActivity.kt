package kmzbrnoI.hjoprcsdebugger.ui.serverConnector

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.BuildConfig
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication

class ServerConnectorActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_connector_activity)
        setTitle(getString(R.string.app_name) + " v"+ BuildConfig.VERSION_NAME)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = ServerConnector()
            supportFragmentManager.beginTransaction()
                .replace(R.id.server_connector_activity, fragment)
                .commit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                TCPClientApplication.getInstance().disconnect()
                finish()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
