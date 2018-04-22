package gsort.pos.engsisubiq.EmileMobile

import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.*
import org.json.JSONObject
import android.view.*
import android.widget.AbsListView
import android.widget.Toast
import android.view.animation.AnimationUtils
import android.graphics.Point
import android.support.v7.widget.CardView

class MessagesFragment : Fragment(), IRequestHttpFragment, AbsListView.OnScrollListener, AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var isRequestRunning    : Boolean                = false
    private var listView            : ListView?              = null
    private var floatingButton      : FloatingActionButton?  = null
    private var activity            : MainActivity?          = null
    private var requestHandle       : MessagesRequestHandle? = null
    private var swipeRefreshWidget  : SwipeRefreshLayout?    = null
    private var listViewAdapter     : SimpleAdapter?         = null
    private var messagesOnTheView   : ArrayList<Int>?        = null
    private var listViewModel       : ArrayList<HashMap<String,String>>? = null
    private var fieldsNames         = arrayOf("sender", "title", "message", "date", "time")
    private var fieldsIds           = intArrayOf(R.id.sender, R.id.title, R.id.message, R.id.date, R.id.time)
    private var userProfile         : UserProfile?           = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity            = getActivity() as? MainActivity
        listViewModel       = ArrayList()
        messagesOnTheView   = ArrayList()
        requestHandle       = MessagesRequestHandle(this)
        userProfile         = UserProfile.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.messages_main, container, false)
    }

    override fun onResume() {
        super.onResume()

        activity!!.setToolBarTitle(activity!!.resources.getString(R.string.view_msg))
        activity!!.setNavViewItemSelected(R.id.view_msg)

        listView = activity!!.findViewById(R.id.list)
        floatingButton = activity!!.findViewById(R.id.send_message_action)
        floatingButton!!.setOnClickListener { _ ->
            Toast.makeText(activity!!, "Create a new message...", Toast.LENGTH_SHORT).show()
        }

        listViewAdapter = SimpleAdapter(activity!!, listViewModel, R.layout.messages_list_item, fieldsNames, fieldsIds)
        listView!!.adapter = listViewAdapter

        swipeRefreshWidget = activity!!.findViewById(R.id.swipe_container)
        swipeRefreshWidget!!.setOnRefreshListener(this)

        listView!!.onItemClickListener = this
        listView!!.setOnScrollListener(this)

        // get messages from local storage if exists from previous execution
        requestHandle!!.loadFromLocalStorage()

        /**
         * dynamic message append
         * @WARNING only to test
         */
        addNewMessage()

        // start a request to load for new messages
        onRefresh()
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) { }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        var topRowVerticalPosition = 0

        if (listView != null && listView!!.childCount > 0)
            topRowVerticalPosition = listView!!.getChildAt(0).top

        swipeRefreshWidget!!.isEnabled = firstVisibleItem == 0 && topRowVerticalPosition >= 0

        if (firstVisibleItem > 0)
            hideFloatingButton()
        else
            showFloatingButton()
    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        Toast.makeText(activity!!, listViewModel!![p2]["title"], Toast.LENGTH_LONG).show()
    }

    override fun requestOpened() {
        isRequestRunning = true
        activity!!.runOnUiThread({
            setProgressBarEnabled(true)
            swipeRefreshWidget!!.isRefreshing = false
        })
    }

    override fun requestFinished() {
        isRequestRunning = false
        activity!!.runOnUiThread({
            setProgressBarEnabled(false)
            swipeRefreshWidget!!.isRefreshing = false
        })
    }

    override fun requestSuccess(statusCode: Int, response: Any?, headers: java.util.HashMap<String, String>?) {
        if (statusCode != 200)
            return
        activity!!.runOnUiThread({
            Toast.makeText(activity!!, response as String, Toast.LENGTH_LONG).show()
        })
    }

    override fun requestError(statusCode: Int, response: Any?, headers: java.util.HashMap<String, String>?) {
        activity!!.runOnUiThread({
            Toast.makeText(activity!!, response as String, Toast.LENGTH_LONG).show()
        })
    }

    override fun setViewData(data: Any?) {
        activity!!.runOnUiThread({
            val list = data as ArrayList<*>
            for (message in list) {
                if (messagesOnTheView!!.contains((message as MessageBean).id))
                    continue
                appendMessageToList(message)
            }
        })
    }

    /**
     * Handle user swipe down to request data
     * and start a new http request to check for new messages
     * @override from SwipeRefreshLayout class
     */
    override fun onRefresh() {
        requestHandle!!.request(userProfile!!.id)
    }

    private fun appendMessageToList(message: MessageBean, index: Int = -1) {
        val map = HashMap<String, String>()
        map["sender"]   = message.sender
        map["title"]    = message.title
        map["message"]  = message.message
        map["date"]     = message.date
        map["time"]     = message.time
        if (index != -1) {
            listViewModel!!.add(index, map)
            if (listViewModel!!.size > index) {
                val view: View? = listView!!.getChildAt(index)
                if (view != null) {
                    try {
                        val animation = AnimationUtils.loadAnimation(activity, R.anim.slide_in_top)
                        view.startAnimation(animation)
                        if (userProfile!!.id == message.senderId) {
                            val cardView = view as CardView
                            cardView.setCardBackgroundColor(activity!!.resources.getColor(R.color.messageAlternateColor))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            listViewModel!!.add(map)
        }
        messagesOnTheView!!.add(message.id)
    }

    /**
     * @example
     */
    private fun addNewMessage() {
        Handler().postDelayed({
            val json = JSONObject()
            val sender = JSONObject()
            sender.put("id", userProfile!!.id)
            sender.put("name", "Enoque josé")
            json.put("id", 5000)
            json.put("date", "2018-03-28T23:56:46.617378")
            json.put("title", "Teste de mensagem dinâmica")
            json.put("sender", sender)
            json.put("message", "An easy adapter to map static data to views defined in an XML file. You can specify the data backing the list as an ArrayList of Maps. Each entry in the ArrayList corresponds to one row in the list. The Maps contain the data for each row. You also specify an XML file that defines the views used to display the row, and a mapping from keys in the Map to specific views")
            val objc = MessageBean(json)
            appendMessageToList(objc,0)
            listViewAdapter!!.notifyDataSetChanged()
        }, 10000)
    }

    private fun showFloatingButton() {
        floatingButton!!.animate().setDuration(650).translationY(0F).start()
    }

    // magic happens here!
    private fun hideFloatingButton() {
        val point = Point()
        activity!!.window.windowManager.defaultDisplay.getSize(point)
        val translation = floatingButton!!.y - point.y
        floatingButton!!.animate().setDuration(800).translationYBy(-translation).start()
    }

    private fun setProgressBarEnabled(enabled: Boolean) {
        val visibilityMode = if (enabled) View.VISIBLE else View.INVISIBLE
        val progressBar = activity!!.findViewById<ProgressBar>(R.id.messagesProgressBar)

        if (progressBar != null && progressBar.visibility != visibilityMode)
            progressBar.visibility = visibilityMode
    }
}
