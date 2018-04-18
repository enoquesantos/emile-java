package gsort.pos.engsisubiq.EmileMobile;

import android.support.v4.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import org.json.JSONObject
import android.widget.ArrayAdapter
import org.json.JSONArray

class SendMessageFragment : Fragment(), AdapterView.OnItemClickListener {

    private var selectedOption      : JSONObject?                        = null
    private var activity            : MainActivity?                      = null
    private var listView            : ListView?                          = null
    private var options             : Array<String>?                     = null
    private var destinations        : JSONArray?                         = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity        = getActivity() as? MainActivity
        selectedOption  = JSONObject()
        destinations    = UserProfile.getInstance().messageDestinations

        loadDestinations()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.send_message_main, container, false)
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        selectedOption = destinations!![p2] as JSONObject

        Toast.makeText(activity!!, selectedOption!!.getString("title"), Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            val writeFragment = WriteMessageFragment()
            val bundle = Bundle()
            bundle.putString("submit_args", selectedOption!!.toString())
            writeFragment.arguments = bundle
            activity!!.addFragment(writeFragment, "write_message")
        }, 550)
    }

    override fun onResume() {
        super.onResume()

        activity!!.setToolBarTitle("Enviar mensagem para:")
        activity!!.setNavViewItemSelected(R.id.send_msg)

        listView = activity!!.findViewById(R.id.sm_list)
        listView!!.adapter = ArrayAdapter(activity!!, android.R.layout.simple_list_item_checked, options)
        listView!!.onItemClickListener = this
    }

    private fun loadDestinations() {
        var i = 0
        var destination: JSONObject
        val length = destinations!!.length()
        val values = ArrayList<String>()

        while (i < length) {
            destination = destinations!![i++] as JSONObject
            values.add(destination.getString("title"))
        }

        options = values.toTypedArray()
    }
}
