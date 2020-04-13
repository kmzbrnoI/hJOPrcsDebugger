package kmzbrnoI.hjoprcsdebugger.ui.selectServer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kmzbrnoI.hjoprcsdebugger.R

class SelectServerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_server_activity)

        if (savedInstanceState == null) {
            val fragment = SelectServer()
            supportFragmentManager.beginTransaction()
                .replace(R.id.select_server_activity, fragment)
                .commit()
        }
    }
}
