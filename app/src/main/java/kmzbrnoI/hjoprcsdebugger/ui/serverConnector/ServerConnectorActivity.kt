package kmzbrnoI.hjoprcsdebugger.ui.serverConnector

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication

class ServerConnectorActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_connector_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = ServerConnector()
            supportFragmentManager.beginTransaction()
                .replace(R.id.server_connector_activity, fragment)
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        TCPClientApplication.getInstance().disconnect()
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        TCPClientApplication.getInstance().disconnect()

        super.onBackPressed()
    }
}