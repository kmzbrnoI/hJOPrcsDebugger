package kmzbrnoI.hjoprcsdebugger.ui.modulesList

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication

class ModulesListActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modules_list_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.modules_list)

        if (savedInstanceState == null) {
            val fragment = ModulesList()
            supportFragmentManager.beginTransaction()
                .replace(R.id.modules_list_activity, fragment)
                .commit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                TCPClientApplication.getInstance().disconnectModule()
                TCPClientApplication.getInstance().disconnect()

                startActivity(parentActivityIntent)

                finish()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
