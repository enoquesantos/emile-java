package gsort.pos.engsisubiq.EmileMobile

import android.os.Bundle
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
import java.util.HashMap

class WriteMessageFragment : Fragment(), IRequestHttpFragment {

    private var messageText         : String?           = null
    private var submitArgs          : JSONObject?       = null
    private var activity            : MainActivity?     = null
    private var destinationTitle    : TextView?         = null
    private var textField           : EditText?         = null
    private var msgCharsCount       : TextView?         = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getActivity() as? MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        submitArgs = JSONObject(arguments.getString("submit_args"))
        return inflater!!.inflate(R.layout.write_message_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        destinationTitle = view.findViewById<View>(R.id.destination_title) as? TextView
        destinationTitle!!.text = submitArgs!!.getString("title")

        textField = view.findViewById(R.id.msg_text)
        msgCharsCount = view.findViewById(R.id.msg_chars_count)

        textField!!.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                messageText = s.toString()
                msgCharsCount!!.text = (300 - s.length).toString()
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
            Toast.makeText(getActivity(), "Enviando...", Toast.LENGTH_LONG).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun requestOpened() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestFinished() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestError(statusCode: Int, response: Any?, headers: HashMap<String, String>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun requestSuccess(statusCode: Int, response: Any?, headers: HashMap<String, String>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setViewData(data: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
