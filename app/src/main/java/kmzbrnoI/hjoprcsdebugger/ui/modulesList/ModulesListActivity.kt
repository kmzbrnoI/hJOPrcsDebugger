package kmzbrnoI.hjoprcsdebugger.ui.modulesList

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication

class ModulesListActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.modules_list_activity)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val fragment = ModulesList()
            supportFragmentManager.beginTransaction()
                .replace(R.id.modules_list_activity, fragment)
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        TCPClientApplication.getInstance().disconnect()

        startActivity(parentActivityIntent)

        return true
    }
}