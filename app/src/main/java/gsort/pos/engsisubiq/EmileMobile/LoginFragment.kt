package gsort.pos.engsisubiq.EmileMobile

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import java.util.HashMap
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener

class LoginFragment : Fragment(), IRequestHttpFragment {

    private var submitLoginBtn      : Button?               = null
    private var lostPasswordBtn     : Button?               = null
    private var loginField          : EditText?             = null
    private var passwordField       : EditText?             = null
    private var activity            : MainActivity?         = null
    private var progressBar         : ProgressBar?          = null
    private var loginRequestHandle  : LoginRequestHandle?   = null

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putBoolean("SUBMIT_STATE", submitLoginBtn!!.isEnabled)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as? MainActivity
        loginRequestHandle = LoginRequestHandle(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.login_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // find inputs and buttons as view object
        loginField      = view.findViewById(R.id.login_field)
        passwordField   = view.findViewById(R.id.password_field)
        submitLoginBtn  = view.findViewById(R.id.submit_login_btn)
        lostPasswordBtn = view.findViewById(R.id.lost_password_btn)

        // handle clicks in "Login" button
        submitLoginBtn!!.setOnClickListener {
            val login    = loginField!!.text.toString()
            val password = passwordField!!.text.toString()

            if (login == "" || password == "")
                activity!!.showAlertDialog("Erro!", "Digite o login e a senha!", "OK", { println("confirm callback!") })
            else
                loginRequestHandle!!.post(login, password)
        }

        // handle clicks in "Lost Password" button
        lostPasswordBtn!!.setOnClickListener {
            activity!!.addFragment(LostPasswordFragment(), "lost_password")
        }

        passwordField!!.setOnEditorActionListener(OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event != null && event.action == KeyEvent.ACTION_DOWN) {
                Log.i("Login", "Submit now------")
                submitLoginBtn!!.performClick()
            }
            false
        })
    }

    override fun requestFinished() {
        activity!!.runOnUiThread({
            setProgressBarEnabled(false)
        })
    }

    override fun requestOpened() {
        activity!!.runOnUiThread({
            submitLoginBtn!!.isEnabled = false
            lostPasswordBtn!!.isEnabled = false
            setProgressBarEnabled(true)
        })
    }

    override fun requestSuccess(statusCode: Int, response: Any?, headers: HashMap<String, String>?) {
        activity!!.runOnUiThread({
            Toast.makeText(activity, response as String, Toast.LENGTH_LONG).show()
            Handler().postDelayed({
                activity!!.loadInitialFragment(true)
            }, 3500)
        })
    }

    override fun requestError(statusCode: Int, response: Any?, headers: java.util.HashMap<String, String>?) {
        activity!!.runOnUiThread({
            Toast.makeText(activity, "Wrong Credentials!!", Toast.LENGTH_LONG).show()
            submitLoginBtn!!.isEnabled  = true
            lostPasswordBtn!!.isEnabled = true
        })
    }

    override fun setViewData(data: Any?) { }

    private fun setProgressBarEnabled(enabled: Boolean) {
        val visibilityMode = if (enabled)
            View.VISIBLE
        else
            View.INVISIBLE
        if (progressBar == null)
            progressBar = activity!!.findViewById(R.id.progressBar)
        progressBar!!.visibility = visibilityMode
    }
}
