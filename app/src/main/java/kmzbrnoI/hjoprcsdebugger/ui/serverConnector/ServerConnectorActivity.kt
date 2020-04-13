package kmzbrnoI.hjoprcsdebugger.ui.serverConnector

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.R

class ServerConnectorActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_connector_activity)

        if (savedInstanceState == null) {
            val fragment = ServerConnector()
            supportFragmentManager.beginTransaction()
                .replace(R.id.server_connector_activity, fragment)
                .commit()
        }
    }
}