package kmzbrnoI.hjoprcsdebugger.ui.moduleInfo

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.network.TCPClientApplication

class ModuleInfoActivity: AppCompatActivity() {
    private lateinit var module: Module

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_info_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = intent.extras?.getString("addressOfModule") + ": " + intent.extras?.getString("nameOfModule")

        if (savedInstanceState == null) {
            module = Module()
            supportFragmentManager.beginTransaction()
                .replace(R.id.module_info_activity, module)
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }

    override fun onBackPressed() {
        TCPClientApplication.getInstance().disconnectModule()
        module.adapter.sound?.release()
        finish()

        super.onBackPressed()
    }
}