package gsort.pos.engsisubiq.EmileMobile

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.*
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ProgressBar
import java.util.HashMap

class WriteMessageFragment : Fragment(), IRequestHttpFragment {

    private var isRequestRunning    = false
    private var messageCharsLimit   = 300
    private var messageText         : String?           = null
    private var messageArgs         : JSONObject?       = null
    private var activity            : MainActivity?     = null
    private var destinationTitle    : TextView?         = null
    private var textField           : EditText?         = null
    private var msgCharsCount       : TextView?         = null
    private var progressBar         : ProgressBar?      = null
    private var requestHandle       : WriteMessageRequestHandle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as? MainActivity
        requestHandle = WriteMessageRequestHandle(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        messageArgs = JSONObject(arguments!!.getString("message_args"))
        return inflater.inflate(R.layout.write_message_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        destinationTitle = view.findViewById<View>(R.id.destination_title) as? TextView
        progressBar      = view.findViewById(R.id.progressBar2)
        destinationTitle!!.text = messageArgs!!.getString("title")

        textField = view.findViewById(R.id.msg_text)
        msgCharsCount = view.findViewById(R.id.msg_chars_count)
        msgCharsCount!!.text = messageCharsLimit.toString()

        // listener changes in TextArea to update the messages characters length
        textField!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                messageText = s.toString()
                msgCharsCount!!.text = (messageCharsLimit - s.length).toString()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        activity!!.setToolBarTitle("Nova mensagem para:")
        activity!!.setNavViewItemSelected(R.id.send_msg)
        activity!!.setToolBarBackButton()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.write_message, menu)
        super.onCreateOptionsMenu(menu, inflater)

        var drawable = menu.findItem(R.id.action_wm_submit).icon
        drawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(drawable, ContextCompat.getColor(activity!!, R.color.white))
        menu.findItem(R.id.action_wm_submit).icon = drawable
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_wm_submit) {
            if (isRequestRunning)
                return false
            Log.i("WriteMessageFragment", messageArgs.toString())
            if (messageText!!.length > messageCharsLimit) {
                activity!!.showAlertDialog("Erro!","Número de caracteres acima do permitido ($messageCharsLimit)")
            } else {
                activity!!.setKeyboardEnabled(false)
                requestHandle!!.request(messageArgs!!.getString("url_service") + "/", getSubmitData())
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun requestOpened() {
        isRequestRunning = true
        activity!!.runOnUiThread({
            progressBar!!.visibility = View.VISIBLE
        })
    }

    override fun requestFinished() {
        isRequestRunning = false
        activity!!.runOnUiThread({
            progressBar!!.visibility = View.INVISIBLE
        })
    }

    override fun requestError(statusCode: Int, response: Any?, headers: HashMap<String, String>?) {
        activity!!.runOnUiThread({
            Toast.makeText(activity, "Ocorreu algum erro! Tente novamente.", Toast.LENGTH_LONG).show()
        })
    }

    override fun requestSuccess(statusCode: Int, response: Any?, headers: HashMap<String, String>?) {
        // msgCharsCount!!.text = messageCharsLimit.toString()
        activity!!.runOnUiThread({
            textField!!.setText("")
            textField!!.text.clear()
            Toast.makeText(activity, "Enviado com sucesso!", Toast.LENGTH_SHORT).show()
            textField!!.requestFocus()
            Handler().postDelayed({
                activity!!.setKeyboardEnabled(true, textField)
            }, 3000)
        })
    }

    override fun setViewData(data: Any?) {
        // pass
    }

    /**
     * Constrói o json da requisição de envio de mensagem.
     * É importante observar que o valor de 'post_data_key' conterá uma string que será configurada
     * de acordo com o destinatário ─ selecionado na tela anterior. Por exemplo:
     *  - se o destinatário for "All Students Of Program", 'post_data_key' terá 'program_id'
     *  - se o destinatário for "Students Of A Section", 'post_data_key' terá 'course_section_id'
     *
     * Para todas as mensagens os parâmetros obrigatórios são: 'message', 'sender', 'device' e 'title'
     * Sendo título também configurado de acordo com o destinatário
     */
    private fun getSubmitData(): JSONObject {
        val json = JSONObject()
        json.put("device", "ANDROID_DEVICE")
        json.put("message", messageText)
        json.put("sender", messageArgs!!.getString("sender"))
        json.put("title", messageArgs!!.getString("title"))
        json.put(messageArgs!!.getString("post_data_key"), messageArgs!!.getString("post_data_val"))
        return json
    }
}