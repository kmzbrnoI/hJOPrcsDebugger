package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication

class ModuleInfoActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_info_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = intent.extras?.getString("addressOfModule") + ": " + intent.extras?.getString("nameOfModule")

        if (savedInstanceState == null) {
            val fragment = Module()
            supportFragmentManager.beginTransaction()
                .replace(R.id.module_info_activity, fragment)
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }

    override fun onBackPressed() {
        TCPClientApplication.getInstance().disconnectModule()
        finish()

        super.onBackPressed()
    }
}